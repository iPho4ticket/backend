package com.ipho4ticket.eventservice.presentation.except;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(String message) {
        super(message);  // 부모 클래스인 RuntimeException의 생성자 호출
    }
}