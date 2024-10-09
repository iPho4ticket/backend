package com.ipho4ticket.paymentservice.infrastructure.external;

import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import com.ipho4ticket.paymentservice.domain.service.PaymentProcessor;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoPayService implements PaymentProcessor {

    @Value("${kakao.pay.secret-key}")
    private String kakaoPaySecretKey;

    // 카카오페이 결제 준비
    @Override
    public ReadyResponse payReady(String itemName, BigDecimal totalAmount, UUID ticket_id, UUID payment_id) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", "TC0ONETIME");                                    // 가맹점 코드
        parameters.put("partner_order_id", "1234567890");          // 주문번호
        parameters.put("partner_user_id", "userId");                            // 회원 아이디
        parameters.put("item_name", itemName);                                  // 상품명
        parameters.put("quantity", "1");                                        // 상품 수량
        parameters.put("total_amount", String.valueOf(totalAmount));            // 총 금액
        parameters.put("tax_free_amount", "0");                                 // 비과세 금액
        parameters.put("approval_url", "http://localhost:8080/api/v1/payments/approve?payment_id=" + payment_id + "&ticket_id=" + ticket_id); // 성공 시 URL
        parameters.put("cancel_url", "http://localhost:8080/order/pay/cancel");      // 취소 시 URL
        parameters.put("fail_url", "http://localhost:8080/order/pay/fail");          // 실패 시 URL

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        ResponseEntity<ReadyResponse> responseEntity = template.postForEntity(url, requestEntity, ReadyResponse.class);

        return responseEntity.getBody();
    }

    // 카카오페이 결제 승인
    @Override
    public ApproveResponse payApprove(String tid, String pgToken) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", "TC0ONETIME");
        parameters.put("tid", tid);
        parameters.put("partner_order_id", "1234567890");
        parameters.put("partner_user_id", "userId");
        parameters.put("pg_token", pgToken);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ApproveResponse approveResponse = template.postForObject(url, requestEntity, ApproveResponse.class);

        return approveResponse;
    }

    // 카카오페이 API 호출 시 필요한 헤더 생성
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + kakaoPaySecretKey); // 발급받은 Secret Key 입력
        headers.set("Content-type", "application/json");

        return headers;
    }
}

