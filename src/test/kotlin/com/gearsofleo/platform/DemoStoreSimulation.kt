package com.gearsofleo.platform

import io.gatling.javaapi.core.Choice
import io.gatling.javaapi.core.CoreDsl.ElFileBody
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers
import io.gatling.javaapi.core.CoreDsl.csv
import io.gatling.javaapi.core.CoreDsl.doIf
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.feed
import io.gatling.javaapi.core.CoreDsl.jmesPath
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.pause
import io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers
import io.gatling.javaapi.core.CoreDsl.randomSwitch
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration
import kotlin.random.Random

private val authHeader = mapOf(
    "authorization" to "Bearer #{jwtToken}"
)
const val isAuthenticated = "authenticated"

private fun initSession() = exec { session -> session.set(isAuthenticated, false) }

object Auth {
    fun authenticate() =
        doIf { session -> !session.getBoolean(isAuthenticated) }.then(
            exec(
                http("Authenticate")
                    .post("/api/authenticate")
                    .body(
                        StringBody(
                            """
                   {
                        "username": "admin",
                        "password": "admin"
                    } 
                """.trimIndent()
                        )
                    )
                    .check(status().shouldBe(200))
                    .check(jsonPath("$.token").saveAs("jwtToken")),
            ).exec { s -> s.set(isAuthenticated, true) }
        )
}

object Category {
    val categoryFeeder = csv("data/categories.csv").random()
    fun list() =
        feed(categoryFeeder)
            .exec(
                http("List Category")
                    .get("/api/category")
                    .check(jsonPath("$[?(@.id == #{categoryId})].name").isEL("#{categoryName}"))
            )

    fun update() =
        feed(categoryFeeder)
            .exec(Auth.authenticate())
            .exec(
                http("Update Category")
                    .put("/api/category/#{categoryId}")
                    .headers(authHeader)
                    .body(StringBody("{\"name\": \"#{categoryName}\"}"))
                    .check(jsonPath("$.name").isEL("#{categoryName}"))
            )
}

object Product {
    private val productFeeder = csv("data/products.csv").circular()

    fun listAll() = exec(
        http("List All Products").get("/api/product").check(jmesPath("[*]").ofList().saveAs("allProducts"))
    )

    fun list() =
        feed(Category.categoryFeeder)
            .exec(
                http("List Products")
                    .get("/api/product?category=#{categoryId}")
                    .check(jmesPath("[*].id").ofList().saveAs("allProductsIds"))
            )

    fun get() =
        exec { session ->
            val allProducts = session.getList<Int>("allProductsIds")
            return@exec session.set("productId", allProducts[Random.nextInt(allProducts.size)])
        }.exec { session ->
            println("All product ids: ${session.getString("allProductsIds")}")
            println("Selected product id: ${session.getString("productId")}")
            return@exec session
        }
            .exec(
                http("Get Product")
                    .get("/api/product/#{productId}")
                    .check(jmesPath("id").ofInt().isEL("#{productId}"))
                    .check(jmesPath("@").ofMap().saveAs("product"))
            )

    fun update() =
        exec(Auth.authenticate())
            .exec { session ->
                val product: Map<String, Any> = session.getMap("product")

                return@exec session
                    .set("productCategoryId", product.get("categoryId"))
                    .set("productName", product.get("name"))
                    .set("productDescription", product.get("description"))
                    .set("productImage", product.get("image"))
                    .set("productPrice", product.get("price"))
                    .set("productId", product.get("id"))
            }
            .exec(
                http("Update Product #{productName}")
                    .put("/api/product/#{productId}")
                    .headers(authHeader)
                    .body(ElFileBody("create_product.json"))
                    .check(jsonPath("$.price").isEL("#{productPrice}"))
            )


    fun create() = exec(Auth.authenticate())
        .repeat(4, "count").on(
            feed(productFeeder)
                .exec(
                    http("Create Product #{productName}")
                        .post("/api/product")
                        .headers(authHeader)
                        .body(ElFileBody("create_product.json"))
                        .check(jsonPath("$.price").isEL("#{productPrice}"))
                )
        )
}

object UserJourneys {
    val minPause = Duration.ofMinutes(200)
    val maxPause = Duration.ofMinutes(3000)
    val admin = exec(
        initSession(),
        Category.list(),
        pause(minPause, maxPause),
        Product.list(),
        pause(minPause, maxPause),
        Product.get(),
        pause(minPause, maxPause),
        Product.update(),
        pause(minPause, maxPause),
        Product.create(),
        pause(minPause, maxPause),
        Category.update()
    )

    val priceScraper = exec(
        Category.list(),
        pause(1),
        Product.listAll()
    )

    val priceUpdated = exec(initSession())
        .exec(Product.listAll())
        .repeat("#{allProducts.size()}", "productIndex").on(
            exec { session ->
                val index = session.getInt("productIndex")
                val allProducts = session.getList<Any>("allProducts")
                return@exec session.set("product", allProducts[index])
            }.exec(Product.update())
        )
}

object Scenarios {
    val defaultScenario = scenario("DefaultLoadTest")
        .during(Duration.ofSeconds(60))
        .on(
            randomSwitch().on(
                Choice.WithWeight(20.0, exec(UserJourneys.admin)),
                Choice.WithWeight(40.0, exec(UserJourneys.priceUpdated)),
//                Choice.WithWeight(40.0, exec(UserJourneys.priceScraper))
            )
        )
    val noAdmin = scenario("NoAdminLoadTest")
        .during(Duration.ofSeconds(60))
        .on(
            randomSwitch().on(
                Choice.WithWeight(60.0, exec(UserJourneys.priceUpdated)),
//                Choice.WithWeight(40.0, exec(UserJourneys.priceScraper))
            )
        )
}

class DemoStoreSimulation : Simulation() {
    //
    private val httpProtocol = http
        .baseUrl("https://demostore.gatling.io")
        .contentTypeHeader("application/json")
        .acceptHeader("application/json")
//
//    private val scn = scenario("DemoStoreSimulation")

    init {
        setUp(
            Scenarios.defaultScenario.injectClosed(
                rampConcurrentUsers(1).to(5).during(Duration.ofSeconds(30)),
                constantConcurrentUsers(5).during(Duration.ofSeconds(60))
            ).protocols(httpProtocol),
            Scenarios.noAdmin.injectClosed(
                rampConcurrentUsers(1).to(5).during(Duration.ofSeconds(30)),
                constantConcurrentUsers(5).during(Duration.ofSeconds(60))
            ).protocols(httpProtocol)
        )
//        setUp(
//            scn.injectClosed(
//                rampConcurrentUsers(1).to(50).during(Duration.ofSeconds(20)),
//                constantConcurrentUsers(100).during(Duration.ofSeconds(5))
//            ).protocols(httpProtocol)
//        )
    }

}
