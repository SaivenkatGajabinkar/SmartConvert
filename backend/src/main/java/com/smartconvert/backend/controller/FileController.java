package com.smartconvert.backend.controller;

import com.smartconvert.backend.model.ConversionResultEntity;
import com.smartconvert.backend.model.FileEntity;
import com.smartconvert.backend.repository.FileRepository;
import com.smartconvert.backend.repository.ConversionResultRepository;
import com.smartconvert.backend.service.ConversionEngine;
import com.smartconvert.backend.service.GeminiService;
import com.smartconvert.backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private ConversionEngine conversionEngine;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ConversionResultRepository resultRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty.");
            if (file.getSize() > 50 * 1024 * 1024) return ResponseEntity.badRequest().body("File exceeds 50MB limit.");

            FileEntity fileEntity = storageService.storeFile(file);
            List<String> formats = conversionEngine.getAvailableFormats(fileEntity.getMimeType(), fileEntity.getOriginalFilename());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileId", fileEntity.getId());
            response.put("originalName", fileEntity.getOriginalFilename());
            response.put("availableFormats", formats);
            response.put("recommendedFormat", formats.isEmpty() ? null : formats.get(0));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/convert/{fileId}")
    public ResponseEntity<?> convertFile(@PathVariable String fileId, @RequestBody Map<String, List<String>> request) {
        try {
            Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
            if (fileOpt.isEmpty()) return ResponseEntity.notFound().build();

            List<String> formats = request.get("formats");
            List<ConversionResultEntity> results = conversionEngine.convert(fileOpt.get(), formats);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Conversion failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{resultId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String resultId) {
        try {
            Optional<ConversionResultEntity> resultOpt = resultRepository.findById(resultId);
            if (resultOpt.isEmpty()) return ResponseEntity.notFound().build();

            Resource resource = storageService.loadAsResource(resultId);
            String filename = resultOpt.get().getOutputFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/summarize/{fileId}")
    public ResponseEntity<?> summarizeFile(@PathVariable String fileId) {
        try {
            Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
            if (fileOpt.isEmpty()) return ResponseEntity.notFound().build();

            String text = conversionEngine.extractText(fileOpt.get());
            Map<String, String> summaryResult = geminiService.summarize(text);
            
            return ResponseEntity.ok(summaryResult);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Summarization failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        try {
            List<ConversionResultEntity> results = new ArrayList<>();
            resultRepository.findAll().forEach(results::add);
            results.sort((a, b) -> b.getConvertedAt().compareTo(a.getConvertedAt()));
            
            List<Map<String, Object>> response = new ArrayList<>();
            for (ConversionResultEntity res : results) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", res.getId());
                map.put("sourceFileId", res.getSourceFileId());
                map.put("outputFormat", res.getOutputFormat());
                map.put("outputFilename", res.getOutputFilename());
                map.put("convertedAt", res.getConvertedAt().toString());
                
                Optional<FileEntity> sourceOpt = fileRepository.findById(res.getSourceFileId());
                if (sourceOpt.isPresent()) {
                    map.put("originalFilename", sourceOpt.get().getOriginalFilename());
                } else {
                    map.put("originalFilename", "Unknown Source");
                }
                response.add(map);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve history: " + e.getMessage());
        }
    }
}
