package util;

import model.Performance;
import model.Ticket;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import repository.PerformanceRepository;
import repository.TicketRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ExcelExporter {

    private static final String FILE_NAME = "Excel.xlsx";

    private final int userId;
    private final PerformanceRepository performanceRepository;
    private final TicketRepository ticketRepository;

    public ExcelExporter(int userId,
                         PerformanceRepository performanceRepository,
                         TicketRepository ticketRepository) {
        this.userId = userId;
        this.performanceRepository = performanceRepository;
        this.ticketRepository = ticketRepository;
    }

    public void export() throws IOException, SQLException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(FILE_NAME)) {

            writePerformancesSheet(workbook);
            writeTicketsSheet(workbook);

            workbook.write(out);
        }
    }

    // ---------- Лист "Спектакли" ----------

    private void writePerformancesSheet(Workbook wb) throws SQLException {
        Sheet sheet = wb.createSheet("Спектакли");
        String[] headers = {"ID", "Название", "Описание", "Дата", "Длительность (мин)", "Цена"};

        writeHeader(wb, sheet, headers);

        List<Performance> performances = performanceRepository.findAll();
        int rowIdx = 1;
        for (Performance p : performances) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getTitle());
            row.createCell(2).setCellValue(p.getDescription());
            row.createCell(3).setCellValue(p.getDate());
            row.createCell(4).setCellValue(p.getDuration());
            row.createCell(5).setCellValue(p.getPrice());
        }

        autoSize(sheet, headers.length);
    }

    // ---------- Лист "Мои билеты" ----------

    private void writeTicketsSheet(Workbook wb) throws SQLException {
        Sheet sheet = wb.createSheet("Мои билеты");
        String[] headers = {"ID", "Спектакль", "Ряд", "Место", "Статус", "Создан"};

        writeHeader(wb, sheet, headers);

        List<Ticket> tickets = ticketRepository.findAllSortedByDate(userId);
        int rowIdx = 1;
        for (Ticket t : tickets) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(t.getId());
            row.createCell(1).setCellValue(t.getPerformanceTitle());
            row.createCell(2).setCellValue(t.getRowNumber());
            row.createCell(3).setCellValue(t.getSeatNumber());
            row.createCell(4).setCellValue(t.getStatus().name());
            row.createCell(5).setCellValue(t.getCreatedAt());
        }

        autoSize(sheet, headers.length);
    }

    // ---------- Вспомогательные ----------

    private void writeHeader(Workbook wb, Sheet sheet, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
