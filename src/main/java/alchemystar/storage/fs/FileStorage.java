package alchemystar.storage.fs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.table.Column;
import alchemystar.table.Row;
import alchemystar.table.Table;
import alchemystar.value.Value;
import alchemystar.value.ValueBoolean;
import alchemystar.value.ValueByte;
import alchemystar.value.ValueInt;
import alchemystar.value.ValueLong;
import alchemystar.value.ValueNull;
import alchemystar.value.ValueString;

/**
 * FileStorage
 *
 * @Author lizhuyang
 */
public class FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    // 当前FileStorage都在Session里
    private ArrayList<Row> rows;
    private int current;
    private Table table;
    private String filePath;
    private BufferedReader bufferedReader;
    private String seperator;
    private boolean skipWrong;

    public FileStorage(String filePath, Table table) {
        // 最多defaultRowsInMemory在缓存中
        rows = new ArrayList<Row>();
        current = -1;
        this.filePath = filePath;
        this.table = table;
        this.seperator = table.getSeperator();
        this.skipWrong = table.isSkipWrong();
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

    public boolean previous() {
        if (current > 0) {
            current = current - 1;
            return true;
        }
        return false;
    }

    public void initRead() {
        try {
            bufferedReader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(filePath), table.getCharset()));
            String line = null;
            // 一行一行读取
            int skipedCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (skipedCount < table.getSkipRows()) {
                    skipedCount++;
                    continue;
                }
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                String[] rawRow = line.split(this.seperator);
                if (rawRow.length != table.getColumns().length) {
                    // 这边做一个处理
                    // 如果",,"的形式split出来是2,加上空格后",, "split出来是3
                    // 这种情况的话,将最后的rawRow最后一个设置成"",表示空
                    rawRow = (line + " ").split(this.seperator);
                    rawRow[rawRow.length - 1] = "";
                    if (rawRow.length != table.getColumns().length) {
                        // 如果不skip wrong的话,则抛出异常
                        if (!this.skipWrong) {
                            throw new RuntimeException("file column length not match");
                        } else {
                            // 略过这一行
                            continue;
                        }
                    }
                }
                Value[] values = new Value[rawRow.length];
                // Only Support String|long
                for (int i = 0; i < table.getColumns().length; i++) {
                    Column column = table.getColumns()[i];
                    int type = column.getType();
                    String rawString = rawRow[i];
                    switch (type) {
                        case Value.STRING:
                            values[i] = ValueString.get(rawString);
                            break;
                        case Value.LONG:
                            if (StringUtils.isEmpty(rawString)) {
                                values[i] = ValueNull.INSTANCE;
                                break;
                            }
                            values[i] = ValueLong.get(Long.valueOf(rawString));
                            break;
                        case Value.BOOLEAN:
                            if ("TRUE".equals(rawString.toUpperCase())) {
                                values[i] = ValueBoolean.get(true);
                            } else if ("FALSE".equals(rawString.toUpperCase())) {
                                values[i] = ValueBoolean.get(false);
                            } else {
                                throw new RuntimeException("Can't Convert to Boolean,rawString=" + rawString);
                            }
                            break;
                        case Value.INT:
                            if (StringUtils.isEmpty(rawString)) {
                                values[i] = ValueNull.INSTANCE;
                                break;
                            }
                            values[i] = ValueInt.get(Integer.valueOf(rawString));
                            break;
                        case Value.BYTE:
                            if (StringUtils.isEmpty(rawString)) {
                                values[i] = ValueNull.INSTANCE;
                                break;
                            }
                            values[i] = ValueByte.get(Byte.valueOf(rawString));
                            break;
                        default:
                            throw new RuntimeException("Only Support BIGINT,VARCHAR,BOOLEAN");
                    }
                }
                Row row = new Row(values);
                rows.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                logger.error("close file fail", e);
            }
        }
    }

}
