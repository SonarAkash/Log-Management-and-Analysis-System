package com.LogManagementSystem.LogManager.QueryAPI;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QueryDTO(@NotNull @NotBlank @NotEmpty String query) {
}
