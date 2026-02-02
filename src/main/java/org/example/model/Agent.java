package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Agent(
        long personId,
        BigDecimal salary,
        LocalDate hireDate
) {}
