package alchemystar.parser;

import alchemystar.result.ResultInterface;

/**
 * @Author lizhuyang
 */
public interface CommandInterface {

    // UNKNOWN statement
    int UNKNOWN = 0;
    // CREATE table
    int CREATE_TABLE = 1;
    // SELECT statement
    int SELECT = 2;

    int getCommandType();

    boolean isQuery();

    ResultInterface executeQuery();

    // ResultInterface getMetaData();

}
