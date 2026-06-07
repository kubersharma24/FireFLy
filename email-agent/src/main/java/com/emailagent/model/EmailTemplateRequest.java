package com.emailagent.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateRequest {

	    @NotBlank(message = "Description is required")
	    private String description;

	    @NotBlank(message = "Topic is required")
	    private String topic;

	    private String myNumber;

	    @NotBlank(message = "Sender name is required")
	    private String myName;
}
