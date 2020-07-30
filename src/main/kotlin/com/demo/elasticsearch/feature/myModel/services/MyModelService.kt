package com.demo.elasticsearch.feature.myModel.services

import com.demo.elasticsearch.constant.ApplicationConstant.DEFAULT_ES_DOC_TYPE
import com.demo.elasticsearch.constant.ApplicationConstant.MYMODEL_ES_INDEX
import com.demo.elasticsearch.model.MyModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct


@Service
class MyModelService(
    private val reactiveElasticsearchOperations: ReactiveElasticsearchOperations,
    private val reactiveElasticsearchClient: ReactiveElasticsearchClient
) {
    private val logger: Logger = LoggerFactory.getLogger(MyModelService::class.java)

    @PostConstruct
    private fun checkIndexExists() {
        val request = GetIndexRequest()
        request.indices(MYMODEL_ES_INDEX)
        reactiveElasticsearchClient.indices()
            .existsIndex(request)
            .doOnError({ throwable -> logger.error(throwable.message, throwable) })
            .flatMap({ indexExists ->
                logger.info("Index {} exists: {}", MYMODEL_ES_INDEX, indexExists)
                if (!indexExists)
                    return@flatMap createIndex()
                else
                    return@flatMap Mono.justOrEmpty("")
            })
            .block()
    }

    private fun createIndex(): Mono<Void?>? {
        val request = CreateIndexRequest()
        request.index(MYMODEL_ES_INDEX)
        request.mapping(
            DEFAULT_ES_DOC_TYPE,
            """{
                      "properties": {
                        "timestamp": {
                          "type": "date",
                          "format": "epoch_millis||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd"
                        }
                      }
                    }""",
            XContentType.JSON
        )
        return reactiveElasticsearchClient.indices()
            .createIndex(request)
            .doOnSuccess({ aVoid -> logger.info("Created Index {}", MYMODEL_ES_INDEX) })
            .doOnError({ throwable -> logger.error(throwable.message, throwable) })
    }

    suspend fun findMyModelById(id: String): MyModel {
        return reactiveElasticsearchOperations.get(
            id,
            MyModel::class.java,
            IndexCoordinates.of(MYMODEL_ES_INDEX)
        ).doOnError({ throwable -> logger.error(throwable.message, throwable) }).awaitFirstOrNull()
            ?: throw Exception("Data not found")
    }

    suspend fun findAllMyModels(field: String, value: String): MutableList<Any> {
        val query = NativeSearchQueryBuilder()
        if (!StringUtils.isEmpty(field) && !StringUtils.isEmpty(value)) {
            query.withQuery(QueryBuilders.matchQuery(field, value))
        }

        return reactiveElasticsearchOperations.search(
            query.build(),
            MyModel::class.java,
            IndexCoordinates.of(MYMODEL_ES_INDEX)
        )
            .map { obj: SearchHit<*> -> obj.content }
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .doOnError { throwable -> logger.error(throwable.message, throwable) }
            .collectList().awaitSingle()
            ?: throw Exception("Data not found")
    }

    suspend fun saveMyModel(myModel: MyModel): MyModel {
        return reactiveElasticsearchOperations.save(
            myModel,
            IndexCoordinates.of(MYMODEL_ES_INDEX)
        ).doOnError { throwable -> logger.error(throwable.message, throwable) }.awaitFirstOrNull()
            ?: throw Exception("Data not found")
    }

    suspend fun deleteMyModelById(id: String): String {
        return reactiveElasticsearchOperations.delete(
            id,
            IndexCoordinates.of(MYMODEL_ES_INDEX)
        ).doOnError { throwable: Throwable ->
            logger.error(
                throwable.message,
                throwable
            )
        }.awaitFirstOrNull()
            ?: throw Exception("Data not found")
    }
}