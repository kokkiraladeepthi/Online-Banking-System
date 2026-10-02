package com.bank.mvp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bank.mvp.model.SavingsGoal;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<SavingsGoal> findByAccountId(Long accountId);
}

