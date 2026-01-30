package com.GDP.GDP.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiError (
    LocalDateTime timestamp,
    int status,
    String code, 
    String message,  // ex: "Ce nom est déjà pris"
    String path,     // ex: "/api/business/create"
    Map<String, List<String>> validationErrors // Optionnel (pour les formulaires)
) {}
