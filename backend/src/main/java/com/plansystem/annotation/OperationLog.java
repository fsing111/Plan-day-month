package com.plansystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic operation logging via AOP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** Operation type: CREATE, UPDATE, DELETE, SUBMIT, WITHDRAW, APPROVE, REJECT */
    String operation();

    /** Target type: PLAN, ACHIEVEMENT, APPROVAL */
    String targetType();
}
