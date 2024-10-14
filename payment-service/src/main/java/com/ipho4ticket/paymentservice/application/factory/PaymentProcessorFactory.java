package com.ipho4ticket.paymentservice.application.factory;

import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.domain.model.PaymentMethod;
import com.ipho4ticket.paymentservice.domain.service.PaymentProcessor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processorMap;

    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        // 결제 모듈 이름과 구현체를 매핑
        this.processorMap = processors.stream()
            .collect(Collectors.toMap(processor -> processor.getClass().getSimpleName(), processor -> processor));
    }

    public PaymentProcessor getPaymentProcessor(PaymentMethod paymentMethod) {
        // Enum의 processorName 값을 사용하여 적절한 결제 모듈을 반환
        return processorMap.get(paymentMethod.getProcessorName());
    }


}
