package com.smartconvert.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FileEntity - Represents an uploaded source file stored in the system.
 * 
 * Key JPA Concepts:
 * - @Entity: Tells Hibernate/JPA to map this class to a relational database table (file_entity).
 * - @Id: Denotes the unique primary key for each record.
 * - @PrePersist: A JPA lifecycle callback that executes automatically right before the entity
 *   is first inserted into the database. We use this to assign a secure random UUID and timestamp.
 */
@Entity
public class FileEntity {
    
    @Id
    private String id; // Unique UUID identifier
    
    private String originalFilename; // Original filename uploaded by user (e.g. "report.docx")
    private String mimeType;         // MIME content type (e.g. "application/pdf", "image/png")
    private String storagePath;      // Absolute filesystem path where the file is stored
    private LocalDateTime uploadedAt;// Timestamp when the file was received

    /**
     * JPA Lifecycle Hook:
     * Generates a UUID and records the current timestamp automatically before insertion.
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        uploadedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

