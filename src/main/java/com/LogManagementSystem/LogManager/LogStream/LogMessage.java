package com.LogManagementSystem.LogManager.LogStream;

import lombok.*;

@Builder
@Data
@AllArgsConstructor @NoArgsConstructor
@Setter @Getter
public class LogMessage {
    private String type;
    private String payload;
}
