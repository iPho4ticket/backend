package com.ipho4ticket.paymentservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    KAKAO_PAY("KakaoPayService");

    private final String processorName;
}
