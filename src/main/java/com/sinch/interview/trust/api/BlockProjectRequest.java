package com.sinch.interview.trust.api;

import jakarta.validation.constraints.NotBlank;

public record BlockProjectRequest(
        @NotBlank String reason,
        @NotBlank String actor
) {
}
