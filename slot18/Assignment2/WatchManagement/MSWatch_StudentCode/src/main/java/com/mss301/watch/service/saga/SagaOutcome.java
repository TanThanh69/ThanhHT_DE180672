package com.mss301.watch.service.saga;

// [TODO-6.11]
public record SagaOutcome(String sagaId, Integer watchId, SagaStatus status) {
}
