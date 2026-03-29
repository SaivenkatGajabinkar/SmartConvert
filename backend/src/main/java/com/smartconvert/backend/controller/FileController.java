package com.smartconvert.backend.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartconvert.backend.model.ConversionResultEntity;
import com.smartconvert.backend.model.FileEntity;
import com.smartconvert.backend.repository.ConversionResultRepository;
import com.smartconvert.backend.repository.FileRepository;
import com.smartconvert.backend.service.ConversionEngine;
import com.smartconvert.backend.service.StorageService;
import com.smartconvert.backend.service.GeminiService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class FileController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private ConversionEngine conversionEngine;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ConversionResultRepository conversionResultRepository;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile document) {
        if (document.isEmpty() || document.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File is empty or exceeds 10MB limit");
        }
        try {
            FileEntity fileEntity = storageService.storeFile(document);
            List<String> formats = conversionEngine.getAvailableFormats(fileEntity.getMimeType(), fileEntity.getOriginalFilename());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileId", fileEntity.getId());
            response.put("originalName", fileEntity.getOriginalFilename());
            response.put("availableFormats", formats);
            response.put("recommendedFormat", formats.isEmpty() ? null : formats.get(0));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        }
    }

    @PostMapping("/convert/{fileId}")
    public ResponseEntity<?> convertFile(@PathVariable String fileId, @RequestBody Map<String, List<String>> request) {
        Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            List<String> formats = request.get("formats");
            List<ConversionResultEntity> results = conversionEngine.convert(fileOpt.get(), formats);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Conversion failed");
        }
    }

    @GetMapping("/download/{resultId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String resultId) {
        File file = storageService.getConvertedFile(resultId);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        Optional<ConversionResultEntity> resultEntityOpt = conversionResultRepository.findById(resultId);
        String outputName = resultEntityOpt.map(ConversionResultEntity::getOutputFilename).orElse("converted_file");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    @PostMapping("/summarize/{fileId}")
    public ResponseEntity<?> summarizeFile(@PathVariable String fileId) {
        Optional<FileEntity> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            File inputFile = storageService.getFile(fileId);
            String text = "";
            String filename = fileOpt.get().getOriginalFilename().toLowerCase();

            if (filename.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(inputFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(document);
                }
            } else {
                text = "File format not fully supported for detailed text extraction yet, but here is the metadata: " + filename;
            }

            String summary = geminiService.summarize(text);
            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Summarization failed: " + e.getMessage());
        }
    }
}
