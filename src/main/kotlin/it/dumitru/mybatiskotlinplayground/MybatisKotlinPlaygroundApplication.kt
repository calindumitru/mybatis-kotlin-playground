package it.dumitru.mybatiskotlinplayground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MybatisKotlinPlaygroundApplication

fun main(args: Array<String>) {
    runApplication<MybatisKotlinPlaygroundApplication>(*args)
}
