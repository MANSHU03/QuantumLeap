package com.quantumleap.dto.whiteboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWhiteboardRequest {

    @NotBlank(message = "Whiteboard name is required")
    @Size(min = 1, max = 255, message = "Whiteboard name must be between 1 and 255 characters")
    private String name;

    // Constructors
    public CreateWhiteboardRequest() {}

    public CreateWhiteboardRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
