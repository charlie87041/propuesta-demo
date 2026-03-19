package com.cookiesstore.common.authorization.evaluator;

import com.cookiesstore.common.authorization.annotation.RequiresAbility;
import com.cookiesstore.common.authorization.annotation.RequiresPermission;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

@Aspect
@Component
public class AuthorizationAspect {

    private final DomainAuthorizationEvaluator evaluator;

    public AuthorizationAspect(DomainAuthorizationEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Around("@annotation(requiresPermission)")
    public Object enforcePermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String domainCode = extractDomainCode(joinPoint);

        if (!evaluator.hasPermission(authentication, domainCode, requiresPermission.value())) {
            throw new AccessDeniedException("Permission denied");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requiresAbility)")
    public Object enforceAbility(ProceedingJoinPoint joinPoint, RequiresAbility requiresAbility) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String domainCode = extractDomainCode(joinPoint);

        if (!evaluator.hasAbility(authentication, domainCode, requiresAbility.value())) {
            throw new AccessDeniedException("Ability denied");
        }

        return joinPoint.proceed();
    }

    private String extractDomainCode(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

            if (pathVariable != null) {
                String name = pathVariable.name();
                if (!StringUtils.hasText(name)) {
                    name = pathVariable.value();
                }
                if (!StringUtils.hasText(name)) {
                    name = parameter.getName();
                }

                if ("domainCode".equals(name) && args[i] instanceof String domainCode) {
                    return domainCode;
                }
            }

            if ("domainCode".equals(parameter.getName()) && args[i] instanceof String domainCode) {
                return domainCode;
            }
        }

        throw new AccessDeniedException("domainCode path variable is required");
    }
}
