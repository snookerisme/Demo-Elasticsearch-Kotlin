package com.demo.elasticsearch.feature.myModel.models

data class FindByIdRequest(
    val id: String
)

data class DeleteByIdRequest(
    val id: String
)

data class FindAllRequest(
    val field: String,
    val value: String
)
