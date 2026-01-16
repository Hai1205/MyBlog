package com.example.blogservice.services;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.blogservice.exceptions.OurException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileParserService {

    public String extractTextFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new OurException("File is empty", 400);
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new OurException("Invalid filename", 400);
        }

        String extension = getFileExtension(filename);

        try {
            switch (extension.toLowerCase()) {
                case "pdf":
                    return extractTextFromPDF(file.getInputStream());
                case "docx":
                case "doc":
                    return extractTextFromDocx(file.getInputStream());
                case "txt":
                    return extractTextFromTxt(file.getInputStream());
                default:
                    throw new OurException("Unsupported file format. Please upload PDF, DOCX, or TXT file.", 400);
            }
        } catch (IOException e) {
            throw new OurException("Error reading file: " + e.getMessage(), 500);
        }
    }

    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromTxt(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes());
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
