package com.LogManagementSystem.LogManager.QueryAPI;

import jakarta.validation.constraints.NotBlank;

public record SearchRequestDTO(
        @NotBlank String q,
        String start,
        String end
        ) {

}
