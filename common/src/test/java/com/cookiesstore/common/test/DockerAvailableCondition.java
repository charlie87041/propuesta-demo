package com.cookiesstore.common.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 condition that checks if Docker is available.
 * Tests will be skipped when Docker is not available.
 */
public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            DockerClientFactory.instance().client();
            return ConditionEvaluationResult.enabled("Docker is available");
        } catch (Throwable t) {
            return ConditionEvaluationResult.disabled("Docker is not available: " + t.getMessage());
        }
    }
}
