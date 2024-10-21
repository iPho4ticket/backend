//package com.ipho4ticket.paymentservice;
//
//import java.lang.reflect.Field;
//
//public class ReflectionUtils {
//
//    // UUID 설정을 위한 리플렉션 메소드
//    public static void setField(Object target, String fieldName, Object value) {
//        try {
//            Field field = target.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true); // private 필드 접근 허용
//            field.set(target, value);  // 필드에 값 설정
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException(e); // 예외 발생 시 처리
//        }
//    }
//}
