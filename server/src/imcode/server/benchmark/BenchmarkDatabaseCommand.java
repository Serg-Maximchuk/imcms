package imcode.server.benchmark;

import com.imcode.db.DatabaseCommand;
import com.imcode.db.DatabaseConnection;
import com.imcode.db.DatabaseException;
import com.imcode.db.ConnectionDatabaseConnection;
import com.imcode.db.commands.SqlQueryDatabaseCommand;
import org.apache.commons.lang.time.StopWatch;

class BenchmarkDatabaseCommand implements DatabaseCommand {

    private final DatabaseCommand databaseCommand;
    private BenchmarkDatabase benchmarkDatabase;

    BenchmarkDatabaseCommand(BenchmarkDatabase benchmarkDatabase, DatabaseCommand databaseCommand) {
        this.benchmarkDatabase = benchmarkDatabase;
        this.databaseCommand = databaseCommand;
    }

    public Object executeOn(final DatabaseConnection connection) throws DatabaseException {
        return databaseCommand.executeOn(new BenchmarkDatabaseConnection(benchmarkDatabase, connection));
    }

}
