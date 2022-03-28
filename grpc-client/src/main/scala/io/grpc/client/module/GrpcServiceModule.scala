package io.grpc.client.module

import akka.actor.typed.scaladsl.ActorContext
import akka.grpc.GrpcClientSettings
import com.google.inject.{AbstractModule, Provides, Singleton}
import io.grpc.common.util.LoggerUtil
import io.schemas.{GreeterService, GreeterServiceClient}

case class GrpcServiceModule(context: ActorContext[_]) extends AbstractModule with LoggerUtil {

  implicit val classicActorSystem = context.system.classicSystem

  @Singleton
  @Provides
  def greeterService: GreeterServiceClient = {
    val settings = GrpcClientSettings.fromConfig(GreeterService.name)
    GreeterServiceClient(settings)
//    val clientSettings         = GrpcClientSettings.connectToServiceAt("0.0.0.0", 8080).withTls(false)
//    GreeterServiceClient(clientSettings)
  }

}
