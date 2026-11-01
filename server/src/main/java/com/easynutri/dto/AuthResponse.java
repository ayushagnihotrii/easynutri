package com.easynutri.dto;

public record AuthResponse(boolean success, String message, String username, String redirectUrl) {}
