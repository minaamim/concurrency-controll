package mina.concurrencycontroll

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication
class ConcurrencyControllApplicationTests {

    fun main(args: Array<String>) {
        runApplication<ConcurrencyControllApplicationTests>(*args)
    }

}
