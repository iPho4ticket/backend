package com.ipho4ticket.paymentservice.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class PaymentInfoResponse {

    private String tid;  // 결제 고유번호
    private String cid;  // 가맹점 코드
    private String status;  // 결제 상태
    private String partnerOrderId;  // 가맹점 주문번호
    private String partnerUserId;  // 가맹점 회원 ID
    private String paymentMethodType;  // 결제 수단, CARD 또는 MONEY
    private Amount amount;  // 결제 금액 정보
    private CanceledAmount canceledAmount;  // 취소된 금액 정보
    private CancelAvailableAmount cancelAvailableAmount;  // 남은 취소 가능 금액
    private String itemName;  // 상품 이름
    private String itemCode;  // 상품 코드
    private Integer quantity;  // 상품 수량
    private LocalDateTime createdAt;  // 결제 준비 요청 시각
    private LocalDateTime approvedAt;  // 결제 승인 시각
    private LocalDateTime canceledAt;  // 결제 취소 시각
    private SelectedCardInfo selectedCardInfo;  // 결제 카드 정보
    private List<PaymentActionDetail> paymentActionDetails;  // 결제/취소 상세 내역

    // 내부 클래스들

    @Getter
    @ToString
    public static class Amount {

        private Integer total;  // 전체 결제 금액
        private Integer taxFree;  // 비과세 금액
        private Integer vat;  // 부가세 금액
        private Integer point;  // 사용된 포인트 금액
        private Integer discount;  // 할인 금액
        private Integer greenDeposit;  // 컵 보증금
    }

    @Getter
    @ToString
    public static class CanceledAmount {

        private Integer total;  // 취소된 전체 누적 금액
        private Integer taxFree;  // 취소된 비과세 누적 금액
        private Integer vat;  // 취소된 부가세 누적 금액
        private Integer point;  // 취소된 포인트 누적 금액
        private Integer discount;  // 취소된 할인 누적 금액
        private Integer greenDeposit;  // 취소된 컵 보증금
    }

    @Getter
    @ToString
    public static class CancelAvailableAmount {

        private Integer total;  // 전체 취소 가능 금액
        private Integer taxFree;  // 취소 가능한 비과세 금액
        private Integer vat;  // 취소 가능한 부가세 금액
        private Integer point;  // 취소 가능한 포인트 금액
        private Integer discount;  // 취소 가능한 할인 금액
        private Integer greenDeposit;  // 취소 가능한 컵 보증금
    }

    @Getter
    @ToString
    public static class SelectedCardInfo {

        private String cardBin;  // 카드 BIN 번호
        private Integer installMonth;  // 할부 개월 수
        private String installmentType;  // 할부 유형 (CARD_INSTALLMENT, SHARE_INSTALLMENT)
        private String cardCorpName;  // 카드사 이름
        private String interestFreeInstall;  // 무이자 할부 여부 (Y/N)
    }

    @Getter
    @ToString
    public static class PaymentActionDetail {

        private String aid;  // Request 고유 번호
        private LocalDateTime approvedAt;  // 거래 시간
        private Integer amount;  // 결제/취소 금액
        private Integer pointAmount;  // 결제/취소 포인트 금액
        private Integer discountAmount;  // 할인 금액
        private Integer greenDeposit;  // 컵 보증금
        private String paymentActionType;  // 결제 타입 (PAYMENT, CANCEL, ISSUED_SID)
        private String paymentMethodType;  // 결제 수단
        private String payload;  // Request로 전달된 값
    }
}


