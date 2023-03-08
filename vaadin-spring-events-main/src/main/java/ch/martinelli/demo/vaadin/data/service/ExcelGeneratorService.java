package ch.martinelli.demo.vaadin.data.service;

import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.data.entity.OperatorToDoItem;
import ch.martinelli.demo.vaadin.repository.OperationRepository;
import ch.martinelli.demo.vaadin.repository.OperatorToDoItemsRepository;
import ch.martinelli.demo.vaadin.repository.ProductInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.w3c.dom.Text;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class ExcelGeneratorService {
    private static final String RU = "RU";
    private static final String EN = Locale.ENGLISH.getLanguage();

    private final ProductInfoRepository productInfoRepository;
    private final OperationRepository operationRepository;
    private final OperatorToDoItemsRepository toDoItemsRepository;

    @Autowired
    public ExcelGeneratorService(ProductInfoRepository productInfoRepository, OperationRepository operationRepository, OperatorToDoItemsRepository toDoItemsRepository) {
        this.productInfoRepository = productInfoRepository;
        this.operationRepository = operationRepository;
        this.toDoItemsRepository = toDoItemsRepository;
    }

    @Transactional
    public Workbook crateExcelFile(UUID productId, Locale locale) {

        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("Operator actions");

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.index);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Font headerFont = wb.createFont();
//        headerFont.setColor(IndexedColors.BLUE.getIndex());
        Short headerSize = 12;
        headerFont.setFontHeightInPoints(headerSize);
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        //header
        int rowNumber = 0;
        Row row = sheet.createRow(rowNumber);
        Cell cell = row.createCell(0);
        cell.setCellValue("#");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(1);
        cell.setCellValue(locale.getLanguage().equalsIgnoreCase("EN") ? "Description" : "Описание");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(2);
        cell.setCellValue(locale.getLanguage().equalsIgnoreCase("EN") ? "Action" : "Действие");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(3);
        cell.setCellValue(locale.getLanguage().equalsIgnoreCase("EN") ? "Operation #" : "Операция №");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(4);
        cell.setCellValue(locale.getLanguage().equalsIgnoreCase("EN") ? "Date/Time" : "Дата/Время");
        cell.setCellStyle(headerStyle);


        CellStyle style2 = wb.createCellStyle();
        Font font2 = wb.createFont();
        font2.setBold(true);
        style2.setFont(font2);

        CellStyle operationStyle = wb.createCellStyle();
        Font operationFont = wb.createFont();
        operationFont.setBold(true);
        operationFont.setItalic(true);
        operationFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        Short operationSize = 12;
        operationFont.setFontHeightInPoints(operationSize);
        operationStyle.setFont(operationFont);

        List<Operation> operations = operationRepository.findAll(Sort.by("operationNumber").ascending());
        rowNumber = 2;
        for (Operation operation : operations) {
            row = sheet.createRow(rowNumber);
            createCell(row, 0, operationStyle, String.valueOf(operation.getOperationNumber()));

            createCell(row, 1, operationStyle,
                    locale.getLanguage().equalsIgnoreCase("EN") ? operation.getOperationName_en() : operation.getOperationName_ru());
//            rowNumber++;


            List<OperatorToDoItem> todos = toDoItemsRepository.findAllByProductInfoAndOperation(productId, operation.getId());
            for (OperatorToDoItem item : todos) {
                rowNumber++;
                row = sheet.createRow(rowNumber);

                createCell(row, 0, style2,
                        (item.getOperation() == null || item.getActionDescriptions() == null) ? "" :
                                item.getOperation().getOperationNumber() + "." + item.getActionDescriptions().getActionNumber());
                createCell(row, 1, style2,
                        locale.getLanguage().equalsIgnoreCase("EN") ?
                                item.getActionDescriptions().getActionNameEn() : item.getActionDescriptions().getActionNameRu());
                createCell(row, 2, style2,
                        item.getAction() == null ? "" :
                                (locale.getLanguage().equalsIgnoreCase("EN") ?
                                        item.getAction().name() : item.getAction().name()));
                createCell(row, 3, style2,item.getActionPerformedDT()==null?"": item.getActionPerformedDT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));

//                rowNumber++;
            }
        }

        return wb;
    }

    private Cell createCell(Row row, int cellNumber, CellStyle style, String value) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }
}
