package imcode.server.db;

import imcode.server.test.Log4JConfiguredTestCase;

import java.sql.Timestamp;

public class TestDatabaseService extends Log4JConfiguredTestCase {
    static final String DB_HOST = "localhost";

    static final int SQLSERVER_PORT = 1433;
    static final String SQLSERVER_DATABASE_NAME = "test";
    static final String SQLSERVE_DATABASE_USER = "sa";
    static final String SQLSERVE_DATABASE_PASSWORD = "sa";

    static final int MIMER_PORT = 1360;
    static final String MIMMER_DATABASE_NAME = "test";
    static final String MIMMER_DATABASE_USER = "sysadm";
    static final String MIMMER_DATABASE_PASSWORD = "admin";

    static int MYSQL_PORT = 3306;
    static String MYSQL_DATABASE_NAME = "test";
    static String MYSQL_DATABASE_USER = "root";
    static String MYSQL_DATABASE_PASSWORD = "";

    private DatabaseService sqlServer;
    private DatabaseService mimer;
    private DatabaseService mySql;

    private final boolean testMimer = true;  // because it is so slow to test this database we need sometimes to turn those tests off.

    protected void setUp() {
        initMySql();
        initSqlServer();
        if( testMimer )
            initMimer();
    }

    private void nonmodifyingTestSameResultFrom_sproc_getAllRoles() {
        DatabaseService.Table_roles[] sqlServerRoles = sqlServer.sprocGetAllRoles();
        DatabaseService.Table_roles[] mySQLRoles = mySql.sprocGetAllRoles();
        assertEquals( 2, sqlServerRoles.length );
        assertEquals( 2, mySQLRoles.length );

        if( testMimer ) {
            DatabaseService.Table_roles[] mimerRoles = mimer.sprocGetAllRoles();
            assertEquals( 2, mimerRoles.length );
            static_assertEquals( mimerRoles, sqlServerRoles, mySQLRoles );
        }
    }

    private void nonmodifyingTestSameResultFrom_sproc_getAllUsers() {
        DatabaseService.Table_users[] sqlServerUsers = sqlServer.sprocGetAllUsers_OrderByLastName();
        DatabaseService.Table_users[] mySQLUsers = mySql.sprocGetAllUsers_OrderByLastName();
        assertEquals( 2, sqlServerUsers.length );
        assertEquals( 2, mySQLUsers.length );

        if( testMimer ) {
            DatabaseService.Table_users[] mimerUsers = mimer.sprocGetAllUsers_OrderByLastName();
            assertEquals( 2, mimerUsers.length );
            static_assertEquals( mimerUsers, sqlServerUsers, mySQLUsers );
        }
    }

    private void nonmodifyingTestSameResultFrom_sproc_getTemplatesInGroup() {
        DatabaseService.ViewTemplateGroup templateGroupZero = new DatabaseService.ViewTemplateGroup( 1, "Start" );

        DatabaseService.ViewTemplateGroup[] sqlServerTemplatesInGroupZero = sqlServer.sprocGetTemplatesInGroup( 0 );
        assertEquals( 1, sqlServerTemplatesInGroupZero.length );
        assertEquals( templateGroupZero, sqlServerTemplatesInGroupZero[0] );
        DatabaseService.ViewTemplateGroup[] sqlServerTemplatesInGroupOneo = sqlServer.sprocGetTemplatesInGroup( 1 );
        DatabaseService.ViewTemplateGroup[] sqlServerTemplatesInGroupTwo = sqlServer.sprocGetTemplatesInGroup( 2 );

        DatabaseService.ViewTemplateGroup[] mySQLTemplatesInGroupZero = mySql.sprocGetTemplatesInGroup( 0 );
        assertEquals( 1, mySQLTemplatesInGroupZero.length );
        assertEquals( templateGroupZero, mySQLTemplatesInGroupZero[0] );
        DatabaseService.ViewTemplateGroup[] mySQLTemplatesInGroupOne = mySql.sprocGetTemplatesInGroup( 1 );
        DatabaseService.ViewTemplateGroup[] mySQLTemplatesInGroupTwo = mySql.sprocGetTemplatesInGroup( 2 );

        if( testMimer ) {
            DatabaseService.ViewTemplateGroup[] mimerTemplatesInGroupZero = mimer.sprocGetTemplatesInGroup( 0 );
            assertEquals( 1, mimerTemplatesInGroupZero.length );
            assertEquals( templateGroupZero, mimerTemplatesInGroupZero[0] );
            DatabaseService.ViewTemplateGroup[] mimerTemplatesInGroupOne = mimer.sprocGetTemplatesInGroup( 1 );
            assertEquals( mimerTemplatesInGroupOne.length, sqlServerTemplatesInGroupOneo.length );
            assertEquals( mimerTemplatesInGroupOne.length, mySQLTemplatesInGroupOne.length );
            DatabaseService.ViewTemplateGroup[] mimerTemplatesInGroupTwo = mimer.sprocGetTemplatesInGroup( 2 );
            assertEquals( mimerTemplatesInGroupTwo.length, sqlServerTemplatesInGroupTwo.length );
            assertEquals( mimerTemplatesInGroupTwo.length, mySQLTemplatesInGroupTwo.length );
        }
    }

    private void nonmodifyingTest_sproc_getHighestUserId() {
        if( testMimer ) {
            int mimerUserMax = mimer.sproc_getHighestUserId();
            assertEquals( 3, mimerUserMax );
        }

        int sqlServerUserMax = sqlServer.sproc_getHighestUserId();
        int mySqlUserMax = mySql.sproc_getHighestUserId();
        assertEquals( 3, sqlServerUserMax );
        assertEquals( 3, mySqlUserMax );
    }


    /** in order to speed up the testing a bit **/
    public void testAllNonModifyingTests() {
        nonmodifyingTest_sproc_getHighestUserId();
        nonmodifyingTestSameResultFrom_sproc_getAllRoles();
        nonmodifyingTestSameResultFrom_sproc_getAllUsers();
        nonmodifyingTestSameResultFrom_sproc_getTemplatesInGroup();
    }

    public void test_sproc_AddNewuser() {
        int nextFreeUserId = 3;
        DatabaseService.Table_users user = createDummyUser( nextFreeUserId );

        if( testMimer ) {
            DatabaseService.Table_users[] mimerUsersBefore = mimer.sprocGetAllUsers_OrderByLastName();
            mimer.sproc_AddNewuser( user );
            DatabaseService.Table_users[] mimerUsersAfter = mimer.sprocGetAllUsers_OrderByLastName();
            assertTrue( mimerUsersAfter.length == mimerUsersBefore.length + 1 );
        }

        DatabaseService.Table_users[] sqlServerUsersBefore = sqlServer.sprocGetAllUsers_OrderByLastName();
        sqlServer.sproc_AddNewuser( user );
        DatabaseService.Table_users[] sqlServerUsersAfter = sqlServer.sprocGetAllUsers_OrderByLastName();
        assertTrue( sqlServerUsersAfter.length == sqlServerUsersBefore.length + 1 );

        DatabaseService.Table_users[] mySqlUsersBefore = mySql.sprocGetAllUsers_OrderByLastName();
        mySql.sproc_AddNewuser( user );
        DatabaseService.Table_users[] mySqlUsersAfter = mySql.sprocGetAllUsers_OrderByLastName();
        assertTrue( mySqlUsersAfter.length == mySqlUsersBefore.length + 1 );
    }

    public void test_sproc_updateUser() {
        int nextFreeUserId = 3;
        DatabaseService.Table_users user = createDummyUser( nextFreeUserId );
        if( testMimer ) {
            mimer.sproc_AddNewuser( user );
            int mimerRowAffected = mimer.sproc_updateUser( user );
            assertEquals( 1, mimerRowAffected );
        }

        sqlServer.sproc_AddNewuser( user );
        mySql.sproc_AddNewuser( user );

        int sqlServerRowAffected = sqlServer.sproc_updateUser( user );
        int mySqlRowAffected = mySql.sproc_updateUser( user );

        assertEquals( 1, sqlServerRowAffected );
        assertEquals( 1, mySqlRowAffected );

        DatabaseService.Table_users[] sqlServerUsers = sqlServer.sprocGetAllUsers_OrderByLastName();
        DatabaseService.Table_users modifiedUser = null;
        int i = 0;
        while( modifiedUser == null ) {
            if( sqlServerUsers[i].user_id == nextFreeUserId ) {
                modifiedUser = sqlServerUsers[i];
            }
            i++;
        }
        assertEquals( user, modifiedUser );
    }

    public void test_sproc_phoneNbrAdd() {
        DatabaseService.Table_users user = createDummyUser( 3 );
        if( testMimer ) {
            mimer.sproc_AddNewuser( user );
            int mimerRowAffected = mimer.sproc_phoneNbrAdd( 3, "1234567", 0 );
            assertEquals( 1, mimerRowAffected );
        }

        sqlServer.sproc_AddNewuser( user );
        int sqlServerRowAffected = sqlServer.sproc_phoneNbrAdd( 3, "1234567", 0 );

        mySql.sproc_AddNewuser( user );
        int mySqlRowAffected = mySql.sproc_phoneNbrAdd( 3, "1234567", 0 );

        assertEquals( 1, sqlServerRowAffected );
        assertEquals( 1, mySqlRowAffected );
    }

    private DatabaseService.Table_users createDummyUser( int nextFreeUserId ) {
        DatabaseService.Table_users user = new DatabaseService.Table_users( nextFreeUserId, "test login name", "test password", "First name", "Last name", "Titel", "Company", "Adress", "City", "Zip", "Country", "Country council", "Email adress", 0, 1001, 0, 1, 1, 1, new Timestamp( new java.util.Date().getTime() ) );
        return user;
    }

    private static void static_assertEquals( Object[] ref, Object[] one, Object[] another ) {
        static_assertEquals( ref, one );
        static_assertEquals( ref, another );
    }

    private static void static_assertEquals( Object[] oneArr, Object[] anotherArr ) {
        if( oneArr == null ) {
            assertNotNull( anotherArr );
        } else if( anotherArr == null ) {
            fail( "The second array is null, but not the first oneArr" );
        } else {
            assertTrue( oneArr != anotherArr );
            assertEquals( oneArr.length, anotherArr.length );
            for( int i = 0; i < oneArr.length; i++ ) {
                Object one = oneArr[i];
                Object another = anotherArr[i];
                assertTrue( one != another );
                assertEquals( one, another );
            }
        }
    }

    private void initMimer() {
        mimer = new DatabaseService( DatabaseService.MIMER, TestDatabaseService.DB_HOST, TestDatabaseService.MIMER_PORT, TestDatabaseService.MIMMER_DATABASE_NAME, TestDatabaseService.MIMMER_DATABASE_USER, TestDatabaseService.MIMMER_DATABASE_PASSWORD );
        mimer.initializeDatabase();
    }

    private void initSqlServer() {
        sqlServer = new DatabaseService( DatabaseService.SQL_SERVER, TestDatabaseService.DB_HOST, TestDatabaseService.SQLSERVER_PORT, TestDatabaseService.SQLSERVER_DATABASE_NAME, TestDatabaseService.SQLSERVE_DATABASE_USER, TestDatabaseService.SQLSERVE_DATABASE_PASSWORD );
        sqlServer.initializeDatabase();
    }

    private void initMySql() {
        mySql = new DatabaseService( DatabaseService.MY_SQL, TestDatabaseService.DB_HOST, TestDatabaseService.MYSQL_PORT, TestDatabaseService.MYSQL_DATABASE_NAME, TestDatabaseService.MYSQL_DATABASE_USER, TestDatabaseService.MYSQL_DATABASE_PASSWORD );
        mySql.initializeDatabase();
    }
}
