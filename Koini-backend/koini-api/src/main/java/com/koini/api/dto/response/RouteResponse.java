package com.koini.api.dto.response;

public record RouteResponse(
    String routeId,
    String name,
    String origin,
    String destination,
    long fareKc,
    String fareUsd,
    boolean isActive
) {
}
