package com.ipho.ticketservice.domain.model;

public enum TicketStatus {
    OPENED, PENDING, CONFIRMED, CANCELED
    
    
    // OPENED 좌석 처리 직전의 최초의 상태
    // PENDING 결제 직전 좌석 처리 상태
    // CANCELED 취소 상태 ( 복구 불가, 처리 끝 )
    // CONFIRMED 결제완료 상태


    // 결제 완료 ( CONFIRMED ) 가 되기 위한 Ticket 의 상태 조건
    // => PENDING && ( IsDelete == false ) && ( expirationTime 이 지나지 않은 상태 )----- 해당 조건 아니면 무조건 예외 발생


}
