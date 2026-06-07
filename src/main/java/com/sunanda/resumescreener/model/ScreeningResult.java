package com.sunanda.resumescreener.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ScreeningResult {
    private int matchScore;         // 0-100
    private String summary;         // AI summary of candidate
    private String strengths;       // Key strengths found
    private String gaps;            // Missing skills/gaps
    private String recommendation;  // Hire / Maybe / Reject
}