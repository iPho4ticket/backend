package com.ipho4ticket.seatservice.application.aspect;

import com.ipho4ticket.seatservice.application.config.ConcurrencyControl;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Description("AOP 로직 구현")
public class ConcurrencyAspect {
    private final RedissonClient redissonClient;
    private final TransactionAspect transactionAspect;

    @Around("@annotation(com.ipho4ticket.seatservice.application.config.ConcurrencyControl) && args(targetId)")
    public Object around(ProceedingJoinPoint joinPoint, Long targetId) throws Throwable {
        Object result;

        // Get annotation
        ConcurrencyControl annotation = getAnnotation(joinPoint);

        // Get lock name and acquire lock
        String lockName = getLockName(targetId, annotation);
        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean available = lock.tryLock(annotation.waitTime(), annotation.leaseTime(),
                    annotation.timeUnit());

            if (!available) {
                log.warn("Redisson GetLock Timeout {}", lockName);
                throw new IllegalArgumentException();
            }

            log.info("Redisson GetLock {}", lockName);

            // Proceed with the original method execution
            return transactionAspect.proceed(joinPoint);
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.warn("Redisson Lock Already UnLock {}", lockName);
            }
        }
    }

    private ConcurrencyControl getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(ConcurrencyControl.class);
    }

    private String getLockName(Long targetId, ConcurrencyControl annotation) {
        String lockNameFormat = "lock:%s:%s";
        String relevantParameter = targetId.toString();
        return String.format(lockNameFormat, annotation.lockName(), relevantParameter);
    }
}
