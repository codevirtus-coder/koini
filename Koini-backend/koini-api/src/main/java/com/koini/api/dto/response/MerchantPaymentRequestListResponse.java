package com.koini.api.dto.response;

import java.util.List;

public record MerchantPaymentRequestListResponse(
    List<MerchantPaymentRequestResponse> requests,
    int page,
    int size,
    long total
) {
}

