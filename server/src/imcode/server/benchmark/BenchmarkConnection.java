package imcode.server.benchmark;

import imcode.util.jdbc.ConnectionWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

class BenchmarkConnection extends ConnectionWrapper {

    private BenchmarkDatabase benchmarkDatabase;

    private final static Logger log = Logger.getLogger(BenchmarkConnection.class);

    BenchmarkConnection(BenchmarkDatabase benchmarkDatabase, Connection connection) {
        super(connection);
        this.benchmarkDatabase = benchmarkDatabase;
    }

    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return new BenchmarkPreparedStatement(benchmarkDatabase, connection.prepareStatement(sql), sql);
    }
}
