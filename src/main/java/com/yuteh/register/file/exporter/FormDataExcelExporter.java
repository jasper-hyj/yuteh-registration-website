package com.yuteh.register.file.exporter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuteh.register.form.model.Column;
import com.yuteh.register.form.model.Form;
import com.yuteh.register.form.model.FormData;
import com.yuteh.register.form.model.Section;
import com.yuteh.register.util.JSONUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.configurationprocessor.json.JSONException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataExcelExporter {
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private final List<FormData> formDataList;
    private final Form form;
    private final Map<String, Map<Integer, String>> sectionColumnMap;

    public FormDataExcelExporter(List<FormData> formDataList, Form form) {
        this.formDataList = formDataList;
        this.form = form;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet(this.form.getName());
        sectionColumnMap = new HashMap<>();
    }

    /**
     * Write excel header
     */
    private void writeHeaderRow() {
        Row row = sheet.createRow(0);
        int columnCount = 2;
        Cell cell = row.createCell(0);
        cell.setCellValue("時間戳記");
        cell = row.createCell(1);
        cell.setCellValue("註記");
        for (Section section : form.getSectionList()) {
            Map<Integer, String> columnMap = new HashMap<>();
            for (Column column : section.getColumnList()) {
                cell = row.createCell(columnCount);
                cell.setCellValue(column.getName());
                columnMap.put(columnCount, column.getId());
                columnCount += 1;
            }
            sectionColumnMap.put(section.getId(), columnMap);
        }
    }

    /**
     * Write data wto excel
     *
     * @throws JSONException getting errors
     */
    private void writeDataRows() throws JSONException {
        int rowCount = 1;
        for (FormData formData : this.formDataList) {
            Row row = sheet.createRow(rowCount);
            Cell cell = row.createCell(0);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cell.setCellValue(formatter.format(formData.getCreateTime()));
            cell = row.createCell(1);
            cell.setCellValue(formData.getMark());
            Map<String, Object> map = JSONUtil.jsonToMap(formData.getIdData());
            for (Map.Entry<String, Map<Integer, String>> entry : sectionColumnMap.entrySet()) {
                if (map.containsKey(entry.getKey()) && map.get(entry.getKey()) instanceof Map) {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
                    };
                    Map<String, Object> columnMap = new ObjectMapper().convertValue(map.get(entry.getKey()), typeRef);
                    for (Map.Entry<Integer, String> objectEntry : entry.getValue().entrySet()) {
                        if (columnMap.containsKey(objectEntry.getValue())) {
                            cell = row.createCell(objectEntry.getKey());
                            if (columnMap.get(objectEntry.getValue()) instanceof String) {
                                cell.setCellValue((String) columnMap.get(objectEntry.getValue()));
                            } else if (columnMap.get(objectEntry.getValue()) instanceof ArrayList) {
                                cell.setCellValue(String.join(", ", (ArrayList<String>) columnMap.get(objectEntry.getValue())));
                            }
                        }
                    }
                }
            }
            rowCount += 1;
        }
    }

    /**
     * Export excel file by servlet output stream
     *
     * @param response response
     * @throws IOException   file reading exception
     * @throws JSONException json parsing exception
     */
    public void export(HttpServletResponse response) throws IOException, JSONException {
        writeHeaderRow();
        writeDataRows();
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        workbook.close();
        stream.close();
    }
}
