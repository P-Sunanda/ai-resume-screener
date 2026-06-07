// ResumeService.java
// Updated to use GeminiService (FREE) instead of OpenAI
// Author: Sunanda Panda

package com.sunanda.resumescreener.service;

import com.sunanda.resumescreener.model.ScreeningResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    // âœ… Now using FREE Gemini instead of OpenAI
    private final GeminiService geminiService;

    public ScreeningResult screenResume(MultipartFile resumeFile,
                                        String jobDescription) throws IOException {
        // Step 1: Extract text from uploaded PDF resume
        String resumeText = extractTextFromPDF(resumeFile);
        log.info("Extracted {} characters from resume: {}",
                resumeText.length(), resumeFile.getOriginalFilename());

        // Step 2: Send to Gemini AI for analysis
        String aiResponse = geminiService.analyzeResume(resumeText, jobDescription);
        log.info("Gemini AI analysis complete");

        // Step 3: Parse structured response
        return parseAIResponse(aiResponse);
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IOException("Could not extract text from PDF. " +
                        "Please ensure the PDF is not scanned/image-only.");
            }
            return text;
        }
    }

    private ScreeningResult parseAIResponse(String response) {
        ScreeningResult.ScreeningResultBuilder builder = ScreeningResult.builder();

        // Default values in case parsing fails
        builder.matchScore(0)
                .summary("Analysis complete")
                .strengths("See full response")
                .gaps("See full response")
                .recommendation("REVIEW MANUALLY");

        if (response == null || response.isBlank()) {
            return builder.build();
        }

        String[] lines = response.split("\n");
        StringBuilder strengthsBuilder = new StringBuilder();
        StringBuilder gapsBuilder = new StringBuilder();
        boolean inStrengths = false;
        boolean inGaps = false;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("SCORE:")) {
                inStrengths = false; inGaps = false;
                String scoreStr = line.replace("SCORE:", "")
                        .trim().replaceAll("[^0-9]", "");
                if (!scoreStr.isEmpty()) {
                    builder.matchScore(Math.min(100,
                            Integer.parseInt(scoreStr)));
                }
            } else if (line.startsWith("SUMMARY:")) {
                inStrengths = false; inGaps = false;
                builder.summary(line.replace("SUMMARY:", "").trim());
            } else if (line.startsWith("STRENGTHS:")) {
                inStrengths = true; inGaps = false;
                String val = line.replace("STRENGTHS:", "").trim();
                if (!val.isEmpty()) strengthsBuilder.append(val);
            } else if (line.startsWith("GAPS:")) {
                inStrengths = false; inGaps = true;
                String val = line.replace("GAPS:", "").trim();
                if (!val.isEmpty()) gapsBuilder.append(val);
            } else if (line.startsWith("RECOMMENDATION:")) {
                inStrengths = false; inGaps = false;
                builder.recommendation(
                        line.replace("RECOMMENDATION:", "").trim());
            } else if (inStrengths && !line.isEmpty()) {
                strengthsBuilder.append(" ").append(line);
            } else if (inGaps && !line.isEmpty()) {
                gapsBuilder.append(" ").append(line);
            }
        }

        if (strengthsBuilder.length() > 0)
            builder.strengths(strengthsBuilder.toString().trim());
        if (gapsBuilder.length() > 0)
            builder.gaps(gapsBuilder.toString().trim());

        return builder.build();
    }
}
