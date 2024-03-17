package mina.concurrencycontroll.global.redis

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig() {
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config().apply {
            useSingleServer().address = "redis://127.0.0.01:6379"
        }
        return Redisson.create(config)
    }
}