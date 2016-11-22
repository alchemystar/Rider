package alchemystar.storage.fs;

import java.util.concurrent.ConcurrentHashMap;

import alchemystar.table.Table;

/**
 * 文件系统 For Archer
 *
 * @Author lizhuyang
 */
public class FileSys {

    // Todo Concurrency
    public ConcurrentHashMap<String, FileStorage> fileTableMap = new ConcurrentHashMap<String, FileStorage>();

    public FileStorage readTable(String filePath, Table table) {
        return null;
    }
}
