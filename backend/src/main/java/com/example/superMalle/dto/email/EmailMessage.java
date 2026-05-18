package com.example.superMalle.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Message DTO
 * 
 * Represents an email message to be sent asynchronously
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private String to;
    private String subject;
    private String templateName;
    private Object context;
    private String from;
    private String fromName;
    private String replyTo;
}
