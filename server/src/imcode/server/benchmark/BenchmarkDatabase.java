package imcode.server.benchmark;

import com.imcode.db.Database;
import com.imcode.db.DatabaseCommand;
import com.imcode.db.DatabaseException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;

public class BenchmarkDatabase implements Database {

    private HashMap averagesMap = new HashMap() ;

    private final Database database;

    public BenchmarkDatabase(Database database) {
        this.database = database;
    }

    public Object execute(final DatabaseCommand databaseCommand) throws DatabaseException {
        return database.execute(new BenchmarkDatabaseCommand(this, databaseCommand)) ;
    }

    BenchmarkAverages getAverages(String sql) {
        BenchmarkAverages averages = (BenchmarkAverages) averagesMap.get(sql);
        if (null == averages ) {
            averages = new BenchmarkAverages();
            averagesMap.put(sql, averages) ;
        }
        return averages;
    }

    public Object executeCommand(DatabaseCommand databaseCommand) throws DatabaseException {
        return execute(databaseCommand) ;
    }

    public Map getBenchmarks() {
        return (Map) averagesMap.clone();
    }

}
