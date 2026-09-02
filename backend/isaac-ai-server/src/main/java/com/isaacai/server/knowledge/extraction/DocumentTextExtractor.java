package com.isaacai.server.knowledge.extraction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DocumentTextExtractor {

    public String extract(MultipartFile file) {

        String contentType = file.getContentType();

        try {
            if ("application/pdf".equals(contentType)) {
                return extractPdf(file);
            }

            if ("text/plain".equals(contentType)) {
                return extractText(file);
            }

            throw new IllegalArgumentException(
                    "Unsupported file type: " + contentType
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to extract text from document.",
                    exception
            );
        }
    }

    private String extractPdf(
            MultipartFile file
    ) throws IOException {

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }

    private String extractText(
            MultipartFile file
    ) throws IOException {

        return new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
    }
}