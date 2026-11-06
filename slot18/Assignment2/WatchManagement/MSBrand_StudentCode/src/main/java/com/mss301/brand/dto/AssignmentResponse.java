package com.mss301.brand.dto;

// [TODO-5.2, TODO-5.3]
public record AssignmentResponse(
    Integer watchId,
    Integer brandId,
    Integer previousBrandId,
    String status
) {
}
