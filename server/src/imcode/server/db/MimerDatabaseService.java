package imcode.server.db;

import org.apache.log4j.Logger;
import imcode.server.db.sql.SQLTransaction;
import imcode.server.db.sql.TransactionContent;

import java.sql.SQLException;

/**
 * Mimer transaction differ from many others, they uses optimistic transaction management, witch meens that
 * deadlock can't happend, but instead they throw Exceptions when to transaction clashes, 
 * see http://developer.mimer.com/features/feature_12.htm
 */
public class MimerDatabaseService extends DatabaseService {

    public MimerDatabaseService( String hostName, Integer port, String databaseName, String user, String password, Integer maxConnectionCount ) {
        super( Logger.getLogger( MimerDatabaseService.class ) );

        // log.debug( "Creating a 'Mimer' database service");
        String jdbcDriver = "com.mimer.jdbc.Driver";
        String jdbcUrl = "jdbc:mimer://";
        String serverUrl = jdbcUrl + hostName + ":" + port + "/" + databaseName;
        String serverName = "Mimer test server";

        super.initConnectionPoolAndSQLProcessor( serverName, jdbcDriver, serverUrl, user, password, maxConnectionCount );

        // This is only needed to be done the first time
        // sqlProcessor.executeUpdate("CREATE DATABANK " + databaseName , null);
    }

    void backup( final String fullPathToBackupFile ) {
        final SQLTransaction transaction = sqlProcessor.createNewTransaction();
        transaction.executeAndCommit( new TransactionContent() {
            public void execute() throws SQLException {
                transaction.executeUpdate( "START BACKUP", null );
                transaction.executeUpdate( "CREATE BACKUP IN '" + fullPathToBackupFile + "' FOR DATABANK test", null );
                transaction.executeUpdate( "COMMIT BACKUP", null );
            }
        } );
    }

    /*
    Not working, but I'm tired, whaiting a few days before desiding on if this is needed.
    void restore( String fullPathToDatabaseFile, String fullPathToBackupFile )  throws IOException {
        SQLProcessor.SQLTransaction transaction = sqlProcessor.createNewTransaction();
        sqlProcessor.executeUpdate( "SET DATABASE OFFLINE", null );
        copyFile( fullPathToDatabaseFile, fullPathToBackupFile );
        sqlProcessor.executeUpdate( "ALTER DATABANK test RESTORE USING '" + fullPathToDatabaseFile + "'", null );
        transaction.commit();
        sqlProcessor.executeUpdate( "SET DATABASE ONLINE RESET LOG", null );
    }

    private void copyFile( String to, String from ) throws IOException {
        // Create channel on the source
        FileChannel srcChannel = new FileInputStream(from).getChannel();

        // Create channel on the destination
        FileChannel dstChannel = new FileOutputStream(to).getChannel();

        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        // Close the channels
        srcChannel.close();
        dstChannel.close();
    }
    */
}
