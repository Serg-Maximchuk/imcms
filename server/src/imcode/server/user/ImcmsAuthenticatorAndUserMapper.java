package imcode.server.user;

import com.imcode.imcms.api.RoleConstants;
import imcode.server.IMCServiceInterface;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class ImcmsAuthenticatorAndUserMapper implements UserAndRoleMapper, Authenticator {

    // todo: make sure that these stored procedures are accesed (called) only used within this class
    // todo: and nowhere else directly to decouple
    // todo: if not, make the constant public and use it in place?
    // todo: Remove space in constansts
    private static final String SPROC_GET_ALL_USERS = "getAllUsers";
    private static final String SPROC_GET_HIGHEST_USER_ID = "GetHighestUserId";
    private static final String SPROC_ADD_NEW_USER = "AddNewUser";
    private static final String SPROC_UPDATE_USER = "UpdateUser";
    private static final String SPROC_GET_USER_BY_LOGIN = "GetUserByLogin";

    private static final String SPROC_ADD_USER_ROLE = "AddUserRole";
    private static final String SPROC_ROLE_ADD_NEW = "RoleAddNew";
    private static final String SPROC_ROLE_DELETE = "RoleDelete";
    private static final String SPROC_GET_ALL_ROLES = "GetAllRoles";
    private static final String SPROC_GET_USER_ROLES = "GetUserRoles";
    private static final String SPROC_DEL_USER_ROLES = "DelUserRoles";
    private static final String SPROC_GET_USERS_WHO_BELONGS_TO_ROLE = "GetUsersWhoBelongsToRole";

    private static final String SPROC_GET_USER_PHONE_NUMBERS = "GetUserPhoneNumbers ";
    private static final String SPROC_PHONE_NBR_ADD = "phoneNbrAdd";
    private static final String SPROC_DEL_PHONE_NR = "DelPhoneNr";

    private static final int USER_EXTERN_ID = 2;

    private IMCServiceInterface service;
    private Logger log = Logger.getLogger( ImcmsAuthenticatorAndUserMapper.class );

    public ImcmsAuthenticatorAndUserMapper( IMCServiceInterface service ) {
        this.service = service;
    }

    public boolean authenticate( String loginName, String password ) {
        boolean userExistsAndPasswordIsCorrect = false;
        UserDomainObject user = getUser( loginName );
        if ( null != user ) {
            String login_password_from_db = user.getPassword();
            String login_password_from_form = password;

            if ( login_password_from_db.equals( login_password_from_form ) && user.isActive() ) {
                userExistsAndPasswordIsCorrect = true;
            } else if ( !user.isActive() ) {
                userExistsAndPasswordIsCorrect = false;
            } else {
                userExistsAndPasswordIsCorrect = false;
            }
        }

        return userExistsAndPasswordIsCorrect;
    }

    public UserDomainObject getUser( String loginName ) {
        loginName = loginName.trim();

        String[] user_data = service.sqlProcedure( SPROC_GET_USER_BY_LOGIN, new String[]{loginName} );

        return getUserFromSqlResult( user_data );
    }

    private UserDomainObject getUserFromSqlResult( String[] sqlResult ) {
        UserDomainObject user;

        if ( sqlResult.length == 0 ) {
            user = null;
        } else {
            user = new UserDomainObject();

            initUserFromSqlData( user, sqlResult );

        }
        return user;
    }

    void initUserFromSqlData( UserDomainObject user, String[] sqlResult ) {
        user.setId( Integer.parseInt( sqlResult[0] ) );
        user.setLoginName( sqlResult[1] );
        user.setPassword( sqlResult[2].trim() );
        user.setFirstName( sqlResult[3] );
        user.setLastName( sqlResult[4] );
        user.setTitle( sqlResult[5] );
        user.setCompany( sqlResult[6] );
        user.setAddress( sqlResult[7] );
        user.setCity( sqlResult[8] );
        user.setZip( sqlResult[9] );
        user.setCountry( sqlResult[10] );
        user.setCountyCouncil( sqlResult[11] );
        user.setEmailAddress( sqlResult[12] );
        user.setLangId( Integer.parseInt( sqlResult[13] ) );
        user.setLanguageIso639_2( (String)ObjectUtils.defaultIfNull( sqlResult[14], service.getDefaultLanguageAsIso639_2() ) );
        user.setActive( 0 != Integer.parseInt( sqlResult[16] ) );
        user.setCreateDate( sqlResult[17] );
        user.setImcmsExternal( 0 != Integer.parseInt( sqlResult[18] ) );

        setPhoneNumbersForUser( user );

        user.setRoles( getRolesForUser( user ) );
    }

    private RoleDomainObject[] getRolesForUser( UserDomainObject user ) {
        String sqlStr = "SELECT roles.role_id, role_name, roles.admin_role FROM roles, user_roles_crossref"
                        + " WHERE user_roles_crossref.role_id = roles.role_id"
                        + " AND user_roles_crossref.user_id = ?";
        String[][] sqlResult = service.sqlQueryMulti( sqlStr, new String[]{"" + user.getId()} );
        RoleDomainObject[] roles = new RoleDomainObject[sqlResult.length];
        for ( int i = 0; i < sqlResult.length; i++ ) {
            String[] sqlRow = sqlResult[i];
            roles[i] = getRoleFromSqlResult( sqlRow );
        }
        return roles;
    }

    private void setPhoneNumbersForUser( UserDomainObject user ) {
        String[][] phoneNbr = service.sqlProcedureMulti( SPROC_GET_USER_PHONE_NUMBERS,
                                                         new String[]{"" + user.getId()} );
        String workPhone = "";
        String mobilePhone = "";
        String homePhone = "";

        if ( phoneNbr != null ) {
            for ( int i = 0; i < phoneNbr.length; i++ ) {
                if ( ( "2" ).equals( phoneNbr[i][3] ) ) {
                    workPhone = phoneNbr[i][1];
                } else if ( ( "3" ).equals( phoneNbr[i][3] ) ) {
                    mobilePhone = phoneNbr[i][1];
                } else if ( ( "1" ).equals( phoneNbr[i][3] ) ) {
                    homePhone = phoneNbr[i][1];
                }
            }
        }
        user.setWorkPhone( workPhone );
        user.setMobilePhone( mobilePhone );
        user.setHomePhone( homePhone );
    }

    /**
     * @return An object representing the user with the given id.
     */
    public UserDomainObject getUser( int userId ) {
        return new UserDomainObject( userId );
    }

    String[] sqlSelectUserData( int userId ) {
        String sqlStr = "SELECT user_id, login_name, login_password, first_name, last_name, "
                        + "title, company, address, city, zip, country, county_council, "
                        + "email, users.lang_id, lang_prefix, user_type, active, "
                        + "create_date, external "
                        + "FROM users, lang_prefixes "
                        + "WHERE users.lang_id = lang_prefixes.lang_id "
                        + "AND user_id = ?";

        String[] user_data = service.sqlQuery( sqlStr, new String[]{"" + userId} );
        return user_data;
    }

    public void updateUser( String loginName, UserDomainObject newUser ) {
        UserDomainObject imcmsUser = getUser( loginName );
        UserDomainObject tempUser = (UserDomainObject)newUser.clone();
        tempUser.setId( imcmsUser.getId() );
        tempUser.setLoginName( loginName );

        callSprocModifyUserProcedure( SPROC_UPDATE_USER, tempUser );
        removePhoneNumbers( tempUser );
        addPhoneNumbers( tempUser );
    }

    public synchronized void addUser( UserDomainObject newUser ) {
        String newUserId = service.sqlProcedureStr( SPROC_GET_HIGHEST_USER_ID, new String[]{} );
        int newIntUserId = Integer.parseInt( newUserId );
        newUser.setId( newIntUserId );

        callSprocModifyUserProcedure( SPROC_ADD_NEW_USER, newUser );
        addPhoneNumbers( newUser );
    }

    private void removePhoneNumbers( UserDomainObject newUser ) {
        staticSprocDelPhoneNr( service, newUser.getId() );
    }

    private void addPhoneNumbers( UserDomainObject newUser ) {
        final int PHONE_TYPE_HOME_PHONE = 1;
        final int PHONE_TYPE_WORK_PHONE = 2;
        final int PHONE_TYPE_WORK_MOBILE = 3;
        staticSprocPhoneNbrAdd( service, newUser.getId(), newUser.getHomePhone(), PHONE_TYPE_HOME_PHONE );
        staticSprocPhoneNbrAdd( service, newUser.getId(), newUser.getWorkPhone(), PHONE_TYPE_WORK_PHONE );
        staticSprocPhoneNbrAdd( service, newUser.getId(), newUser.getMobilePhone(), PHONE_TYPE_WORK_MOBILE );
    }

    public String[] getRoleNames( UserDomainObject user ) {
        String[] roleNames = service.sqlProcedure( SPROC_GET_USER_ROLES, new String[]{"" + user.getId()} );
        return roleNames;
    }

    public String[] getAllRoleNames() {
        String[] roleNamesMinusUsers = service.sqlProcedure( SPROC_GET_ALL_ROLES, new String[]{} );

        Set roleNamesSet = new HashSet();
        for ( int i = 0; i < roleNamesMinusUsers.length; i += 2 ) {
            String roleName = roleNamesMinusUsers[i + 1];
            roleNamesSet.add( roleName );
        }

        roleNamesSet.add( RoleConstants.USERS );

        String[] roleNames = (String[])roleNamesSet.toArray( new String[roleNamesSet.size()] );
        Arrays.sort( roleNames );

        return roleNames;
    }

    public void addRoleNames( String[] externalRoleNames ) {
        for ( int i = 0; i < externalRoleNames.length; i++ ) {
            String externalRoleName = externalRoleNames[i];
            this.addRole( externalRoleName );
        }
    }

    public void addRoleToUser( UserDomainObject user, String roleName ) {
        addRole( roleName );
        log.debug( "Trying to assign role " + roleName + " to user " + user.getLoginName() );
        RoleDomainObject role = getRoleByName( roleName );
        sqlAddRoleToUser( role, user );
    }

    private void sqlAddRoleToUser( RoleDomainObject role, UserDomainObject user ) {
        service.sqlUpdateProcedure( SPROC_ADD_USER_ROLE, new String[]{String.valueOf( user.getId() ), "" + role.getId()} );
    }

    // todo: make a quicker version that not loops over all of the user_ids and makes a new db searc
    // todo: change the "getAllUsers" sproc to specify its arguments.
    public UserDomainObject[] getAllUsers() {
        int noOfColumnsInSearchResult = 20;
        String[] allUsersSqlResult = service.sqlProcedure( SPROC_GET_ALL_USERS, new String[]{} );
        int noOfUsers = allUsersSqlResult.length / noOfColumnsInSearchResult;
        UserDomainObject[] result = new UserDomainObject[noOfUsers];
        for ( int i = 0; i < noOfUsers; i++ ) {
            String userId = allUsersSqlResult[i * noOfColumnsInSearchResult];
            result[i] = getUser( Integer.parseInt( userId ) );
        }
        return result;
    }

    public UserDomainObject[] getUsers( boolean includeUserExtern, boolean includeInactiveUsers ) {
        UserDomainObject[] users = getAllUsers();
        List filterdUsers = new ArrayList();
        if ( !includeUserExtern ) {
            for ( int i = 0; i < users.length; i++ ) {
                UserDomainObject user = users[i];
                boolean includeAcordingToUserExtern = !includeUserExtern && USER_EXTERN_ID != user.getId();
                boolean includeAcordingToInactiveUser = user.isActive() || includeInactiveUsers;
                if ( includeAcordingToUserExtern && includeAcordingToInactiveUser ) {
                    filterdUsers.add( user );
                }
            }
        }
        return (UserDomainObject[])filterdUsers.toArray( new UserDomainObject[filterdUsers.size()] );
    }

    public void setUserRoles( UserDomainObject user, String[] roleNames ) {
        this.sqlRemoveAllRoles( user );

        for ( int i = 0; i < roleNames.length; i++ ) {
            String roleName = roleNames[i];
            this.addRoleToUser( user, roleName );
        }
    }

    private void sqlRemoveAllRoles( UserDomainObject user ) {
        service.sqlUpdateProcedure( SPROC_DEL_USER_ROLES, new String[]{"" + user.getId(), "-1"} );
    }

    public UserDomainObject[] getAllUsersWithRole( String roleName ) {
        RoleDomainObject role = getRoleByName( roleName );
        if( null == role ) {
            return new UserDomainObject[]{};
        }
        String[] usersWithRole = service.sqlProcedure( SPROC_GET_USERS_WHO_BELONGS_TO_ROLE,
                                                       new String[]{"" + role.getId()} );
        UserDomainObject[] result = new UserDomainObject[usersWithRole.length / 2];

        for ( int i = 0; i < result.length; i++ ) {
            String userIdStr = usersWithRole[i * 2];
            UserDomainObject user = getUser( Integer.parseInt( userIdStr ) );
            result[i] = user;
        }
        return result;
    }

    public static void staticSprocPhoneNbrAdd( IMCServiceInterface service,
                                               int newUserId, String phoneNumber, int phoneNumberType ) {
        String[] sprocParameters = new String[]{
            String.valueOf( newUserId ), phoneNumber, String.valueOf( phoneNumberType )
        };
        service.sqlUpdateProcedure( SPROC_PHONE_NBR_ADD, sprocParameters );
    }

    private static void staticSprocDelPhoneNr( IMCServiceInterface service, int userId ) {
        String[] sprocParameters = new String[]{String.valueOf( userId )};
        service.sqlUpdateProcedure( SPROC_DEL_PHONE_NR, sprocParameters );
    }

    public synchronized RoleDomainObject addRole( String roleName ) {
        RoleDomainObject role = getRoleByName( roleName );
        boolean roleExists = null != role;
        if ( !roleExists ) {
            service.sqlUpdateProcedure( SPROC_ROLE_ADD_NEW, new String[]{roleName} );
        }
        return getRoleByName( roleName ) ;
    }

    public void deleteRole( String roleName ) {
        RoleDomainObject role = getRoleByName( roleName );
        final boolean roleExists = null != role;
        if ( roleExists ) {
            service.sqlUpdateProcedure( SPROC_ROLE_DELETE, new String[]{"" + role.getId()} );
        }
    }

    public RoleDomainObject getRoleById( int roleId ) {
        String sqlStr = "SELECT role_id, role_name, admin_role FROM roles WHERE role_id = ?";
        String[] sqlResult = service.sqlQuery( sqlStr, new String[]{"" + roleId} );
        return getRoleFromSqlResult( sqlResult );
    }

    public RoleDomainObject getRoleByName( String wantedRoleName ) {
        String sqlStr = "SELECT role_id, role_name, admin_role FROM roles WHERE role_name = ?";
        String[] sqlResult = service.sqlQuery( sqlStr, new String[]{wantedRoleName} );
        return getRoleFromSqlResult( sqlResult );
    }

    private RoleDomainObject getRoleFromSqlResult( String[] sqlResult ) {
        RoleDomainObject role = null;
        if ( sqlResult.length > 0 ) {
            int roleId = Integer.parseInt( sqlResult[0] );
            String roleName = sqlResult[1];
            int adminRoleId = Integer.parseInt( sqlResult[2] );
            role = new RoleDomainObject( roleId, roleName, adminRoleId );
        }
        return role;
    }

    private void callSprocModifyUserProcedure( String modifyUserProcedureName, UserDomainObject tempUser ) {
        String[] params = {
            String.valueOf( tempUser.getId() ),
            tempUser.getLoginName(),
            null == tempUser.getPassword() ? "" : tempUser.getPassword(),
            tempUser.getFirstName(),
            tempUser.getLastName(),
            tempUser.getTitle(),
            tempUser.getCompany(),
            tempUser.getAddress(),
            tempUser.getCity(),
            tempUser.getZip(),
            tempUser.getCountry(),
            tempUser.getCountyCouncil(),
            tempUser.getEmailAddress(),
            tempUser.isImcmsExternal() ? "1" : "0",
            "1001",
            "0",
            String.valueOf( tempUser.getLangId() ),
            "0",
            tempUser.isActive() ? "1" : "0"
        };
        service.sqlUpdateProcedure( modifyUserProcedureName, params );

        sqlRemoveAllRoles( tempUser );
        sqlAddAllRoles( tempUser );

    }

    private void sqlAddAllRoles( UserDomainObject tempUser ) {
        tempUser.addRole( RoleDomainObject.USERS );

        RoleDomainObject[] userRoles = tempUser.getRoles() ;
        for ( int i = 0; i < userRoles.length; i++ ) {
            RoleDomainObject userRole = userRoles[i];
            sqlAddRoleToUser( userRole, tempUser );
        }
    }

}