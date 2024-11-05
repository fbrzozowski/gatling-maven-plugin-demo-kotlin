package com.gearsofleo.platform

import java.time.Duration

import io.gatling.javaapi.core.*
import io.gatling.javaapi.http.*
import io.gatling.javaapi.jdbc.*

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import io.gatling.javaapi.jdbc.JdbcDsl.*

class DemoStoreSimulation : Simulation() {

  private val httpProtocol = http
    .baseUrl("https://demostore.gatling.io")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")

  private val authHeader = mapOf(
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )


  private val scn = scenario("DemoStoreSimulation")
    .exec(
      http("List Category")
        .get("/api/category"),
      pause(1),
      http("List Products")
        .get("/api/product?category=7"),
      pause(1),
      http("Get Product")
        .get("/api/product/33"),
      pause(1),
      http("Authenticate")
        .post("/api/authenticate")
        .body(RawFileBody("0003_request.json")),
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
      http("Update Category")
        .put("/api/category/7")
        .headers(authHeader)
        .body(RawFileBody("0008_request.json"))
    )

  init {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)
  }
}
