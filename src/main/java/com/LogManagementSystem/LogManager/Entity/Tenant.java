package com.LogManagementSystem.LogManager.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@AllArgsConstructor @NoArgsConstructor
@Setter @Getter
public class Tenant {

    @Id
    private UUID id;

    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;

    @Column(name = "api_token_hash", nullable = false, unique = true)
    private String apiTokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "tenant")
    private List<User> users;
}
