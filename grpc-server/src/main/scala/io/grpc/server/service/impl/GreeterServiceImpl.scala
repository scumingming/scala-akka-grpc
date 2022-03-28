package io.grpc.server.service.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.google.inject.Inject
import com.google.protobuf.timestamp.Timestamp
import io.grpc.common.util.LoggerUtil
import io.schemas.{GreeterService, HelloReply, HelloRequest}

import scala.concurrent.{ExecutionContext, Future}

case class GreeterServiceImpl @Inject() (implicit ec: ExecutionContext, system: ActorSystem)
    extends GreeterService
    with LoggerUtil {
  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    logger.info(s"sayHello $in")
    Future.successful[HelloReply](
      HelloReply(
        message = s"reply:: sayHello ${in.name}, my name is grpc-server.",
        timestamp = Some(Timestamp.apply(System.currentTimeMillis(), 0))
      )
    )
  }

  override def itKeepsTalking(
      in: Source[HelloRequest, NotUsed]
  ): Future[HelloReply] = {
    in.runWith(Sink.seq)
      .map { elements =>
        logger.info(s"itKeepsTalking $elements")
        HelloReply(
          message = s"reply:: itKeepsTalking ${elements.map(_.name)}, my name is grpc-server.",
          timestamp = Some(Timestamp.apply(System.currentTimeMillis(), 0))
        )
      }
  }

  override def itKeepsReplying(in: HelloRequest): Source[HelloReply, NotUsed] = {
    logger.info(s"itKeepsReplying $in")
    Source(s"reply:: itKeepsReplying ${in.name}".toList).map(char => HelloReply(char.toString))
  }

  override def streamHellos(
      in: Source[HelloRequest, NotUsed]
  ): Source[HelloReply, NotUsed] =
    in.map(request => HelloReply(s"reply:: streamHellos ${request.name}"))

}
