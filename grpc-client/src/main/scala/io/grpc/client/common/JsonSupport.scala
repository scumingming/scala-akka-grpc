package io.grpc.client.common

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjackson.JacksonSupport

case class ResponseData[T](code: Int, data: T)
trait JsonSupport extends JacksonSupport {

  def success[T](data: T): Route =
    complete(ResponseData(code = 0, data = data))

  def failed[T](code: Int, data: T): Route =
    complete(ResponseData(code = code, data = data))

//  def paginated[T](page: Int, pageSize: Int, data: Seq[T]): Route =
//    complete(
//      ResponseData(
//        code = 0,
//        PaginatedData(
//          list = data,
//          pagination = Pagination(
//            page = page,
//            pageSize = pageSize,
//            hasNext = data.size > pageSize,
//            limit = 0,
//            totalSize = 0
//          )
//        )
//      )
//    )

}
