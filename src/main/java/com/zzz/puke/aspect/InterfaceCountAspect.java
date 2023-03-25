package com.zzz.puke.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class InterfaceCountAspect {

    public static String METHOD_COUNT = "METHOD_COUNT:";

    @Autowired
    RedisTemplate redisTemplate;

    //    @Pointcut("execution(public * com.zzz.puke.controlloer.*.*(..))")
    @Pointcut("@annotation(com.zzz.puke.anno.InterfaceCount)")
    public void method() {

    }

    @Before("method()")
    public void getCount(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        RedisAtomicInteger redisAtomicInteger = new RedisAtomicInteger(METHOD_COUNT + methodName, redisTemplate.getConnectionFactory());
        redisAtomicInteger.incrementAndGet();
    }
}
