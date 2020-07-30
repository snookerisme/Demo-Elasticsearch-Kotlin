package com.demo.elasticsearch.feature.myModel

import com.demo.elasticsearch.feature.myModel.models.DeleteByIdRequest
import com.demo.elasticsearch.feature.myModel.models.FindAllRequest
import com.demo.elasticsearch.feature.myModel.models.FindByIdRequest
import com.demo.elasticsearch.feature.myModel.services.MyModelService
import com.demo.elasticsearch.model.MyModel
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class MyModelHandler(
    private val myModelService: MyModelService
) {
    suspend fun index(request: ServerRequest) =
        ServerResponse
            .ok()
            .json()
            .bodyValueAndAwait(
                "Hello world!!"
            )

    suspend fun save(request: ServerRequest) =
        request
            .awaitBody<MyModel>()
            .let {
                ServerResponse
                    .ok()
                    .bodyValueAndAwait(
                        myModelService.saveMyModel(it)
                    )
            }

    suspend fun findById(request: ServerRequest) =
        request
            .awaitBody<FindByIdRequest>()
            .let {
                ServerResponse
                    .ok()
                    .bodyValueAndAwait(
                        myModelService.findMyModelById(it.id)
                    )
            }

    suspend fun delete(request: ServerRequest) =
        request
            .awaitBody<DeleteByIdRequest>()
            .let {
                ServerResponse
                    .ok()
                    .bodyValueAndAwait(
                        myModelService.deleteMyModelById(it.id)
                    )
            }

    suspend fun findAll(request: ServerRequest) =
        request
            .awaitBody<FindAllRequest>()
            .let {
                ServerResponse
                    .ok()
                    .bodyValueAndAwait(
                        myModelService.findAllMyModels(it.field,it.value)
                    )
            }
}