package com.greenko.alertservice.repository;

import com.greenko.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}