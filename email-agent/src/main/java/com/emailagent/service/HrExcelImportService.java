package com.emailagent.service;

import com.emailagent.model.HrContact;
import com.emailagent.repo.HrContactRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HrExcelImportService {

    @Autowired
    private HrContactRepository repository;

    public int loadHrList(MultipartFile file) throws IOException {
        List<HrContact> contacts = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                HrContact hr = new HrContact();
                hr.setSno((int) row.getCell(0).getNumericCellValue());
                hr.setName(getCellValue(row.getCell(1)));
                hr.setEmail(getCellValue(row.getCell(2)));
                hr.setTitle(getCellValue(row.getCell(3)));
                hr.setCompany(getCellValue(row.getCell(4)));

                contacts.add(hr);
            }
        }

        repository.saveAll(contacts);
        return contacts.size();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> "";
        };
    }
}