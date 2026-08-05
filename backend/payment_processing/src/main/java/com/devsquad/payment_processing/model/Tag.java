package com.devsquad.payment_processing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Tag {

    private Integer tagId;

    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must not exceed 100 characters")
    private String tagName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public Tag() {}

    public Tag(Integer tagId, String tagName, String description) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.description = description;
    }

    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}