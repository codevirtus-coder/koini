package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record TransactionHistoryFilter(
    LocalDate dateFrom,
    LocalDate dateTo,
    String type,
    String status,
    int page,
    int size
) {
}
