package imcode.util.jdbc;

import imcode.util.jdbc.ResultSetWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CountingResultSet extends ResultSetWrapper {

    int rowCount;

    public CountingResultSet(ResultSet resultSet) {
        super(resultSet);
    }

    public boolean next() throws SQLException {
        boolean b = super.next();
        if (b) {
            ++rowCount ;
        }
        return b;
    }

    public int getRowCount() {
        return rowCount;
    }
}
