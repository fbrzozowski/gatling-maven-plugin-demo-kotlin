package com.gearsofleo.platform

import io.gatling.javaapi.core.CoreDsl.RawFileBody
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.doIf
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.pause
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

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
    fun list() = http("List Category")
        .get("/api/category")
        .check(jsonPath("$[?(@.id == 6)].name").`is`("For Her"))

    fun update() =
        exec(Auth.authenticate())
            .exec(
                http("Update Category")
                    .put("/api/category/7")
                    .headers(authHeader)
                    .body(RawFileBody("0008_request.json"))
                    .check(jsonPath("$.name").`is`("Everyone"))
            )
}

object Product {
    fun list() = http("List Products")
        .get("/api/product?category=7")

    fun update() =
        exec(Auth.authenticate())
            .exec(
                http("Update Product")
                    .put("/api/product/17")
                    .headers(authHeader)
                    .body(RawFileBody("0004_request.json"))
                    .check(jsonPath("$.price").`is`("15.99"))
            )

    fun get() = http("Get Product")
        .get("/api/product/33")

    fun create() =
        exec(Auth.authenticate())
            .repeat(3, "count").on(
                http("Create Product #{count}")
                    .post("/api/product")
                    .headers(authHeader)
                    .body(RawFileBody("create_product_#{count}.json"))
            )
}

class DemoStoreSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("https://demostore.gatling.io")
        .contentTypeHeader("application/json")
        .acceptHeader("application/json")

    private val scn = scenario("DemoStoreSimulation")
        .exec(
            initSession(),
            pause(1),
            Category.list(),
            pause(1),
            Product.list(),
            pause(1),
            Product.get(),
            pause(1),
            Product.update(),
            pause(1),
            Product.create(),
            pause(1),
            Category.update()
        )

    init {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)
    }
}
