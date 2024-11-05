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
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.42.0")
  
  private val headers_0 = mapOf("Postman-Token" to "074dea53-0534-4c03-80fe-b53d2ddb9f87")
  
  private val headers_1 = mapOf("Postman-Token" to "a11676c4-e5a5-41ef-9d12-04427bbba511")
  
  private val headers_2 = mapOf("Postman-Token" to "99677b9e-5051-436b-837b-ecc5ed1dd5f9")
  
  private val headers_3 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "be9eecd8-6965-40ba-9bc2-f8b7e56ea178"
  )
  
  private val headers_4 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "9be80f89-09be-4b7d-a4f3-e8cca4df4e7b",
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )
  
  private val headers_5 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "398cae6a-3ef1-49e7-8dbd-986efd810b92",
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )
  
  private val headers_6 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "b7ff2609-f765-4f5d-a38b-9ceb6d7e097d",
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )
  
  private val headers_7 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "067261bf-8dc0-4596-8fbf-be2885b1b5b6",
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )
  
  private val headers_8 = mapOf(
    "Content-Type" to "application/json",
    "Postman-Token" to "0063ccf3-dad0-4b3f-b4f5-72408f7b5746",
    "authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDgyMjExMSwiZXhwIjoxNzMwODI1NzExfQ.iylhGp0OCbsvAzaqdiLANRmVTtynSFat-sKwebBD8kU"
  )


  private val scn = scenario("DemoStoreSimulation")
    .exec(
      http("request_0")
        .get("/api/category")
        .headers(headers_0),
      pause(1),
      http("request_1")
        .get("/api/product?category=7")
        .headers(headers_1),
      pause(1),
      http("request_2")
        .get("/api/product/33")
        .headers(headers_2),
      pause(1),
      http("request_3")
        .post("/api/authenticate")
        .headers(headers_3)
        .body(RawFileBody("0003_request.json")),
      pause(1),
      http("request_4")
        .put("/api/product/17")
        .headers(headers_4)
        .body(RawFileBody("0004_request.json")),
      pause(1),
      http("request_5")
        .post("/api/product")
        .headers(headers_5)
        .body(RawFileBody("0005_request.json")),
      pause(1),
      http("request_6")
        .post("/api/product")
        .headers(headers_6)
        .body(RawFileBody("0006_request.json")),
      pause(1),
      http("request_7")
        .post("/api/product")
        .headers(headers_7)
        .body(RawFileBody("0007_request.json")),
      pause(1),
      http("request_8")
        .put("/api/category/7")
        .headers(headers_8)
        .body(RawFileBody("0008_request.json"))
    )

  init {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)
  }
}
