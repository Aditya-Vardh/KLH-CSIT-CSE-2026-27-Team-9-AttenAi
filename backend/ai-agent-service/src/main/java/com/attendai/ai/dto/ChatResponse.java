package com.attendai.ai.dto;

public record ChatResponse(
        String intent,
        String answer,
        Object data     // structured payload (leave balance list, attendance %, etc.)
) {}
