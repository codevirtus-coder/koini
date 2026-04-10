package com.koini.persistence.repository;

import com.koini.core.domain.entity.Route;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, UUID> {
}
