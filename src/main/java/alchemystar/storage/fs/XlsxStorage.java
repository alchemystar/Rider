package alchemystar.storage.fs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.table.Column;
import alchemystar.table.Row;
import alchemystar.table.Table;
import alchemystar.value.Value;
import alchemystar.value.ValueString;

/**
 * @Author lizhuyang
 */
public class XlsxStorage {

    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    // 当前FileStorage都在Session里
    private ArrayList<Row> rows;
    private int current;
    private Table table;
    private String filePath;
    private boolean skipWrong;
    private int sheetNumber;

    public XlsxStorage(String filePath, Table table) {
        // 最多defaultRowsInMemory在缓存中
        rows = new ArrayList<Row>();
        current = -1;
        this.filePath = filePath;
        this.table = table;
        this.skipWrong = table.isSkipWrong();
        this.sheetNumber = table.getSheetNumber();
        initRead();
    }

    public Row get() {
        return rows.get(current);
    }

    public boolean next() {
        if (current < rows.size() - 1) {
            current += 1;
            return true;
        }
        return false;
    }

    public void reset() {
        current = -1;
    }

    public boolean previous() {
        if (current > 0) {
            current = current - 1;
            return true;
        }
        return false;
    }

    public void initRead() {
        Workbook wb = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);

            wb = new XSSFWorkbook(inputStream);//解析xlsx格式
            Sheet sheetOne = wb.getSheetAt(sheetNumber);
            int lastRowIndex = sheetOne.getLastRowNum();
            int skipedCount = 0;
            for (int rIndex = 1; rIndex <= lastRowIndex; rIndex++) {
                org.apache.poi.ss.usermodel.Row row = sheetOne.getRow(rIndex);
                if (skipedCount < table.getSkipRows()) {
                    skipedCount++;
                    continue;
                }
                if (row.getLastCellNum() != table.getColumns().length) {
                    // 如果不skip wrong的话,则抛出异常
                    if (!this.skipWrong) {
                        throw new RuntimeException("file column length not match");
                    } else {
                        // 略过这一行
                        continue;
                    }
                }
                if (row == null) {
                    continue;
                }
                Value[] values = new Value[table.getColumns().length];
                for (int i = 0; i < table.getColumns().length; i++) {
                    Column column = table.getColumns()[i];
                    int type = column.getType();
                    String rawString = getValue(row.getCell(i));
                    switch (type) {
                        case Value.STRING:
                            values[i] = ValueString.get(rawString);
                            break;
                        default:
                            throw new RuntimeException("Only Support VARCHAR in Xlsx");
                    }
                }
                Row rowToAdd = new Row(values);
                rows.add(rowToAdd);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.error("close inputstream errors");
                }
            }
        }
    }

    private String getValue(Cell cell) {
        if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            return String.valueOf(cell.getNumericCellValue());
        } else {
            // 返回字符串类型的值
            return String.valueOf(cell.getStringCellValue());
        }
    }

}
