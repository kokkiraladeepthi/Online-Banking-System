package com.bank.mvp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // 1. User Registration & Login
    @Pointcut("execution(* com.bank.mvp.service.UserService.registerUser(..))")
    public void registrationOperation() {}

    @Pointcut("execution(* com.bank.mvp.service.UserService.loginUser(..))")
    public void loginOperation() {}

    // 2. Account Creation, Deposit, Withdrawal, Fund Transfer
    @Pointcut("execution(* com.bank.mvp.service.AccountService.createAccount(..))")
    public void accountCreationOperation() {}

    @Pointcut("execution(* com.bank.mvp.service.AccountService.deposit(..))")
    public void depositOperation() {}

    @Pointcut("execution(* com.bank.mvp.service.AccountService.withdraw(..))")
    public void withdrawalOperation() {}

    @Pointcut("execution(* com.bank.mvp.service.AccountService.transferMoney(..))")
    public void fundTransferOperation() {}

    // 3. Savings Goal Operations (current & future)
    @Pointcut("execution(* com.bank.mvp.service..*SavingsGoal*.*(..))")
    public void savingsGoalOperation() {}

    // 4. Admin Operations (current & future)
    @Pointcut("execution(* com.bank.mvp.service..*Admin*.*(..))")
    public void adminOperation() {}

    // Combined Pointcut for all important operations
    @Pointcut("registrationOperation() || loginOperation() || accountCreationOperation() || " +
              "depositOperation() || withdrawalOperation() || fundTransferOperation() || " +
              "savingsGoalOperation() || adminOperation()")
    public void importantServiceOperations() {}

    @Around("importantServiceOperations()")
    public Object logImportantOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String operationName = formatOperationName(methodName);

        log.info("[AOP] Operation started: {}", operationName);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            log.info("[AOP] Operation completed: {} (Execution time: {} ms)", operationName, executionTime);
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("[AOP] Operation failed: {} (Execution time: {} ms). Reason: {}",
                    operationName, executionTime, ex.getMessage());
            throw ex;
        }
    }

    private String formatOperationName(String methodName) {
        return switch (methodName) {
            case "registerUser" -> "User Registration";
            case "loginUser" -> "User Login";
            case "createAccount" -> "Account Creation";
            case "deposit" -> "Account Deposit";
            case "withdraw" -> "Account Withdrawal";
            case "transferMoney" -> "Fund Transfer";
            default -> {
                if (methodName.toLowerCase().contains("goal")) {
                    yield "Savings Goal Operation (" + methodName + ")";
                } else if (methodName.toLowerCase().contains("admin")) {
                    yield "Admin Operation (" + methodName + ")";
                }
                yield methodName;
            }
        };
    }
}
