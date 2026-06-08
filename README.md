A cloud-native AI-powered resume screening tool built with Java 17, Spring Boot, and Google Gemini API, deployed on GCP Cloud Run.

## Tech Stack
- Java 17
- Spring Boot 3.2
- Google Gemini AI (FREE)
- Apache PDFBox
- GCP Cloud Run
- Docker

## Features
- Upload any PDF resume
- Paste any job description
- Get AI match score (0-100)
- Strengths and gaps analysis
- Hiring recommendation: STRONG HIRE / HIRE / MAYBE / REJECT

## Live API
https://ai-resume-screener-1049212543899.us-central1.run.app

## Local Setup
1. Get free Gemini API key from aistudio.google.com
2. Set environment variable: GEMINI_API_KEY=your_key
3. Run: mvn clean package -DskipTests
4. Run: java -jar target/ai-resume-screener-1.0.0.jar
5. Test: http://localhost:8080/api/resume/health

## Author
Sunanda Panda
Senior Java Developer | Spring Boot | Microservices | GCP
https://www.linkedin.com/in/psunanda/

## Author
Sunanda Panda
Senior Java Developer | Spring Boot | Microservices | GCP
