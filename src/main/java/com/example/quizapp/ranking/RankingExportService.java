package com.example.quizapp.ranking;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingExportService {
    private final RankingService rankingService;
    private final QuizRepository quizRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//    Export rankings to CSV
    public Resource exportToCsv(Long quizId) {
        log.info("Exporting rankings to CSV for quiz: {}", quizId);

        // Verify quiz exists
        quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        // Get full leaderboard
        List<RankingDto> rankings = rankingService.getFullLeaderboard(quizId);

        try (StringWriter stringWriter = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(stringWriter)) {

            // Write header
            String[] header = {
                    "Position", "Player", "Score", "Max Score", "Percentage",
                    "Correct", "Wrong", "Total Questions", "Time (seconds)",
                    "Grade", "Completed At"
            };
            csvWriter.writeNext(header);

            // Write data
            int position = 1;
            for (RankingDto ranking : rankings) {
                String[] data = {
                        String.valueOf(position++),
                        ranking.getPlayerNickname(),
                        String.valueOf(ranking.getScore()),
                        String.valueOf(ranking.getMaxScore()),
                        String.format("%.2f%%", ranking.getPercentageScore()),
                        String.valueOf(ranking.getCorrectAnswers()),
                        String.valueOf(ranking.getWrongAnswers()),
                        String.valueOf(ranking.getTotalQuestions()),
                        ranking.getTimeTakenSeconds() != null ? String.valueOf(ranking.getTimeTakenSeconds()) : "N/A",
                        ranking.getGrade(),
                        ranking.getCompletedAt() != null ? ranking.getCompletedAt().format(DATE_FORMATTER) : "N/A"
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
            byte[] csvBytes = stringWriter.toString().getBytes();

            log.info("CSV export completed for quiz {}. Size: {} bytes", quizId, csvBytes.length);

            return new ByteArrayResource(csvBytes);

        } catch (IOException e) {
            log.error("Failed to export rankings to CSV", e);
            throw new RuntimeException("Failed to export rankings to CSV", e);
        }
    }

//    Export rankings to PDF
    public Resource exportToPdf(Long quizId) {
        log.info("Exporting rankings to PDF for quiz: {}", quizId);

        // Verify quiz exists
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        // Get full leaderboard
        List<RankingDto> rankings = rankingService.getFullLeaderboard(quizId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph title = new Paragraph("Quiz Rankings: " + quiz.getTitle())
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Subtitle with quiz info
            Paragraph subtitle = new Paragraph(
                    String.format("Total Players: %d | Max Score: %d",
                            rankings.size(),
                            quiz.getTotalPoints())
            )
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subtitle);

            // Create table
            float[] columnWidths = {1, 3, 2, 2, 2, 2, 2, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header
            String[] headers = {
                    "Pos", "Player", "Score", "Max", "%",
                    "Correct", "Wrong", "Total", "Time", "Grade"
            };
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
            }

            // Data rows
            int position = 1;
            for (RankingDto ranking : rankings) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(position++))));
                table.addCell(new Cell().add(new Paragraph(ranking.getPlayerNickname())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ranking.getScore()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ranking.getMaxScore()))));
                table.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", ranking.getPercentageScore()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ranking.getCorrectAnswers()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ranking.getWrongAnswers()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ranking.getTotalQuestions()))));
                table.addCell(new Cell().add(new Paragraph(
                        ranking.getTimeTakenSeconds() != null ? ranking.getTimeTakenSeconds() + "s" : "N/A"
                )));
                table.addCell(new Cell().add(new Paragraph(ranking.getGrade())));
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("Generated on: " +
                    java.time.LocalDateTime.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            log.info("PDF export completed for quiz {}. Size: {} bytes", quizId, pdfBytes.length);

            return new ByteArrayResource(pdfBytes);

        } catch (Exception e) {
            log.error("Failed to export rankings to PDF", e);
            throw new RuntimeException("Failed to export rankings to PDF", e);
        }
    }
}