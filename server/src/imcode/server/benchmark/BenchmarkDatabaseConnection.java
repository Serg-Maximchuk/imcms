package imcode.server.benchmark;

import com.imcode.db.DatabaseConnection;
import com.imcode.db.DatabaseConnectionWrapper;
import com.imcode.db.DatabaseException;
import com.imcode.db.ConnectionDatabaseConnection;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.time.StopWatch;

import java.sql.Connection;

class BenchmarkDatabaseConnection extends DatabaseConnectionWrapper {

    private BenchmarkDatabase benchmarkDatabase;

    BenchmarkDatabaseConnection(final BenchmarkDatabase benchmarkDatabase, final DatabaseConnection connection) {
        super(new ConnectionDatabaseConnection(connection.getConnection()) {
            public Connection getConnection() {
                return new BenchmarkConnection(benchmarkDatabase, connection.getConnection());
            }
        });
        this.benchmarkDatabase = benchmarkDatabase ;
    }

    public Object executeQuery(String sqlQuery, Object[] parameters,
                               ResultSetHandler resultSetHandler) throws DatabaseException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = super.executeQuery(sqlQuery, parameters, resultSetHandler);
        stopWatch.stop();
        long time = stopWatch.getTime();
        benchmarkDatabase.getAverages(sqlQuery).getTotalAverage().add(time,1);
        return result ;
    }

}
