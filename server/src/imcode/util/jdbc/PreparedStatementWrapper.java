package imcode.util.jdbc;

import java.sql.*;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.net.URL;

public class PreparedStatementWrapper implements PreparedStatement {

    protected PreparedStatement preparedStatement ;

    public PreparedStatementWrapper(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    public void clearParameters() throws SQLException {
        preparedStatement.clearParameters();
    }

    public boolean execute() throws SQLException {
        return preparedStatement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    public void setArray(int i, Array x) throws SQLException {
        preparedStatement.setArray(i, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        preparedStatement.setBigDecimal(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        preparedStatement.setBlob(i, x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        preparedStatement.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        preparedStatement.setByte(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        preparedStatement.setBytes(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    public void setClob(int i, Clob x) throws SQLException {
        preparedStatement.setClob(i, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        preparedStatement.setDate(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        preparedStatement.setDate(parameterIndex, x, cal);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        preparedStatement.setDouble(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        preparedStatement.setFloat(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        preparedStatement.setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        preparedStatement.setLong(parameterIndex, x);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        preparedStatement.setNull(parameterIndex, sqlType);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        preparedStatement.setNull(paramIndex, sqlType, typeName);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        preparedStatement.setObject(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setRef(int i, Ref x) throws SQLException {
        preparedStatement.setRef(i, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        preparedStatement.setShort(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        preparedStatement.setString(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        preparedStatement.setTime(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        preparedStatement.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x, cal);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        preparedStatement.setURL(parameterIndex, x);
    }

    public void addBatch(String sql) throws SQLException {
        preparedStatement.addBatch(sql);
    }

    public void cancel() throws SQLException {
        preparedStatement.cancel();
    }

    public void clearBatch() throws SQLException {
        preparedStatement.clearBatch();
    }

    public void clearWarnings() throws SQLException {
        preparedStatement.clearWarnings();
    }

    public void close() throws SQLException {
        preparedStatement.close();
    }

    public boolean execute(String sql) throws SQLException {
        return preparedStatement.execute(sql);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return preparedStatement.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return preparedStatement.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return preparedStatement.execute(sql, columnNames);
    }

    public int[] executeBatch() throws SQLException {
        return preparedStatement.executeBatch();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return preparedStatement.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return preparedStatement.executeUpdate(sql);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return preparedStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return preparedStatement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return preparedStatement.executeUpdate(sql, columnNames);
    }

    public Connection getConnection() throws SQLException {
        return preparedStatement.getConnection();
    }

    public int getFetchDirection() throws SQLException {
        return preparedStatement.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return preparedStatement.getFetchSize();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return preparedStatement.getGeneratedKeys();
    }

    public int getMaxFieldSize() throws SQLException {
        return preparedStatement.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return preparedStatement.getMaxRows();
    }

    public boolean getMoreResults() throws SQLException {
        return preparedStatement.getMoreResults();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return preparedStatement.getMoreResults(current);
    }

    public int getQueryTimeout() throws SQLException {
        return preparedStatement.getQueryTimeout();
    }

    public ResultSet getResultSet() throws SQLException {
        return preparedStatement.getResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return preparedStatement.getResultSetConcurrency();
    }

    public int getResultSetHoldability() throws SQLException {
        return preparedStatement.getResultSetHoldability();
    }

    public int getResultSetType() throws SQLException {
        return preparedStatement.getResultSetType();
    }

    public int getUpdateCount() throws SQLException {
        return preparedStatement.getUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return preparedStatement.getWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        preparedStatement.setCursorName(name);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        preparedStatement.setEscapeProcessing(enable);
    }

    public void setFetchDirection(int direction) throws SQLException {
        preparedStatement.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        preparedStatement.setFetchSize(rows);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        preparedStatement.setMaxFieldSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        preparedStatement.setMaxRows(max);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        preparedStatement.setQueryTimeout(seconds);
    }
}
