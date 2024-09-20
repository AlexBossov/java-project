package ru.java.project;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Parser {

    public Map<Class<?>, List<Object>> parse(String filePath) throws Exception {
        Map<Class<?>, List<Object>> resultMap = new HashMap<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            return null;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row classRow = sheet.getRow(0);
            Row fieldRow = sheet.getRow(2);

            List<Class<?>> classes = new ArrayList<>();
            List<List<String>> fields = new ArrayList<>();
            int numColumns = classRow.getLastCellNum();
            Class<?> currentClass = null;
            List<String> currentFields = new ArrayList<>();

            for (int i = 0; i < numColumns; i++) {
                Cell classCell = classRow.getCell(i);
                Cell fieldCell = fieldRow.getCell(i);

                if (classCell != null && !classCell.getStringCellValue().trim().isEmpty()) {
                    if (currentClass != null) {
                        classes.add(currentClass);
                        fields.add(currentFields);
                    }

                    String className = classCell.getStringCellValue().trim();

                    String classNameWithPackage = "ru.java.project." + className;
                    currentClass = Class.forName(classNameWithPackage);
                    currentFields = new ArrayList<>();
                }

                if (fieldCell != null && currentClass != null) {
                    String fieldName = fieldCell.getStringCellValue().trim();
                    if (isNotBlank(fieldName)) {
                        currentFields.add(fieldName);
                    }
                }
            }

            if (currentClass != null) {
                classes.add(currentClass);
                fields.add(currentFields);
            }

            for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);
                if (dataRow == null) continue;

                int column = 0;
                for (int classIndex = 0; classIndex < classes.size(); classIndex++) {
                    Class<?> clazz = classes.get(classIndex);
                    List<String> classFields = fields.get(classIndex);

                    Object obj = clazz.getDeclaredConstructor().newInstance();

                    for (String fieldName : classFields) {
                        Cell cell = dataRow.getCell(column);

                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            setFieldValue(clazz, obj, fieldName, cellValue);
                        }

                        column++;
                    }

                    resultMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(obj);

                    column++;
                }
            }
        }

        return resultMap;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private void setFieldValue(Class<?> clazz, Object obj, String fieldName, String value) throws
            IllegalAccessException {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (fieldType == String.class) {
                field.set(obj, value);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                field.set(obj, Integer.parseInt(value));
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(value));
            }
        } catch (NoSuchFieldException ignored) {
        }
    }
}
