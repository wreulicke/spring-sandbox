package com.github.wreulicke.gclogger

import com.netflix.spectator.gc.GcLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import java.util.*
import kotlin.collections.ArrayList


@SpringBootApplication
class GcLoggerApplication{
    companion object {
        private val log:Logger = LoggerFactory.getLogger(GcLoggerApplication::class.java)
    }

    @EventListener
    fun onStartup(e: ApplicationReadyEvent) {
        log.info("started: {}", e)
        GcLogger().start(null)
    }

    // dirty endpoint
    @Bean
    fun routes() = router {
        GET("/test") {
            val list = ArrayList<String>()
            while (true){
                var uuid = UUID.randomUUID().toString()
                list.add(uuid);
                if (uuid == "") break
            }
            log.info(list.toString())
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(Flux.just("{\"message\":\"hello\"}"),String::class.java)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<GcLoggerApplication>(*args)
}

