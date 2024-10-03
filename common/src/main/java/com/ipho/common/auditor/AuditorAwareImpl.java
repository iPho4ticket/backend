package com.ipho.common.auditor;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String updateBy = request.getHeader("X-USER-ID");   // 임시 헤더
        if (updateBy == null) throw new SecurityException("X-USER-ID is required");

        return Optional.of(updateBy);

    }
}
