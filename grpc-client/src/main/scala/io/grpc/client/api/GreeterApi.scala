package io.grpc.client.api
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, _}
import com.google.inject.Inject
import io.grpc.common.util.LoggerUtil
import io.schemas.{GreeterService, GreeterServiceClient, HelloRequest}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.scaladsl.Source
import io.grpc.client.common.JsonSupport

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class GreeterApi @Inject() (implicit
    classicActorSystem: ClassicActorSystem,
    ec: ExecutionContext,
    greeterService: GreeterServiceClient,

) extends LoggerUtil with JsonSupport {

  val routes = pathPrefix("greeter") {
    concat(
      path("sayHello") {
        onComplete(greeterService.sayHello(HelloRequest("WM"))) {
          case Success(r) =>
            success(r.message)
          case Failure(e) =>
            logger.error(s"sayHello failed:$e")
            complete(StatusCodes.OK, "sayHello failed")
        }
      },
      path("itKeepsTalking") {
        val requests = List("WM", "HY", "FF").map(HelloRequest(_))
        onComplete(greeterService.itKeepsTalking(Source(requests))) {
          case Success(r) =>
            logger.info(s"got single reply for streaming requests: $r")
            complete(StatusCodes.OK, r.message)
          case Failure(e) =>
            logger.error(s"sayHello failed:$e")
            complete(StatusCodes.OK, "sayHello failed")
        }
      },
      path("itKeepsReplying") {
        val responseStream = greeterService.itKeepsReplying(HelloRequest("WM"))
        responseStream.runForeach(reply => logger.info(s"got streaming reply: ${reply.message}")) onComplete {
          case Success(_) =>
            logger.info("streamingReply done")
          case Failure(e) =>
            logger.info(s"Error streamingReply: $e")
        }
        complete(StatusCodes.OK, "OK")
      },
      path("streamHellos") {
        val requests = List("WM", "HY", "FF").map(HelloRequest(_))
        val responseStream = greeterService.streamHellos(Source(requests))
        responseStream.runForeach(reply => logger.info(s"got streaming reply: ${reply.message}")) onComplete {
          case Success(_) =>
            logger.info("streamingReply done")
          case Failure(e) =>
            logger.info(s"Error streamingReply: $e")
        }
        complete(StatusCodes.OK, "OK")
      }
    )
  }

}
