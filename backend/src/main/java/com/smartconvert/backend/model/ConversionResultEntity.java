package com.smartconvert.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ConversionResultEntity {

    @Id
    private String id;
    
    private String sourceFileId;
    private String outputFormat;
    private String outputFilename;
    private String storagePath;
    private LocalDateTime convertedAt;

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
