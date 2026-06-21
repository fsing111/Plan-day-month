package com.plansystem.aspect;

import com.plansystem.annotation.OperationLog;
import com.plansystem.mapper.OperationLogMapper;
import com.plansystem.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * AOP aspect for automatic operation logging.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper logMapper;

    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            com.plansystem.entity.OperationLog entry = new com.plansystem.entity.OperationLog();
            entry.setUserId(UserContext.getUserId());
            entry.setUsername(UserContext.getUsername());
            entry.setOperation(operationLog.operation());
            entry.setTargetType(operationLog.targetType());
            entry.setCreatedAt(LocalDateTime.now());

            // Try to extract target ID from first method argument (usually Long id)
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                entry.setTargetId((Long) args[0]);
            }

            // Extract method summary
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            entry.setSummary(signature.getMethod().getName());

            // Extract IP
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
                entry.setIpAddress(ip);
            }

            logMapper.insert(entry);
        } catch (Exception e) {
            log.warn("Failed to record operation log: {}", e.getMessage());
        }

        return result;
    }
}
