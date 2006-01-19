package imcode.server.benchmark;

import imcode.util.jdbc.PreparedStatementWrapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

class BenchmarkPreparedStatement extends PreparedStatementWrapper {

    private final String sql;
    private BenchmarkDatabase benchmarkDatabase;

    BenchmarkPreparedStatement(BenchmarkDatabase benchmarkDatabase, PreparedStatement preparedStatement, String sql) {
        super(preparedStatement);
        this.benchmarkDatabase = benchmarkDatabase;
        this.sql = sql;
    }

    public ResultSet executeQuery() throws SQLException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ResultSet resultSet = super.executeQuery();
        stopWatch.stop();
        long time = stopWatch.getTime();
        benchmarkDatabase.getAverages(sql).getQueryAverage().add(time, 1);
        return new BenchmarkResultSet(benchmarkDatabase, sql, resultSet);
    }

}
