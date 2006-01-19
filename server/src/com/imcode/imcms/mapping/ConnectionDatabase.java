package com.imcode.imcms.mapping;

import com.imcode.db.Database;
import com.imcode.db.DatabaseConnection;
import com.imcode.db.DatabaseCommand;
import com.imcode.db.DatabaseException;

class ConnectionDatabase implements Database {

    private final DatabaseConnection connection;

    public ConnectionDatabase(DatabaseConnection connection) {
        this.connection = connection;
    }

    public Object execute(DatabaseCommand databaseCommand) throws DatabaseException {
        return databaseCommand.executeOn(connection) ;
    }

    public Object executeCommand(DatabaseCommand databaseCommand) throws DatabaseException {
        return execute(databaseCommand) ;
    }
}
