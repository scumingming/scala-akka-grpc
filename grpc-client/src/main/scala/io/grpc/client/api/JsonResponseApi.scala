package io.grpc.client.api

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.scaladsl.model.StatusCodes
import com.google.inject.Inject
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import io.grpc.client.common.JsonSupport
//import io.circe.generic.auto._

case class JsonResponseApi @Inject()() extends JsonSupport {

  case class Person(name: String, age: Int, height: BigDecimal)

  val routes = pathPrefix("json") {
    path("fetch1") {
      val persion1 = Person("WM", 10, BigDecimal(18.88))
      success(persion1)
    }
  }

}
