package com.bank.mvp.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransferRequest;
import com.bank.mvp.service.AccountService;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
class LoggingAspectTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoggingAspect loggingAspect;

    @Test
    void loggingAspectShouldBeInjected() {
        assertNotNull(loggingAspect);
    }

    @Test
    void shouldLogOperationStartAndCompleteForServiceCalls() {
        CreateAccountRequest req1 = new CreateAccountRequest();
        req1.setName("Aspect User 1");
        req1.setEmail("aspect1@example.com");
        req1.setInitialBalance(new BigDecimal("1000.00"));
        var acc1 = accountService.createAccount(req1);

        CreateAccountRequest req2 = new CreateAccountRequest();
        req2.setName("Aspect User 2");
        req2.setEmail("aspect2@example.com");
        req2.setInitialBalance(new BigDecimal("500.00"));
        var acc2 = accountService.createAccount(req2);

        MoneyRequest depReq = new MoneyRequest();
        depReq.setAmount(new BigDecimal("200.00"));
        accountService.deposit(acc1.getId(), depReq);

        MoneyRequest wdReq = new MoneyRequest();
        wdReq.setAmount(new BigDecimal("100.00"));
        accountService.withdraw(acc1.getId(), wdReq);

        TransferRequest tfReq = new TransferRequest(acc1.getId(), acc2.getId(), new BigDecimal("250.00"), "Aspect test transfer");
        var tfRes = accountService.transferMoney(tfReq);

        assertEquals("SUCCESS", tfRes.getStatus());
        assertEquals(new BigDecimal("850.00"), tfRes.getSenderBalance());
    }

    @Test
    void shouldLogOperationFailedWhenServiceThrowsException() {
        MoneyRequest wdReq = new MoneyRequest();
        wdReq.setAmount(new BigDecimal("999999.00"));

        assertThrows(RuntimeException.class, () -> accountService.withdraw(1L, wdReq));
    }
}
