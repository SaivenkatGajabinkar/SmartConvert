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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartconvert.backend.model.ConversionResultEntity;
import com.smartconvert.backend.model.FileEntity;
import com.smartconvert.backend.repository.ConversionResultRepository;
import com.smartconvert.backend.repository.FileRepository;

import jakarta.annotation.PostConstruct;

@Service
public class StorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path resolvedUploadPath;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ConversionResultRepository conversionResultRepository;

    @PostConstruct
    public void init() {
        try {
            Path root = Paths.get(".").toAbsolutePath().normalize();
            this.resolvedUploadPath = root.resolve(uploadDir).toAbsolutePath();
            Files.createDirectories(resolvedUploadPath);
            System.out.println(">>> Storage fully initialized at: " + resolvedUploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + e.getMessage());
        }
    }

    public FileEntity storeFile(MultipartFile file) throws IOException {
        // Emergency Check: Make sure the cupboard (folder) exists!
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
        
        // Save to database
        try {
            entity = fileRepository.save(entity);
        } catch (Exception e) {
            throw new IOException("Database Error: Could not save file info. " + e.getMessage());
        }

        String storedFilename = entity.getId() + extension;
        Path targetPath = resolvedUploadPath.resolve(storedFilename);
        
        try {
            Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IOException("Storage Error: Could not save file to disk at " + targetPath + ". Error: " + e.getMessage());
        }

        entity.setStoragePath(targetPath.toString());
        return fileRepository.save(entity);
    }
    
    public ConversionResultEntity storeConvertedFile(String sourceId, File convertedFile, String format) {
        ConversionResultEntity entity = new ConversionResultEntity();
        entity.setSourceFileId(sourceId);
        entity.setOutputFormat(format);
        entity.setOutputFilename(convertedFile.getName());
        entity.setStoragePath(convertedFile.getAbsolutePath());
        return conversionResultRepository.save(entity);
    }

    public File getFile(String id) {
        Optional<FileEntity> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            return new File(fileOpt.get().getStoragePath());
        }
        return null;
    }

    public File getConvertedFile(String resultId) {
        Optional<ConversionResultEntity> resultOpt = conversionResultRepository.findById(resultId);
        if (resultOpt.isPresent()) {
            return new File(resultOpt.get().getStoragePath());
        }
        return null;
    }
    public Resource loadAsResource(String resultId) {
        try {
            Optional<ConversionResultEntity> resultOpt = conversionResultRepository.findById(resultId);
            if (resultOpt.isPresent()) {
                Path file = resolvedUploadPath.resolve(Paths.get(resultOpt.get().getStoragePath()).getFileName());
                Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
            }
            return null;
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void cleanupOldFiles() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        List<FileEntity> allFiles = new ArrayList<>();
        fileRepository.findAll().forEach(f -> {
            if (f.getUploadedAt().isBefore(tenMinutesAgo)) {
                allFiles.add(f);
            }
        });

        for (FileEntity file : allFiles) {
            try {
                Files.deleteIfExists(Paths.get(file.getStoragePath()));
                fileRepository.delete(file);
            } catch (IOException e) {
                // Ignore
            }
        }

        List<ConversionResultEntity> oldResults = conversionResultRepository.findByConvertedAtBefore(tenMinutesAgo);
        for (ConversionResultEntity res : oldResults) {
            try {
                Files.deleteIfExists(Paths.get(res.getStoragePath()));
                conversionResultRepository.delete(res);
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
