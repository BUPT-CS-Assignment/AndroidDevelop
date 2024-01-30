package com.labx.scanimal.api

data class ErrorResponse(
    val error_code: Int,
    val error_msg: String
)

data class SearchResultResponse(
    val log_id: String,
    val result:List<Result>
)

data class Result(
    val name: String,
    val score: Double,
    val baike_info: DetailInfo? = null
)

data class DetailInfo(
    val baike_url: String,
    val description: String? = null
)


data class ObjectSearchResult(
    val name: String,
    val score: Double,
    var url: String? = null,
    val description: String? = null
)