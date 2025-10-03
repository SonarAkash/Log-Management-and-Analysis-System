package com.LogManagementSystem.LogManager.Repository;

import com.LogManagementSystem.LogManager.Entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByCompanyName(String companyName);
    Optional<Tenant> findByApiTokenHash(String apiTokenHash);
    boolean existsByCompanyName(String companyName);

    @Modifying
    @Query("UPDATE Tenant t SET t.apiTokenHash = :apiTokenHash WHERE t.id = :tenantId")
    int updateApiToken(@Param("tenantId") UUID tenantId,
                       @Param("apiTokenHash") String apiTokenHash);
}
