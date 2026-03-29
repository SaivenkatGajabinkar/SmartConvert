package com.smartconvert.backend.service;

import com.smartconvert.backend.model.ConversionResultEntity;
import com.smartconvert.backend.model.FileEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class ConversionEngine {

    @Autowired
    private StorageService storageService;

    public List<String> getAvailableFormats(String mimeType, String filename) {
        String lowerFilename = filename.toLowerCase();
        if (mimeType.contains("pdf") || lowerFilename.endsWith(".pdf")) {
            return Arrays.asList("TXT", "DOCX", "HTML", "JPEG", "PNG", "EPUB", "MOBI");
        } else if (mimeType.contains("image") || lowerFilename.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp)")) {
            return Arrays.asList("PDF", "TXT", "DOCX", "PNG", "JPG", "WEBP", "GIF", "BMP", "TIFF", "SVG", "ICO", "SVG");
        } else if (mimeType.contains("spreadsheet") || mimeType.contains("excel") || lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".csv")) {
            return Arrays.asList("CSV", "XLSX", "JSON", "XML", "HTML", "PDF", "TSV", "YAML", "SQL");
        } else if (mimeType.contains("wordprocess") || lowerFilename.endsWith(".docx")) {
            return Arrays.asList("TXT", "PDF", "HTML", "RTF", "ODT", "EPUB");
        } else if (mimeType.contains("audio") || lowerFilename.matches(".*\\.(mp3|wav|ogg|flac|m4a|aac)")) {
            return Arrays.asList("MP3", "WAV", "OGG", "FLAC", "AAC", "M4A", "WMA", "TXT", "SRT");
        } else if (mimeType.contains("video") || lowerFilename.matches(".*\\.(mp4|avi|mkv|mov|wmv|webm)")) {
            return Arrays.asList("MP4", "AVI", "MKV", "MOV", "WEBM", "GIF", "MP3", "WAV");
        }
        // Catch-all
        return Arrays.asList("ZIP", "TAR", "GZ", "PDF", "TXT");
    }

    public List<ConversionResultEntity> convert(FileEntity fileEntity, List<String> targetFormats) throws Exception {
        File inputFile = storageService.getFile(fileEntity.getId());
        List<ConversionResultEntity> results = new ArrayList<>();

        for (String format : targetFormats) {
            File outputFile = new File(inputFile.getParent(), UUID.randomUUID().toString() + "." + format.toLowerCase());
            
            if (format.equals("TXT") && fileEntity.getMimeType().contains("image")) {
                // Tesseract OCR implementation
                try {
                    net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();
                    tesseract.setDatapath("tessdata");
                    String text = tesseract.doOCR(inputFile);
                    try (PrintWriter out = new PrintWriter(outputFile)) {
                        out.println(text == null || text.trim().isEmpty() ? "No text found in image." : text);
                    }
                } catch (Exception e) {
                    // Fallback if Tesseract isn't configured natively on the local machine
                    try (PrintWriter out = new PrintWriter(outputFile)) {
                        out.println("Tesseract OCR fallback triggered. To enable authentic AI OCR, install Tesseract on your host machine.");
                    }
                }
            } else if (format.equals("TXT") && fileEntity.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(inputFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    try (PrintWriter out = new PrintWriter(outputFile)) {
                        if (text != null && !text.trim().isEmpty()) {
                            out.println(text);
                        } else {
                            out.println("Scan Detected: This PDF appears to be an image-only document.");
                            out.println("Enable AI OCR to extract text from scanned images.");
                        }
                    }
                } catch (Exception e) {
                    try (PrintWriter out = new PrintWriter(outputFile)) {
                        out.println("Conversion Error Trace:");
                        out.println("Failed to parse PDF with native stripper: " + e.getMessage());
                        out.println("Recommendation: Use AI conversion module for corrupt or complex PDF structures.");
                    }
                }
            } else if (format.equals("CSV") && fileEntity.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                try (Workbook wb = WorkbookFactory.create(inputFile);
                     PrintWriter out = new PrintWriter(outputFile)) {
                    Sheet sheet = wb.getSheetAt(0);
                    for (Row row : sheet) {
                        List<String> cells = new ArrayList<>();
                        for (Cell cell : row) {
                            cells.add(cell.toString());
                        }
                        out.println(String.join(",", cells));
                    }
                }
            } else if (format.equals("PDF") && fileEntity.getMimeType().contains("image")) {
                 try (PDDocument doc = new PDDocument()) {
                     doc.save(outputFile);
                 }
            } else {
                // Remove Simulation data, just provide a simple placeholder or error
                try (PrintWriter out = new PrintWriter(outputFile)) {
                    out.println("Processing error: Conversion to " + format + " is not supported for this file type yet.");
                }
            }
            results.add(storageService.storeConvertedFile(fileEntity.getId(), outputFile, format));
        }
        return results;
    }
}
