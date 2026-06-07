package com.sunanda.resumescreener.controler;

import com.sunanda.resumescreener.model.ScreeningResult;
import com.sunanda.resumescreener.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Resume Screener is running!");
    }

    // Main screening endpoint
    @PostMapping("/screen")
    public ResponseEntity<ScreeningResult> screenResume(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            log.info("Screening resume: {}", resumeFile.getOriginalFilename());
            ScreeningResult result = resumeService.screenResume(resumeFile, jobDescription);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error screening resume", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}