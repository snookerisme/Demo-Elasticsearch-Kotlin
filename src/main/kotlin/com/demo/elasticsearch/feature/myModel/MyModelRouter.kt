package com.demo.elasticsearch.feature.myModel

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class MyModelRouter {

    @Bean
    fun myModelRouters(myModelHandler: MyModelHandler) = coRouter {
        GET("/index", myModelHandler::index)
        accept(MediaType.APPLICATION_JSON).nest {
            POST("/save", myModelHandler::save)
            POST("/findById", myModelHandler::findById)
            POST("/findAll", myModelHandler::findAll)
            POST("/delete", myModelHandler::delete)
        }
    }
}