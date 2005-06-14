package imcode.server.db;

import imcode.server.db.exceptions.DatabaseException;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;

public class DatabaseConnectionWrapper implements DatabaseConnection {

    private DatabaseConnection databaseConnection ;

    public DatabaseConnectionWrapper(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public int executeUpdate( String sql, Object[] parameters ) throws DatabaseException {
        return databaseConnection.executeUpdate(sql, parameters);
    }

    public Number executeUpdateAndGetGeneratedKey( String sql, String[] parameters ) throws DatabaseException {
        return databaseConnection.executeUpdateAndGetGeneratedKey(sql, parameters);
    }

    public int executeUpdateProcedure( String procedure, String[] parameters ) throws DatabaseException {
        return databaseConnection.executeUpdateProcedure(procedure, parameters);
    }

    public Object executeQuery( String sqlQuery, String[] parameters,
                                 ResultSetHandler resultSetHandler ) throws DatabaseException {
        return databaseConnection.executeQuery(sqlQuery, parameters, resultSetHandler);
    }

    public Object executeProcedure( String procedure, String[] params, ResultSetHandler resultSetHandler ) throws DatabaseException {
        return databaseConnection.executeProcedure(procedure, params, resultSetHandler);
    }

    public Connection getConnection() {
        return databaseConnection.getConnection();
    }
}