package imcode.server.benchmark;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;
import imcode.util.jdbc.CountingResultSet;

public class BenchmarkResultSet extends CountingResultSet {

    private BenchmarkDatabase benchmarkDatabase;
    private final String sql;
    private StopWatch stopWatch = new StopWatch();

    public BenchmarkResultSet(BenchmarkDatabase benchmarkDatabase, String sql, ResultSet resultSet) {
        super(resultSet);
        this.benchmarkDatabase = benchmarkDatabase ;
        this.sql = sql;
    }

    public boolean next() throws SQLException {
        if ( 0 == getRowCount() ) {
            stopWatch.start() ;
        }
        boolean b = super.next();
        if ( !b ) {
            stopWatch.stop();
            long time = stopWatch.getTime();
            benchmarkDatabase.getAverages(sql).getRowAverage().add(time, getRowCount()) ;
        }
        return b;
    }
}
