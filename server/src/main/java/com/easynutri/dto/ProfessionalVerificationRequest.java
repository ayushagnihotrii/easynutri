package com.easynutri.dto;

public record ProfessionalVerificationRequest(
    String professionalCategory,
    String credentialsSummary,
    String licenseNumber,
    String documentFilename) {}
