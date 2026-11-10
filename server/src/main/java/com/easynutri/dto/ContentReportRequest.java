package com.easynutri.dto;

public record ContentReportRequest(
    String targetType, Long targetId, String reasonCategory, String description) {}
