package com.example.gateway

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.timelimiter.TimeLimiter
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.function.Supplier

class GatewayPolicy(
    private val name: String,
    timeoutSeconds: Long = 3,
    maxThreadPoolSize: Int = 8,
    queueCapacity: Int = 10,
    maxRetryAttempts: Int = 3
) {
    private val circuitBreaker = CircuitBreaker.of(
        name,
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .slowCallRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slowCallDurationThreshold(Duration.ofSeconds(timeoutSeconds))
            .permittedNumberOfCallsInHalfOpenState(2)
            .minimumNumberOfCalls(2)
            .build()
    )

    private val bulkhead = ThreadPoolBulkhead.of(
        name,
        ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(maxThreadPoolSize)
            .coreThreadPoolSize(maxThreadPoolSize / 2)
            .queueCapacity(queueCapacity)
            .build()
    )

    private val timeLimiter = TimeLimiter.of(
        name,
        TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(timeoutSeconds))
            .cancelRunningFuture(true)
            .build()
    )

    private val retry = Retry.of(
        name,
        RetryConfig.custom<Any?>()
            .maxAttempts(maxRetryAttempts)
            .waitDuration(Duration.ofMillis(200))
            .retryExceptions(TransientGatewayException::class.java)
            .build()
    )

    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(maxThreadPoolSize)

    fun <T> execute(isIdempotent: Boolean, action: () -> T): T {
        val actionCallable: Callable<T> = Callable { action() }
        val futureSupplier: Supplier<Future<T>> = Supplier {
            executor.submit(actionCallable)
        }
        val timeLimited: Callable<Future<T>> = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier)
        val wrappedCallable: Callable<T> = Callable {
            timeLimited.call().get()
        }

        val circuitBreakerCallable = CircuitBreaker.decorateCallable(circuitBreaker, wrappedCallable)
        val bulkheadCallable = ThreadPoolBulkhead.decorateCallable(bulkhead, circuitBreakerCallable)
        val finalCallable = if (isIdempotent) {
            Retry.decorateCallable(retry, bulkheadCallable)
        } else {
            bulkheadCallable
        }

        try {
            return finalCallable.call()
        } catch (throwable: Throwable) {
            throw unwrapException(throwable)
        }
    }

    private fun unwrapException(throwable: Throwable): Throwable {
        return when (throwable) {
            is java.util.concurrent.ExecutionException -> throwable.cause ?: throwable
            is java.util.concurrent.CompletionException -> throwable.cause ?: throwable
            else -> throwable
        }
    }
}

class GatewayUnavailableException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class TransientGatewayException(message: String) : RuntimeException(message)
