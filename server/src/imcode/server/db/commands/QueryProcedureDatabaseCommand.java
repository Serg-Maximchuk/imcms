package imcode.server.db.commands;

import imcode.server.db.DatabaseConnection;
import imcode.server.db.exceptions.DatabaseException;
import org.apache.commons.dbutils.ResultSetHandler;

public class QueryProcedureDatabaseCommand extends ProcedureDatabaseCommand {

    private final ResultSetHandler resultSetHandler;

    public QueryProcedureDatabaseCommand( String procedure, String[] params, ResultSetHandler resultSetHandler ) {
        super(procedure, params) ;
        this.resultSetHandler = resultSetHandler;
    }

    public Object executeOn( DatabaseConnection connection ) throws DatabaseException {
        return connection.executeProcedure( procedure, params, resultSetHandler );
    }
}