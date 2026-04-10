package com.koini.api.dto.response;

public record MerchantKycApplicationResponse(
    String userId,
    String status,
    MerchantKycDetail merchantDetail,
    MerchantKycDocuments merchantDocuments
) {

  public record MerchantKycDetail(
      String businessName,
      String tradingName,
      String addressLine1,
      String city,
      String country,
      String idNumber,
      String submittedAt
  ) {
  }

  public record MerchantKycDocuments(
      String idDocumentUrl,
      String proofOfAddressUrl
  ) {
  }
}

