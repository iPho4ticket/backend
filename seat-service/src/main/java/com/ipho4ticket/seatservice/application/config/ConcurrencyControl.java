package com.ipho4ticket.seatservice.application.config;

import jdk.jfr.Description;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD) // 메서드에서만 적용할 수 있도록 지정
@Retention(RetentionPolicy.RUNTIME) // AOP나 리플렉션을 사용하여 런타임 시에 락을 적용할 수 있도록 지정
@Description("메서드에 분산 락을 적용하기 위해 정의된 커스텀 어노테이션")
public @interface ConcurrencyControl {
    /**
     * The name of the target resource to acquire a lock on.
     */
    String lockName();

    /**
     * The maximum time to wait for the lock to be available, in the specified time unit.
     */
    long waitTime() default 1L;

    /**
     * The duration to hold the lock for, in the specified time unit.
     */
    long leaseTime() default 10L;

    /**
     * The time unit for the waitTime and leaseTime values.
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
