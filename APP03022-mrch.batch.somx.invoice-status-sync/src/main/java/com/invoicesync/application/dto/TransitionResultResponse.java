package com.invoicesync.application.dto;

import java.time.LocalDateTime;

public record TransitionResultResponse(
        String idProveedor,
        String documentNumber,
        String uuid,
        int previousStatus,
        int newStatus,
        boolean success,
        String message,
        LocalDateTime processedAt
) {}
