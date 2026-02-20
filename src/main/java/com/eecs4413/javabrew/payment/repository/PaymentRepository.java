package com.eecs4413.javabrew.payment.repository;

import com.eecs4413.javabrew.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}