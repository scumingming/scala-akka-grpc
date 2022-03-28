package io.grpc.client

import com.google.inject.Inject
import akka.http.scaladsl.server.Directives._
import io.grpc.client.api.{GreeterApi, JsonResponseApi}

case class ApiDependencyWiring @Inject() (
    greeterApi: GreeterApi,
    jsonResponseApi: JsonResponseApi,
) {

  val routes = pathPrefix("api") {
    concat(
      greeterApi.routes,
      jsonResponseApi.routes
    )
  }

}
