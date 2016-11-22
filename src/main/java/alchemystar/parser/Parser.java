package alchemystar.parser;

import java.util.ArrayList;

import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.expression.Comparison;
import alchemystar.expression.ConditionAndOr;
import alchemystar.expression.ConditionIn;
import alchemystar.expression.Expression;
import alchemystar.expression.ExpressionColumn;
import alchemystar.expression.ExpressionList;
import alchemystar.expression.Operation;
import alchemystar.expression.ValueExpression;
import alchemystar.expression.WildCard;
import alchemystar.parser.ddl.CreateTable;
import alchemystar.parser.dml.Select;
import alchemystar.schema.Schema;
import alchemystar.table.Column;
import alchemystar.table.Table;
import alchemystar.table.TableFilter;
import alchemystar.value.DataType;
import alchemystar.value.Value;
import alchemystar.value.ValueBoolean;
import alchemystar.value.ValueLong;
import alchemystar.value.ValueString;

/**
 * 简单的SQL Parser 不支持Join
 *
 * @Author lizhuyang
 */
public class Parser {

    // used during the tokenizer phase
    private static final int CHAR_END = 1, CHAR_VALUE = 2, CHAR_QUOTED = 3;
    private static final int CHAR_NAME = 4, CHAR_SPECIAL_1 = 5, CHAR_SPECIAL_2 = 6;
    private static final int CHAR_STRING = 7, CHAR_DOT = 8, CHAR_DOLLAR_QUOTED_STRING = 9;

    // this are token types
    private static final int KEYWORD = 1, IDENTIFIER = 2, PARAMETER = 3, END = 4, VALUE = 5;
    private static final int EQUAL = 6, BIGGER_EQUAL = 7, BIGGER = 8;
    private static final int SMALLER = 9, SMALLER_EQUAL = 10, NOT_EQUAL = 11, AT = 12;
    private static final int MINUS = 13, PLUS = 14, STRING_CONCAT = 15;
    private static final int OPEN = 16, CLOSE = 17, NULL = 18, TRUE = 19, FALSE = 20;
    private static final int CURRENT_TIMESTAMP = 21, CURRENT_DATE = 22, CURRENT_TIME = 23, ROWNUM = 24;

    private int currentTokenType;
    private String currentToken;
    private String currentOriginToken;
    private boolean currentTokenQuoted;
    private Value currentValue;
    private ArrayList<String> expectedList;
    private int[] characterTypes;
    private String originalSQL;
    private String sqlCommand;
    private char[] sqlCommandChars;
    private Database database;

    private int parseIndex;
    private int lastParseIndex;

    private Session session;

    Select currentSelect = null;
    Prepared currentPrepared = null;

    private String schemaName;

    private final boolean identifiersToUpper = true;

    public Parser(Session session) {
        database = session.getDatabase();
        this.session = session;
    }

    public Prepared parse(String sql) {
        initialize(sql);
        currentSelect = null;
        currentPrepared = null;
        read();
        return parsePrepared();
    }

    private Prepared parsePrepared() {
        int start = lastParseIndex;
        Prepared c = null;
        String token = currentToken;
        if (token.length() == 0) {
            c = new NoOperation(session);
        } else {
            char first = token.charAt(0);
            switch (first) {
                case 'c':
                case 'C':
                    if (readIf("CREATE")) {
                        c = parseCreate();
                    }
                case 's':
                case 'S':
                    if (isToken("SELECT")) {
                        c = parseSelect();
                    }
                    break;
                default:
                    throw new RuntimeException("Not Support This statement,sqlCommand=" + originalSQL);
            }
        }
        return c;
    }

    // Not Support UNION Now
    private Query parseSelect() {
        Query command = parseSelectSimple();
        command.init();
        return command;
    }

    Select parseSelectSimple() {
        if (!readIf("SELECT")) {
            throw new RuntimeException("Parse error,NOT select first");
        }
        Select command = new Select(session);
        int start = lastParseIndex;
        Select oldSelect = currentSelect;
        currentSelect = command;
        currentPrepared = command;
        parseSelectSimpleSelectPart(command);
        // NOT SUPPORT DUAL
        if (!readIf("FROM")) {
            throw new RuntimeException("Syntax error , from miss");
        }
        parseSelectSimpleFromPart(command);
        if (readIf("WHERE")) {
            Expression condition = readExpression();
            command.addCondition(condition);
        }
        parseEndOfQuery(command);
        // NOT SUPPORT GROUP HAVING SELECT
        return command;

    }

    private void parseEndOfQuery(Query command) {
        if (readIf("LIMIT")) {
            Select temp = currentSelect;
            currentSelect = null;
            // limit 0,100=>read expression(0)
            Expression limit = readExpression();
            command.setLimit(limit);
            if (readIf(",")) {
                Expression offset = limit;
                // read expression(100)
                limit = readExpression();
                command.setOffset(offset);
                command.setLimit(limit);
            }
            currentSelect = temp;
        }

    }

    private void parseSelectSimpleSelectPart(Select command) {
        Select temp = currentSelect;
        currentSelect = null;
        // not support , DISTINCT & ALL
        ArrayList<Expression> expressions = new ArrayList<Expression>();
        do {
            if (readIf("*")) {
                // 执行时刻转译
                expressions.add(new WildCard(null));
            } else {
                Expression expr = readExpression();
                expressions.add(expr);
            }
        } while (readIf(","));
        command.setExpressions(expressions);
    }

    private void parseSelectSimpleFromPart(Select command) {
        do {
            TableFilter filter = readTableFilter(false);
            parseJoinTableFilter(filter, command);
        } while (readIf(","));
    }

    private void parseJoinTableFilter(TableFilter top, final Select command) {
        // top
        top = readJoin(top, command, false);
        command.addTableFilter(top, true);
        while (true) {
            // No nestedJoin
            TableFilter join = top.getJoin();
            if (join == null) {
                break;
            }
            // No Outer here
            Expression on = join.getJoinCondition();
            if (on != null) {
                command.addCondition(on);
            }
            join.removeJoinCondition();
            top.removeJoin();
            command.addTableFilter(join, true);
            top = join;
        }
    }

    private TableFilter readJoin(TableFilter top, Select command, boolean fromOuter) {
        // 不支持nested join
        boolean joined = false;
        // 只支持inner join
        while (true) {
            if (readIf("INNER")) {
                read("JOIN");
                joined = true;
                TableFilter join = readTableFilter(fromOuter);
                // a join b join c join d joine ......
                top = readJoin(top, command, false);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                top.addJoin(join, fromOuter, on);
            } else if (readIf("JOIN")) {
                joined = true;
                TableFilter join = readTableFilter(fromOuter);
                top = readJoin(top, command, false);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                top.addJoin(join, fromOuter, on);
            } else {
                break;
            }
        }
        return top;
    }

    private Prepared parseCreate() {
        if (readIf("TABLE")) {
            return parseCreateTable();
        }
        throw new RuntimeException("Syntax error,table must after create");
    }

    private CreateTable parseCreateTable() {
        boolean ifNotExists = readIfNoExists();
        String tableName = readIdentifierWithSchema();
        Schema schema = getSchema();
        CreateTable command = new CreateTable(session, schema);
        command.setTableName(tableName);
        command.setIfNotExists(ifNotExists);
        if (readIf("(")) {
            if (!readIf(")")) {
                do {
                    // No Alter Here
                    String columnName = readColumnIdentifier();
                    Column column = parseColumnForTable(columnName, true);
                    // Not AutoIncrement, No primaryKey
                    command.addColumn(column);
                    if (readIf("NOT")) {
                        read("NULL");
                        column.setNullable(false);
                    } else {
                        readIf("NULL");
                    }
                } while (readIfMore());
            }
        }
        if (readIf("ENGINE")) {
            read("=");
            command.setTableEngine(readString());
        }
        if (readIf("SEP") || readIf("SEPERATOR")) {
            read("=");
            String seperator = readString();
            // 对|做特殊处理
            if (seperator.equals("|")) {
                seperator = "\\" + seperator;
            }
            command.setSeperator(seperator);
        }
        if (readIf("SKIPWRONG")) {
            read("=");
            String state = readString().toUpperCase();
            if (state.equals("TRUE")) {
                command.setWrongSkip(true);
            } else if (state.equals("FALSE")) {
                command.setWrongSkip(false);
            } else {
                throw new RuntimeException("WRONGSKIP only can be TRUE or FALSE");
            }
        }
        if (readIf("HIDDEN")) {
            command.setHidden(true);
        }
        if (readIf("AS")) {
            // todo 支持用select 渲染表
            command.setQuery(parseSelect());
        }
        command.setComment(readCommentIf());
        return command;
    }

    private boolean readIfMore() {
        if (readIf(",")) {
            return !readIf(")");
        }
        read(")");
        return false;
    }

    private String readCommentIf() {
        if (readIf("COMMENT")) {
            readIf("=");
            return readString();
        }
        return null;
    }

    private String readUniqueIdentifier() {
        return readColumnIdentifier();
    }

    private Column parseColumnForTable(String columnName, boolean defaultNullable) {
        Column column;
        column = parseColumnWithType(columnName);
        if (readIf("NOT")) {
            read("NULL");
            column.setNullable(false);
        } else if (readIf("NULL")) {
            column.setNullable(true);
        }
        // 默认nullable为false
        if (readIf("DEFAULT")) {
            Expression defaultExpression = readExpression();
            column.setDefaultExpression(defaultExpression);
        }
        // 重复的NOT NULL,保证在前或在后都能解析到
        if (readIf("NOT")) {
            read("NULL");
            column.setNullable(false);
        } else {
            readIf("NULL");
        }
        // Not AutoIncrement
        String comment = readCommentIf();
        if (comment != null) {
            column.setComment(comment);
        }
        return column;
    }

    private Column parseColumnWithType(String columnName) {
        String original = currentToken;
        if (!identifiersToUpper) {
            original = original.toUpperCase();
        }
        // To the next token
        read();
        // No UserDataType Support
        int dataType = DataType.getTypeByName(original);
        // No Support bigint(10),unsigned
        Column column = new Column(dataType, columnName);
        column.setOriginalSQL(original);
        return column;
    }

    private String readString() {
        Expression expr = readExpression();
        if (!(expr instanceof ValueExpression)) {
            throw new RuntimeException("Expr not ValueExpression");
        }
        String s = expr.getValue(session).getString();
        return s;
    }

    private String readIdentifierWithSchema() {
        return readIdentifierWithSchema(session.getCurrentSchemaName());
    }

    private TableFilter readTableFilter(boolean fromOuter) {
        Table table;
        String alias = null;
        // Not Support (select)
        String tableName = readIdentifierWithSchema(null);
        table = readTableOrView(tableName);
        alias = readFromAlias(alias);
        return new TableFilter(session, table, alias, currentSelect);
    }

    private Expression readExpression() {
        Expression r = readAnd();
        // 如果遇到or,则建立一个or condition,然后再readAnd
        while (readIf("OR")) {
            r = new ConditionAndOr(ConditionAndOr.OR, r, readAnd());
        }
        return r;
    }

    private Expression readAnd() {
        Expression r = readCondition();
        // if 一直是 and ,则一直递归下去
        while (readIf("AND")) {
            r = new ConditionAndOr(ConditionAndOr.AND, r, readCondition());
        }
        return r;
    }

    private boolean readIfNoExists() {
        if (readIf("IF")) {
            read("NOT");
            read("EXISTS");
            return true;
        }
        return false;
    }

    private Expression readCondition() {
        // Not Support NOT Condition
        // Not Support EXISTS Condition
        Expression r = readConcat();
        while (true) {
            // 支持IN,但不支持IN内子查询
            if (readIf("IN")) {
                read("(");
                if (readIf(")")) {
                    r = ValueExpression.get(ValueBoolean.get(false));
                } else {
                    if (isSelect()) {
                        throw new RuntimeException("Not Support SubQuery");
                    } else {
                        ArrayList<Expression> v = new ArrayList<Expression>();
                        Expression last;
                        do {
                            last = readExpression();
                            v.add(last);
                        } while (readIf(","));
                        r = new ConditionIn(r, v);
                    }
                    read(")");
                }
            } else if (readIf("BETWEEN")) {
                Expression low = readConcat();
                read("AND");
                Expression high = readConcat();
                Expression condLow = new Comparison(session, Comparison.SMALLER_EQUAL, low, r);
                Expression condHigh = new Comparison(session, Comparison.BIGGER_EQUAL, high, r);
                r = new ConditionAndOr(ConditionAndOr.AND, condLow, condHigh);
            } else {
                int compareType = getCompareType(currentTokenType);
                if (compareType < 0) {
                    break;
                }
                read();
                Expression right = readConcat();
                r = new Comparison(session, compareType, r, right);
            }
        }
        return r;
    }

    private static int getCompareType(int tokenType) {
        switch (tokenType) {
            case EQUAL:
                return Comparison.EQUAL;
            case BIGGER_EQUAL:
                return Comparison.BIGGER_EQUAL;
            case BIGGER:
                return Comparison.BIGGER;
            case SMALLER:
                return Comparison.SMALLER;
            case SMALLER_EQUAL:
                return Comparison.SMALLER_EQUAL;
            case NOT_EQUAL:
                return Comparison.NOT_EQUAL;
            default:
                return -1;
        }
    }

    private void read(String expected) {
        if (currentTokenQuoted || !equalsToken(expected, currentToken)) {
            addExpected(expected);
            throw new RuntimeException("Syntax Error");
        }
        read();
    }

    private Expression readConcat() {
        // + - 是较低优先级,=是最低优先级
        Expression r = readSum();
        while (true) {
            if (readIf("||")) {
                r = new Operation(Operation.CONCAT, r, readSum());
            } else {
                return r;
            }
        }

    }

    private Expression readSum() {
        Expression r = readFactor();
        while (true) {
            if (readIf("+")) {
                r = new Operation(Operation.PLUS, r, readFactor());
            } else if (readIf("-")) {
                r = new Operation(Operation.MINUS, r, readFactor());
            } else {
                return r;
            }
        }
    }

    // 利用reactor和term以解决优先级问题
    private Expression readFactor() {
        Expression r = readTerm();
        while (true) {
            if (readIf("*")) {
                r = new Operation(Operation.MULTIPLY, r, readTerm());
            } else if (readIf("/")) {
                r = new Operation(Operation.DIVIDE, r, readTerm());
            } else if (readIf("%")) {
                r = new Operation(Operation.MODULUS, r, readTerm());
            } else {
                return r;
            }
        }
    }

    private Expression readTerm() {
        Expression r;
        switch (currentTokenType) {
            //NOT SUPPORT @
            //NOT SUPPORT ? $
            //NOT SUPPORT SubQuery
            case IDENTIFIER:
                String name = currentToken;
                read();
                if (readIf(".")) {
                    r = readTermObjectDot(name);
                } else {
                    r = new ExpressionColumn(null, name);
                }
                break;
            case MINUS:
                read();
                if (currentTokenType == VALUE) {
                    r = ValueExpression.get(currentValue.negate());
                    read();
                } else {
                    r = new Operation(Operation.NEGATE, readTerm(), null);
                }
                break;
            case PLUS:
                read();
                r = readTerm();
                break;
            case TRUE:
                read();
                r = ValueExpression.get(ValueBoolean.get(true));
                break;
            case FALSE:
                read();
                r = ValueExpression.get(ValueBoolean.get(false));
                break;
            case NULL:
                read();
                r = ValueExpression.getNull();
                break;
            case VALUE:
                r = ValueExpression.get(currentValue);
                read();
                break;
            case OPEN:
                read();
                if (readIf(")")) {
                    r = new ExpressionList(new Expression[0]);
                } else {
                    r = readExpression();
                    if (readIf(",")) {
                        ArrayList<Expression> list = new ArrayList<Expression>();
                        list.add(r);
                        while (!readIf(")")) {
                            r = readExpression();
                            list.add(r);
                            if (!readIf(",")) {
                                read(")");
                                break;
                            }
                        }
                        Expression[] array = new Expression[list.size()];
                        list.toArray(array);
                        r = new ExpressionList(array);
                    } else {
                        read(")");
                    }
                }
                break;
            default:
                throw new RuntimeException("Syntax Error");

        }
        return r;

    }

    private Expression readTermObjectDot(String objectName) {
        String name = readColumnIdentifier();
        return new ExpressionColumn(objectName, name);
    }

    private String readIdentifierWithSchema(String defaultSchemaName) {
        if (currentTokenType != IDENTIFIER) {
            throw new RuntimeException("currentTokenType not IDENTIFIER");
        }
        String s = currentToken;
        read();
        schemaName = defaultSchemaName;
        if (readIf(".")) {
            schemaName = s;
            if (currentTokenType != IDENTIFIER) {
                throw new RuntimeException("currentTokenType not IDENTIFIER");
            }
            s = currentToken;
            read();
        }
        return s;
    }

    private String readFromAlias(String alias) {
        if (readIf("AS")) {
            alias = readAliasIdentifier();
        } else if (currentTokenType == IDENTIFIER) {
            // left and right are not keywords (because they are functions as
            // well)
            if (!isToken("LEFT") && !isToken("RIGHT") && !isToken("FULL")) {
                alias = readAliasIdentifier();
            }
        }
        return alias;
    }

    private String readAliasIdentifier() {
        return readColumnIdentifier();
    }

    private String readColumnIdentifier() {
        if (currentTokenType != IDENTIFIER) {
            throw new RuntimeException("Syntax Error");
        }
        String s = currentToken;
        read();
        return s;
    }

    private Table readTableOrView(String tableName) {
        if (schemaName != null) {
            return getSchema().getTableOrView(tableName);
        }
        Table table = session.getCurrentSchema().getTableOrView(tableName);
        if (table != null) {
            return table;
        }
        throw new RuntimeException("Table Or View doesn't found");
    }

    private boolean isSelect() {
        int start = lastParseIndex;
        while (readIf("(")) {
            // need to read ahead, it could be a nested union:
            // ((select 1) union (select 1))
        }
        boolean select = isToken("SELECT") || isToken("FROM");
        parseIndex = start;
        read();
        return select;
    }

    private boolean readIf(String token) {
        if (!currentTokenQuoted && equalsToken(token, currentToken)) {
            read();
            return true;
        }
        addExpected(token);
        return false;
    }

    private Schema getSchema() {
        return getSchema(schemaName);
    }

    private Schema getSchema(String schemaName) {
        if (schemaName == null) {
            return null;
        }
        Schema schema = database.findSchema(schemaName);
        if (schema == null) {
            schema = database.findSchema(session.getCurrentSchemaName());
            if (schema == null) {
                throw new RuntimeException("can't find schema:" + schema);
            }
        }
        return schema;
    }

    private void initialize(String sql) {
        if (sql == null) {
            sql = "";
        }
        originalSQL = sql;
        sqlCommand = sql;
        int len = sql.length() + 1;
        char[] command = new char[len];
        int[] types = new int[len];
        len--;
        sql.getChars(0, len, command, 0);
        boolean changed = false;
        command[len] = ' ';
        int startLoop = 0;
        int lastType = 0;
        for (int i = 0; i < len; i++) {
            char c = command[i];
            int type = 0;
            switch (c) {
                case '/':
                    if (command[i + 1] == '*') {
                        // block comment
                        changed = true;
                        command[i] = ' ';
                        command[i + 1] = ' ';
                        startLoop = i;
                        i += 2;
                        checkRunOver(i, len, startLoop);
                        while (command[i] != '*' || command[i + 1] != '/') {
                            command[i++] = ' ';
                            checkRunOver(i, len, startLoop);
                        }
                        command[i] = ' ';
                        command[i + 1] = ' ';
                        i++;
                    } else if (command[i + 1] == '/') {
                        // single line comment
                        changed = true;
                        startLoop = i;
                        while (true) {
                            c = command[i];
                            if (c == '\n' || c == '\r' || i >= len - 1) {
                                break;
                            }
                            command[i++] = ' ';
                            checkRunOver(i, len, startLoop);
                        }
                    } else {
                        type = CHAR_SPECIAL_1;
                    }
                    break;
                case '-':
                    if (command[i + 1] == '-') {
                        // single line comment
                        changed = true;
                        startLoop = i;
                        while (true) {
                            c = command[i];
                            if (c == '\n' || c == '\r' || i >= len - 1) {
                                break;
                            }
                            command[i++] = ' ';
                            checkRunOver(i, len, startLoop);
                        }
                    } else {
                        type = CHAR_SPECIAL_1;
                    }
                    break;
                case '$':
                    if (command[i + 1] == '$' && (i == 0 || command[i - 1] <= ' ')) {
                        // dollar quoted string
                        changed = true;
                        command[i] = ' ';
                        command[i + 1] = ' ';
                        startLoop = i;
                        i += 2;
                        checkRunOver(i, len, startLoop);
                        while (command[i] != '$' || command[i + 1] != '$') {
                            types[i++] = CHAR_DOLLAR_QUOTED_STRING;
                            checkRunOver(i, len, startLoop);
                        }
                        command[i] = ' ';
                        command[i + 1] = ' ';
                        i++;
                    } else {
                        if (lastType == CHAR_NAME || lastType == CHAR_VALUE) {
                            // $ inside an identifier is supported
                            type = CHAR_NAME;
                        } else {
                            // but not at the start, to support PostgreSQL $1
                            type = CHAR_SPECIAL_1;
                        }
                    }
                    break;
                case '(':
                case ')':
                case '{':
                case '}':
                case '*':
                case ',':
                case ';':
                case '+':
                case '%':
                case '?':
                case '@':
                case ']':
                    type = CHAR_SPECIAL_1;
                    break;
                case '!':
                case '<':
                case '>':
                case '|':
                case '=':
                case ':':
                case '~':
                    type = CHAR_SPECIAL_2;
                    break;
                case '.':
                    type = CHAR_DOT;
                    break;
                case '\'':
                    type = types[i] = CHAR_STRING;
                    startLoop = i;
                    while (command[++i] != '\'') {
                        checkRunOver(i, len, startLoop);
                    }
                    break;
                case '`':
                    // MySQL alias for ", but not case sensitive
                    command[i] = '"';
                    changed = true;
                    type = types[i] = CHAR_QUOTED;
                    startLoop = i;
                    while (command[++i] != '`') {
                        checkRunOver(i, len, startLoop);
                        c = command[i];
                        command[i] = Character.toUpperCase(c);
                    }
                    command[i] = '"';
                    break;
                case '\"':
                    type = types[i] = CHAR_QUOTED;
                    startLoop = i;
                    while (command[++i] != '\"') {
                        checkRunOver(i, len, startLoop);
                    }
                    break;
                case '_':
                    type = CHAR_NAME;
                    break;
                default:
                    if (c >= 'a' && c <= 'z') {
                       /* if (identifiersToUpper) {
                            command[i] = (char) (c - ('a' - 'A'));
                            changed = true;
                        }*/
                        type = CHAR_NAME;
                    } else if (c >= 'A' && c <= 'Z') {
                        type = CHAR_NAME;
                    } else if (c >= '0' && c <= '9') {
                        type = CHAR_VALUE;
                    } else {
                        if (c <= ' ' || Character.isSpaceChar(c)) {
                            // whitespace
                        } else if (Character.isJavaIdentifierPart(c)) {
                            type = CHAR_NAME;
                            if (identifiersToUpper) {
                                char u = Character.toUpperCase(c);
                                if (u != c) {
                                    command[i] = u;
                                    changed = true;
                                }
                            }
                        } else {
                            type = CHAR_SPECIAL_1;
                        }
                    }
            }
            types[i] = type;
            lastType = type;
        }
        sqlCommandChars = command;
        types[len] = CHAR_END;
        characterTypes = types;
        if (changed) {
            sqlCommand = new String(command);
        }
        parseIndex = 0;
    }

    private void checkRunOver(int i, int len, int startLoop) {
        if (i >= len) {
            parseIndex = startLoop;
            throw new RuntimeException("Syntax error");
        }
    }

    // 分词
    private void read() {
        currentTokenQuoted = false;
        if (expectedList != null) {
            expectedList.clear();
        }
        int[] types = characterTypes;
        lastParseIndex = parseIndex;
        int i = parseIndex;
        int type = types[i];
        while (type == 0) {
            type = types[++i];
        }
        int start = i;
        char[] chars = sqlCommandChars;
        char c = chars[i++];
        currentToken = "";
        switch (type) {
            case CHAR_NAME:
                while (true) {
                    type = types[i];
                    if (type != CHAR_NAME && type != CHAR_VALUE) {
                        break;
                    }
                    i++;
                }
                currentToken = sqlCommand.substring(start, i);
                currentTokenType = getTokenType(currentToken);
                parseIndex = i;
                return;
            case CHAR_QUOTED: {
                String result = null;
                while (true) {
                    for (int begin = i; ; i++) {
                        if (chars[i] == '\"') {
                            if (result == null) {
                                result = sqlCommand.substring(begin, i);
                            } else {
                                result += sqlCommand.substring(begin - 1, i);
                            }
                            break;
                        }
                    }
                    if (chars[++i] != '\"') {
                        break;
                    }
                    i++;
                }
                currentToken = result;
                parseIndex = i;
                currentTokenQuoted = true;
                currentTokenType = IDENTIFIER;
                return;
            }
            case CHAR_SPECIAL_2:
                if (types[i] == CHAR_SPECIAL_2) {
                    i++;
                }
                currentToken = sqlCommand.substring(start, i);
                currentTokenType = getSpecialType(currentToken);
                parseIndex = i;
                return;
            case CHAR_SPECIAL_1:
                currentToken = sqlCommand.substring(start, i);
                currentTokenType = getSpecialType(currentToken);
                parseIndex = i;
                return;
            case CHAR_VALUE:
                if (c == '0' && chars[i] == 'X') {
                    // hex number
                    long number = 0;
                    start += 2;
                    i++;
                    while (true) {
                        c = chars[i];
                        if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                            currentValue = ValueLong.get(number);
                            currentTokenType = VALUE;
                            currentToken = "0";
                            parseIndex = i;
                            return;
                        }
                        number = (number << 4) + c - (c >= 'A' ? ('A' - 0xa) : ('0'));
                        if (number > Integer.MAX_VALUE) {
                            throw new RuntimeException("Not suppor Demical");
                        }
                        i++;
                    }
                }
                long number = c - '0';
                while (true) {
                    c = chars[i];
                    if (c < '0' || c > '9') {
                        if (c == '.') {
                            throw new RuntimeException("Not support Demical");
                        }
                        if (c == 'E') {
                            throw new RuntimeException("Not support Demical");
                        }
                        currentValue = ValueLong.get(number);
                        currentTokenType = VALUE;
                        currentToken = "0";
                        parseIndex = i;
                        break;
                    }
                    number = number * 10 + (c - '0');
                    if (number > Integer.MAX_VALUE) {
                        throw new RuntimeException("Not support Demical");
                    }
                    i++;
                }
                return;
            case CHAR_DOT:
                if (types[i] != CHAR_VALUE) {
                    currentTokenType = KEYWORD;
                    currentToken = ".";
                    parseIndex = i;
                    return;
                }
                throw new RuntimeException("Not support Demical");
            case CHAR_STRING: {
                String result = null;
                while (true) {
                    for (int begin = i; ; i++) {
                        if (chars[i] == '\'') {
                            if (result == null) {
                                result = sqlCommand.substring(begin, i);
                            } else {
                                result += sqlCommand.substring(begin - 1, i);
                            }
                            break;
                        }
                    }
                    if (chars[++i] != '\'') {
                        break;
                    }
                    i++;
                }
                currentToken = "'";
                currentValue = ValueString.get(result);
                parseIndex = i;
                currentTokenType = VALUE;
                return;
            }
            case CHAR_DOLLAR_QUOTED_STRING: {
                String result = null;
                int begin = i - 1;
                while (types[i] == CHAR_DOLLAR_QUOTED_STRING) {
                    i++;
                }
                result = sqlCommand.substring(begin, i);
                currentToken = "'";
                currentValue = ValueString.get(result);
                parseIndex = i;
                currentTokenType = VALUE;
                return;
            }
            case CHAR_END:
                currentToken = "";
                currentTokenType = END;
                parseIndex = i;
                return;
            default:
                throw new RuntimeException("Syntax error");
        }
    }

    private int getTokenType(String s) {
        int len = s.length();
        if (len == 0) {
            throw new RuntimeException("Syntax error");
        }
        // if (!identifiersToUpper) {
        // if not yet converted to uppercase, do it now
        //     s = s.toUpperCase(Locale.ENGLISH);
        // }
        return getSaveTokenType(s, false);
    }

    private static int getSaveTokenType(String s, boolean supportOffsetFetch) {
        switch (s.charAt(0)) {
            case 'C':
                if (s.equals("CURRENT_TIMESTAMP")) {
                    return CURRENT_TIMESTAMP;
                } else if (s.equals("CURRENT_TIME")) {
                    return CURRENT_TIME;
                } else if (s.equals("CURRENT_DATE")) {
                    return CURRENT_DATE;
                }
                return getKeywordOrIdentifier(s, "CROSS", KEYWORD);
            case 'D':
                return getKeywordOrIdentifier(s, "DISTINCT", KEYWORD);
            case 'E':
                if ("EXCEPT".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "EXISTS", KEYWORD);
            case 'F':
                if ("FROM".equals(s)) {
                    return KEYWORD;
                } else if ("FOR".equals(s)) {
                    return KEYWORD;
                } else if ("FULL".equals(s)) {
                    return KEYWORD;
                } else if (supportOffsetFetch && "FETCH".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "FALSE", FALSE);
            case 'G':
                return getKeywordOrIdentifier(s, "GROUP", KEYWORD);
            case 'H':
                return getKeywordOrIdentifier(s, "HAVING", KEYWORD);
            case 'I':
                if ("INNER".equals(s)) {
                    return KEYWORD;
                } else if ("INTERSECT".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "IS", KEYWORD);
            case 'J':
                return getKeywordOrIdentifier(s, "JOIN", KEYWORD);
            case 'L':
                if ("LIMIT".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "LIKE", KEYWORD);
            case 'M':
                return getKeywordOrIdentifier(s, "MINUS", KEYWORD);
            case 'N':
                if ("NOT".equals(s)) {
                    return KEYWORD;
                } else if ("NATURAL".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "NULL", NULL);
            case 'O':
                if ("ON".equals(s)) {
                    return KEYWORD;
                } else if (supportOffsetFetch && "OFFSET".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "ORDER", KEYWORD);
            case 'P':
                return getKeywordOrIdentifier(s, "PRIMARY", KEYWORD);
            case 'R':
                return getKeywordOrIdentifier(s, "ROWNUM", ROWNUM);
            case 'S':
                if (s.equals("SYSTIMESTAMP")) {
                    return CURRENT_TIMESTAMP;
                } else if (s.equals("SYSTIME")) {
                    return CURRENT_TIME;
                } else if (s.equals("SYSDATE")) {
                    return CURRENT_TIMESTAMP;
                }
                return getKeywordOrIdentifier(s, "SELECT", KEYWORD);
            case 'T':
                if ("TODAY".equals(s)) {
                    return CURRENT_DATE;
                }
                return getKeywordOrIdentifier(s, "TRUE", TRUE);
            case 'U':
                if ("UNIQUE".equals(s)) {
                    return KEYWORD;
                }
                return getKeywordOrIdentifier(s, "UNION", KEYWORD);
            case 'W':
                return getKeywordOrIdentifier(s, "WHERE", KEYWORD);
            default:
                return IDENTIFIER;
        }
    }

    private static int getKeywordOrIdentifier(String s1, String s2, int keywordType) {
        if (s1.equals(s2)) {
            return keywordType;
        }
        return IDENTIFIER;
    }

    private int getSpecialType(String s) {
        char c0 = s.charAt(0);
        if (s.length() == 1) {
            switch (c0) {
                case '?':
                case '$':
                    return PARAMETER;
                case '@':
                    return AT;
                case '+':
                    return PLUS;
                case '-':
                    return MINUS;
                case '{':
                case '}':
                case '*':
                case '/':
                case '%':
                case ';':
                case ',':
                case ':':
                case '[':
                case ']':
                case '~':
                    return KEYWORD;
                case '(':
                    return OPEN;
                case ')':
                    return CLOSE;
                case '<':
                    return SMALLER;
                case '>':
                    return BIGGER;
                case '=':
                    return EQUAL;
                default:
                    break;
            }
        } else if (s.length() == 2) {
            switch (c0) {
                case ':':
                    if ("::".equals(s)) {
                        return KEYWORD;
                    } else if (":=".equals(s)) {
                        return KEYWORD;
                    }
                    break;
                case '>':
                    if (">=".equals(s)) {
                        return BIGGER_EQUAL;
                    }
                    break;
                case '<':
                    if ("<=".equals(s)) {
                        return SMALLER_EQUAL;
                    } else if ("<>".equals(s)) {
                        return NOT_EQUAL;
                    }
                    break;
                case '!':
                    if ("!=".equals(s)) {
                        return NOT_EQUAL;
                    } else if ("!~".equals(s)) {
                        return KEYWORD;
                    }
                    break;
                case '|':
                    if ("||".equals(s)) {
                        return STRING_CONCAT;
                    }
                    break;
            }
        }
        throw new RuntimeException("Syntax error");
    }

    private boolean isToken(String token) {
        boolean result = equalsToken(token, currentToken) && !currentTokenQuoted;
        if (result) {
            return true;
        }
        return false;
    }

    private boolean equalsToken(String a, String b) {
        if (a == null) {
            return b == null;
        } else if (a.equals(b)) {
            return true;
        } else if (a.equalsIgnoreCase(b)) {
            return true;
        }
        return false;
    }

    private void addExpected(String token) {
        if (expectedList != null) {
            expectedList.add(token);
        }
    }
}
