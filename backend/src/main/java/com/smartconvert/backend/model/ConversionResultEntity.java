package com.smartconvert.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ConversionResultEntity - Represents a completed file conversion.
 * 
 * Key JPA Concepts:
 * - @Entity: Maps this class to the conversion_result_entity table in H2.
 * - Stores metadata linking the converted output file back to its source file (sourceFileId).
 * - Tracks output format, output filename, physical storage path, and timestamp.
 */
@Entity
public class ConversionResultEntity {

    @Id
    private String id; // Unique UUID identifier for this conversion record
    
    private String sourceFileId;   // Foreign reference linking to FileEntity.id
    private String outputFormat;   // Target format (e.g. "PDF", "DOCX", "CSV", "JSON")
    private String outputFilename; // Human-friendly download filename (e.g. "my_data.csv")
    private String storagePath;    // Absolute path on disk where the converted file is located
    private LocalDateTime convertedAt; // Timestamp when conversion completed

    /**
     * JPA Lifecycle Hook:
     * Generates a unique UUID and sets convertedAt timestamp before entity persistence.
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        convertedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSourceFileId() { return sourceFileId; }
    public void setSourceFileId(String sourceFileId) { this.sourceFileId = sourceFileId; }
    
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    
    public String getOutputFilename() { return outputFilename; }
    public void setOutputFilename(String outputFilename) { this.outputFilename = outputFilename; }
    
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
}

