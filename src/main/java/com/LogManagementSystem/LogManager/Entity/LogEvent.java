package com.LogManagementSystem.LogManager.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "logs")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Data
public class LogEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private Instant ts;

//    @Column(nullable = false)
    private String message;

    private String service;

    private String level;

    // --- Tenant Information ---
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    // --- Enriched Data ---
    @Column(nullable = false)
    private Instant ingestedAt; // Timestamp when our system processed it

    private String hostname;

    private String clientIp;


    // --- JSONB field for flexible attributes ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attrs;

}
