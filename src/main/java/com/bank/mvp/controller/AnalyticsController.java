package com.bank.mvp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.mvp.dto.AccountAnalyticsResponse;
import com.bank.mvp.service.AnalyticsService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<AccountAnalyticsResponse> getAccountAnalytics(@PathVariable Long accountId) {
        AccountAnalyticsResponse analytics = analyticsService.getAccountAnalytics(accountId);
        return ResponseEntity.ok(analytics);
    }
}
