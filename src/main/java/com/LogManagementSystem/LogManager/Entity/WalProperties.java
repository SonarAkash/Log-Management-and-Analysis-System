package com.LogManagementSystem.LogManager.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wal")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class WalProperties {
    private long maxSize;
    private String activeWalPath;
    private String archivedWalDirectoryPath;
}

