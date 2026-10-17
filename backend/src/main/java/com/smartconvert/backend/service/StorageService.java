package com.smartconvert.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartconvert.backend.model.ConversionResultEntity;
import com.smartconvert.backend.model.FileEntity;
import com.smartconvert.backend.repository.ConversionResultRepository;
import com.smartconvert.backend.repository.FileRepository;

import jakarta.annotation.PostConstruct;

/**
 * StorageService - Manages physical file I/O, disk directories, and file lifecycle cleanup.
 * 
 * Key Responsibilities:
 * 1. Directory Initialization (@PostConstruct): Creates upload folder upon application startup.
 * 2. Uploaded File Persistence (storeFile): Writes raw multipart bytes to disk using unique UUID names.
 * 3. Converted File Registration (storeConvertedFile): Saves conversion output metadata in the database.
 * 4. Resource Streaming (loadAsResource): Resolves files from disk as Spring UrlResources for download.
 * 5. Automated Garbage Collection (@Scheduled): Periodically purges files older than 10 minutes.
 */
@Service
public class StorageService {

    // Configurable upload directory path (defaults to "uploads" in working directory)
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path resolvedUploadPath;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ConversionResultRepository conversionResultRepository;

    /**
     * Initializes the physical upload directory as soon as the Spring Bean is constructed.
     * Uses normalized absolute paths to ensure cross-platform compatibility (Windows & Linux).
     */
    @PostConstruct
    public void init() {
        try {
            Path root = Paths.get(".").toAbsolutePath().normalize();
            this.resolvedUploadPath = root.resolve(uploadDir).toAbsolutePath();
            Files.createDirectories(resolvedUploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + e.getMessage());
        }
    }

    /**
     * Stores an incoming multipart upload on disk and persists its metadata in the database.
     * 
     * Steps:
     * 1. Creates a new FileEntity record in H2 to generate a unique UUID.
     * 2. Writes the file bytes to disk using '<UUID>.<ext>' to guarantee no filename collisions.
     * 3. Updates the FileEntity with the absolute storage path and saves it.
     */
    public FileEntity storeFile(MultipartFile file) throws IOException {
        if (!Files.exists(resolvedUploadPath)) {
            Files.createDirectories(resolvedUploadPath);
        }

        String filename = file.getOriginalFilename();
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf("."));
        }

        FileEntity entity = new FileEntity();
        entity.setOriginalFilename(filename);
        entity.setMimeType(file.getContentType());
        
        // Save to database first to obtain generated UUID
        try {
            entity = fileRepository.save(entity);
        } catch (Exception e) {
            throw new IOException("Database Error: Could not save file info. " + e.getMessage());
        }

        String storedFilename = entity.getId() + extension;
        Path targetPath = resolvedUploadPath.resolve(storedFilename);
        
        // Write the incoming input stream directly to disk
        try {
            Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IOException("Storage Error: Could not save file to disk at " + targetPath + ". Error: " + e.getMessage());
        }

        entity.setStoragePath(targetPath.toString());
        return fileRepository.save(entity);
    }
    
    /**
     * Overload to store conversion results using default filename.
     */
    public ConversionResultEntity storeConvertedFile(String sourceId, File convertedFile, String format) {
        return storeConvertedFile(sourceId, convertedFile, format, convertedFile.getName());
    }

    /**
     * Persists conversion result metadata into the database with a human-friendly download name.
     */
    public ConversionResultEntity storeConvertedFile(String sourceId, File convertedFile, String format, String friendlyFilename) {
        ConversionResultEntity entity = new ConversionResultEntity();
        entity.setSourceFileId(sourceId);
        entity.setOutputFormat(format);
        entity.setOutputFilename(friendlyFilename != null && !friendlyFilename.isBlank() ? friendlyFilename : convertedFile.getName());
        entity.setStoragePath(convertedFile.getAbsolutePath());
        return conversionResultRepository.save(entity);
    }

    /**
     * Retrieves an uploaded file from disk by its database ID.
     */
    public File getFile(String id) {
        Optional<FileEntity> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent() && fileOpt.get().getStoragePath() != null) {
            return new File(fileOpt.get().getStoragePath());
        }
        return null;
    }

    /**
     * Retrieves a converted file from disk by its conversion result ID.
     */
    public File getConvertedFile(String resultId) {
        Optional<ConversionResultEntity> resultOpt = conversionResultRepository.findById(resultId);
        if (resultOpt.isPresent() && resultOpt.get().getStoragePath() != null) {
            return new File(resultOpt.get().getStoragePath());
        }
        return null;
    }

    /**
     * Loads a converted file as a Spring UrlResource for streaming downloads to the client browser.
     */
    public Resource loadAsResource(String resultId) {
        try {
            Optional<ConversionResultEntity> resultOpt = conversionResultRepository.findById(resultId);
            if (resultOpt.isPresent()) {
                String storagePath = resultOpt.get().getStoragePath();
                if (storagePath != null) {
                    Path directPath = Paths.get(storagePath);
                    if (Files.exists(directPath) && Files.isReadable(directPath)) {
                        return new UrlResource(directPath.toUri());
                    }
                    Path fallback = resolvedUploadPath.resolve(directPath.getFileName());
                    if (Files.exists(fallback) && Files.isReadable(fallback)) {
                        return new UrlResource(fallback.toUri());
                    }
                }
            }
            return null;
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }

    /**
     * Automated Cleanup Task (@Scheduled):
     * Runs in the background once every 60 seconds (fixedRate = 60000ms).
     * 
     * Why is this important?
     * - In a file conversion platform, uploaded and converted files consume disk storage rapidly.
     * - This scheduler automatically removes files and records older than 10 minutes to keep
     *   server disk usage minimal and prevent storage exhaustion on cloud servers.
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupOldFiles() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        // 1. Clean up old uploaded source files
        List<FileEntity> allFiles = new ArrayList<>();
        fileRepository.findAll().forEach(f -> {
            if (f.getUploadedAt().isBefore(tenMinutesAgo)) {
                allFiles.add(f);
            }
        });

        for (FileEntity file : allFiles) {
            try {
                if (file.getStoragePath() != null) {
                    Files.deleteIfExists(Paths.get(file.getStoragePath()));
                }
                fileRepository.delete(file);
            } catch (IOException e) {
                // Silently ignore if file was already removed
            }
        }

        // 2. Clean up old converted result files
        List<ConversionResultEntity> oldResults = conversionResultRepository.findByConvertedAtBefore(tenMinutesAgo);
        for (ConversionResultEntity res : oldResults) {
            try {
                if (res.getStoragePath() != null) {
                    Files.deleteIfExists(Paths.get(res.getStoragePath()));
                }
                conversionResultRepository.delete(res);
            } catch (IOException e) {
                // Silently ignore if file was already removed
            }
        }
    }
}

