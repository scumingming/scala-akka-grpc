package io.grpc.server.service.impl

import com.google.inject.Inject
import io.grpc.common.util.LoggerUtil
import io.schemas.{AuthRequest, AuthResponse, AuthService}

import scala.concurrent.Future

case class AuthServiceImpl @Inject() () extends AuthService with LoggerUtil {
  override def auth(in: AuthRequest): Future[AuthResponse] = {
    logger.info(s"auth ${in.userId}")
    if (in.userId == "123456") Future.successful(AuthResponse(code = 0, msg = "success"))
    else Future.successful(AuthResponse(code = -1, msg = "failed"))
  }
}
