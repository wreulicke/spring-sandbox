package com.github.wreulicke.flexypool.demo

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

val log = LoggerFactory.getLogger(TestEndpoint::class.java)

@RestController
class TestEndpoint(val dataSource: DataSource) {


    @GetMapping("/")
    fun index(): String {
        dataSource.connection.use { connection ->
            val call = connection.prepareCall("SELECT * from mst_item limit 10")
            call.execute()
            val resultSet = call.resultSet
            while (resultSet.next()) {
                var string = resultSet.getString("item_id")
                log.info(string)
            }
            return "test"
        }
    }
}
