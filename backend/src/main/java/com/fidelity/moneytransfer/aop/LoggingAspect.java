package com.fidelity.moneytransfer.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.fidelity.moneytransfer.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        // Method entry
        logger.info("Executing method: {}", joinPoint.getSignature());

        // Log arguments (debug level to avoid clutter in prod)
        if (joinPoint.getArgs().length > 0) {
            logger.debug("Arguments: {}", Arrays.toString(joinPoint.getArgs()));
        }

        Object result;
        try {
            result = joinPoint.proceed(); // execute target method
        } catch (Exception ex) {
            logger.error("Exception in method: {} | Message: {}",
                    joinPoint.getSignature(), ex.getMessage());
            throw ex;
        }

        long duration = System.currentTimeMillis() - start;

        // Method exit
        logger.info("{} executed in {} ms",
                joinPoint.getSignature(), duration);

        // Log result only at debug level
        logger.debug("Returned value: {}", result);

        return result;
    }
}
