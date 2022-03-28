package io.grpc.client.common

case class Pagination(
    page: Int,
    pageSize: Int,
    totalSize: Int,
    limit: Int,
    hasNext: Boolean = false
)

case class PaginatedData[T](
    list: Seq[T],
    pagination: Pagination
)
