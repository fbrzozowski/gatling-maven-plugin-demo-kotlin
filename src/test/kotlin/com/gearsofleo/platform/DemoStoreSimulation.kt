package com.gearsofleo.platform

import io.gatling.javaapi.core.CoreDsl.RawFileBody
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
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

object Auth {
    fun authenticate() = exec(
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
    )
}

object Category {
    fun list() = http("List Category")
        .get("/api/category")
        .check(jsonPath("$[?(@.id == 6)].name").`is`("For Her"))

    fun update() =
        http("Update Category")
            .put("/api/category/7")
            .headers(authHeader)
            .body(RawFileBody("0008_request.json"))
            .check(jsonPath("$.name").`is`("Everyone"))
}

class DemoStoreSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("https://demostore.gatling.io")
        .contentTypeHeader("application/json")
        .acceptHeader("application/json")

    private val scn = scenario("DemoStoreSimulation")
        .exec(
            Category.list(),
            pause(1),
            http("List Products")
                .get("/api/product?category=7"),
            pause(1),
            http("Get Product")
                .get("/api/product/33"),
            pause(1),
            Auth.authenticate(),
            pause(1),
            http("Update Product")
                .put("/api/product/17")
                .headers(authHeader)
                .body(RawFileBody("0004_request.json")),
            pause(1),
            http("Create Product")
                .post("/api/product")
                .headers(authHeader)
                .body(RawFileBody("0005_request.json")),
            pause(1),
            http("Create Product 2")
                .post("/api/product")
                .headers(authHeader)
                .body(RawFileBody("0006_request.json")),
            pause(1),
            http("Create Product 3")
                .post("/api/product")
                .headers(authHeader)
                .body(RawFileBody("0007_request.json")),
            pause(1),
            Category.update()
        )

    init {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)
    }
}
