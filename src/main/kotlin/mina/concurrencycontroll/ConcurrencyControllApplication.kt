package mina.concurrencycontroll

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConcurrencyControllApplication

fun main(args: Array<String>) {
    runApplication<ConcurrencyControllApplication>(*args)
}
