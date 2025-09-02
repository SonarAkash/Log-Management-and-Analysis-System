package com.LogManagementSystem.LogManager.Repository;

import com.LogManagementSystem.LogManager.Entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByCompanyName(String companyName);
    Optional<Tenant> findByApiTokenHash(String apiTokenHash);
    boolean existsByCompanyName(String companyName);
}
