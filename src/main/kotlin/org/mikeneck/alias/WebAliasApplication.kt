package org.mikeneck.alias

import org.mikeneck.alias.infra.IdGeneratorImpl
import org.mikeneck.alias.model.IdGenerator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.codec.json.Jackson2JsonEncoder

@SpringBootApplication
class WebAliasApplication {

	@Bean
	fun idGenerator(): IdGenerator = IdGeneratorImpl(2)

	@Bean
	fun jackson2JsonEncoder(): Jackson2JsonEncoder = Jackson2JsonEncoder()
}

fun main(args: Array<String>) {
	runApplication<WebAliasApplication>(*args)
}
