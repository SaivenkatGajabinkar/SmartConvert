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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Core File Operations
 * 
 * Handles all client-facing API routes for:
 * 1. File Uploading & Format Detection (/api/upload)
 * 2. File Conversion Execution (/api/convert/{fileId})
 * 3. File Streaming & Downloading (/api/download/{resultId})
 * 4. AI Document Summarization (/api/summarize/{fileId})
 * 5. Historical Conversion Records (/api/history)
 */
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

    /**
     * Step 1: Upload a File
     * 
     * How it works:
     * - Accepts a MultipartFile from an HTTP POST request.
     * - Validates that the file is not empty and is under the 50MB limit.
     * - Saves the file to disk using StorageService and creates a database record in FileEntity.
     * - Inspects the file type to calculate which target formats the user can convert to.
     * - Returns JSON containing the generated fileId, original filename, and available target formats.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty.");
            if (file.getSize() > 50 * 1024 * 1024) return ResponseEntity.badRequest().body("File exceeds 50MB limit.");

            // Store the uploaded file on disk and persist its metadata in H2
            FileEntity fileEntity = storageService.storeFile(file);

            // Determine compatible output formats based on MIME type and file extension
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

    /**
     * Step 2: Convert a File
     * 
     * How it works:
     * - Takes the fileId in the URL path and an array of selected formats in the JSON request body.
     * - Looks up the uploaded file in the database.
     * - Passes the source file to ConversionEngine, which runs format-specific parsers
     *   (PDFBox, Apache POI, Tesseract OCR, etc.) to produce the target files.
     * - Persists conversion metadata in ConversionResultEntity and returns the results.
     */
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

    /**
     * Step 3: Download a Converted File
     * 
     * How it works:
     * - Takes the resultId of the converted file.
     * - Loads the file as a Spring Resource (UrlResource) from disk.
     * - Sets the HTTP Content-Disposition header with 'attachment; filename="..."'
     *   so the user's web browser automatically triggers a file download dialog
     *   with a clean, human-readable filename (e.g. "document.docx").
     */
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

    /**
     * Step 4: AI Document Summarization
     * 
     * How it works:
     * - Extracts raw text content from the uploaded document (PDF, Word, TXT, Excel, PPTX, or Image).
     * - Sends the extracted text to GeminiService.
     * - If a Gemini API key is configured, it calls Google Gemini 1.5 Flash for an AI summary.
     * - If offline or no key is set, it falls back to a fast local extractive algorithm.
     */
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

    /**
     * Step 5: Conversion History
     * 
     * How it works:
     * - Queries all previous conversion results from the H2 database.
     * - Sorts them in descending chronological order (most recent first).
     * - Joins each result with its original source file name so the user can easily identify it.
     */
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
                
                // Fetch original filename for clear display in history table
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

