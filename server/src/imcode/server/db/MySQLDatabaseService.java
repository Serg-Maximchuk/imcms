package imcode.server.db;

import java.io.File ;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MySQLDatabaseService extends DatabaseService {

    private static final String TEXT_TYPE_MY_SQL = "TEXT";
    private static final String CREATE_TABLE_STR = "CREATE TABLE";

    public MySQLDatabaseService( String hostName, Integer port, String databaseName, String user, String password, Integer maxConnectionCount  ) {
        super( Logger.getLogger( MySQLDatabaseService.class ) );
        // log.debug( "Creating a 'My SQL' database service");
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcUrl = "jdbc:mysql://";
        String serverUrl = jdbcUrl + hostName + ":" + port + "/" + databaseName;
        String serverName = "MySql test server";

        super.initConnectionPoolAndSQLProcessor( serverName, jdbcDriver, serverUrl, user, password, maxConnectionCount );
    }

    ArrayList filterInsertCommands( ArrayList commands ) {
        commands = changeCharInCurrentTimestampCast( commands );
        return commands;
    }

    private ArrayList changeCharInCurrentTimestampCast( ArrayList commands ) {
        ArrayList modifiedCommands = new ArrayList();
        // CAST(CURRENT_TIMESTAMP AS CHAR(80)) is changed to CAST(CURRENT_TIMESTAMP AS CHAR)"
        String patternStr = "CAST *\\( *CURRENT_TIMESTAMP *AS *CHAR *\\( *[0-9]+ *\\) *\\)";
        Pattern pattern = Pattern.compile( patternStr, Pattern.CASE_INSENSITIVE );
        String replacementStr = "CAST(CURRENT_TIMESTAMP AS CHAR)";

        for( Iterator iterator = commands.iterator(); iterator.hasNext(); ) {
            String command = (String)iterator.next();
            Matcher matcher = pattern.matcher( command );
            String modifiedCommand = matcher.replaceAll( replacementStr );
            modifiedCommands.add( modifiedCommand );
        }

        return modifiedCommands;
    }

    ArrayList filterCreateCommands( ArrayList commands ) {
        commands = changeTableType( commands );
        commands = changeTimestampToDateTime( commands );
        commands = changeCHAR256PlusToTextForMySQL( commands );
        return commands;
    }

    /** MySQL has a special table type for tables that support transactions named InnoDB that is spceified during
     * table creation.
     */
    private ArrayList changeTableType( ArrayList commands ) {
        ArrayList modifiedCommands = new ArrayList();
        for( Iterator iterator = commands.iterator(); iterator.hasNext(); ) {
            String command = (String)iterator.next();
            if( CREATE_TABLE_STR.equalsIgnoreCase( command.substring( 0, CREATE_TABLE_STR.length() ) ) ) {
                command = command + " TYPE = InnoDB";
                command = addIndexesForForeigKeys( command );
            }
            modifiedCommands.add( command );
        }
        return modifiedCommands;
    }

    /**
     * If a table has a forreign key then there needs to be an index on that, http://www.mysql.com/doc/en/InnoDB_foreign_key_constraints.html
     * And of course it uses a non standard way to add an index in a table during creation.
     * CREATE TABLE ( pk INT, fk, INT, INDEX(fk), FOREIGN KEY (fk) REFERENCES foo (bar) )
     */
    private String addIndexesForForeigKeys( String command ) {
        int startindex = 0;
        String FOREIGN_KEY_IDENTIFIER = "FOREIGN KEY (";
        int foreginIndex = command.indexOf(FOREIGN_KEY_IDENTIFIER, startindex );
        while( -1 != foreginIndex ) {
            int foreignKeyParenthasisEnd = command.indexOf( ")", foreginIndex );
            String columnNameToBeIndexed = command.substring( foreginIndex + FOREIGN_KEY_IDENTIFIER.length(), foreignKeyParenthasisEnd);
            String indexConstraint = " INDEX( " + columnNameToBeIndexed + " ), ";
            command = command.substring( 0, foreginIndex ) + indexConstraint + command.substring( foreginIndex );
            startindex = foreignKeyParenthasisEnd + indexConstraint.length();
            foreginIndex = command.indexOf(FOREIGN_KEY_IDENTIFIER, startindex );
        }
        return command;
    }

    /**
     * MySQL dosen't have VARCHAR larger than VARCHAR(255). This method replaces the occurences found (this far) in the code
     * with TEXT type.
     * @param commands
     * @return
     */
    // todo Denna g�r att g�ra generellare, f�r tillf�llet implementerar den enbart > 1 000 generellt
    private ArrayList changeCHAR256PlusToTextForMySQL( ArrayList commands ) {
        ArrayList modifiedCommands = new ArrayList();
        for( Iterator iterator = commands.iterator(); iterator.hasNext(); ) {
            String command = (String)iterator.next();
            command = command.replaceAll( "VARCHAR\\s*\\(\\s*\\d{4,}\\s*\\)", TEXT_TYPE_MY_SQL); // "VARCHAR ( 1000 )" -> "TEXT"
            command = command.replaceAll( "NCHAR\\s*VARYING\\s*\\(\\s*\\d{4,}\\s*\\)", TEXT_TYPE_MY_SQL); // "NCHAR VARYING ( 15000 )" -> "TEXT"
            modifiedCommands.add( command );
        }
        return modifiedCommands;
    }
}
