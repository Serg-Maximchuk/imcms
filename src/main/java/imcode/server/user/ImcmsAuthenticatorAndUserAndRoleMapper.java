package imcode.server.user;

import com.imcode.db.DatabaseCommand;
import com.imcode.db.DatabaseConnection;
import com.imcode.db.DatabaseException;
import com.imcode.db.commands.*;
import com.imcode.db.exceptions.IntegrityConstraintViolationException;
import com.imcode.db.exceptions.StringTruncationException;
import com.imcode.imcms.db.StringArrayResultSetHandler;
import com.imcode.imcms.mapping.jpa.User;
import com.imcode.imcms.mapping.jpa.UserRepository;
import com.imcode.imcms.servlet.LoginPasswordManager;
import imcode.server.ImcmsServices;
import imcode.util.Utility;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.util.*;

public class ImcmsAuthenticatorAndUserAndRoleMapper implements UserAndRoleRegistry, Authenticator, RoleGetter {

    private final static Logger log = Logger.getLogger(ImcmsAuthenticatorAndUserAndRoleMapper.class);

    private static final String SPROC_GET_ALL_ROLES = "GetAllRoles";
    private static final String SPROC_GET_USER_ROLES = "GetUserRoles";
    private static final String SPROC_GET_USERS_WHO_BELONGS_TO_ROLE = "GetUsersWhoBelongsToRole";

    private static final String SPROC_DEL_PHONE_NR = "DelPhoneNr";

    public static final String SQL_ROLES_COLUMNS = "roles.role_id, roles.role_name, roles.admin_role, roles.permissions";
    private static final String SQL_SELECT_ALL_ROLES = "SELECT " + SQL_ROLES_COLUMNS + " FROM roles";
    private static final String SQL_SELECT_ALL_ROLES_EXCEPT_USERS_ROLE = SQL_SELECT_ALL_ROLES
            + " WHERE roles.role_id != " + RoleId.USERS_ID;

    public static final String SQL_SELECT_ROLE_BY_NAME = SQL_SELECT_ALL_ROLES + " WHERE role_name = ?";
    private static final String SQL_SELECT_ROLE_BY_ID = SQL_SELECT_ALL_ROLES + " WHERE role_id = ?";

    public static final String SQL_INSERT_INTO_ROLES = "INSERT INTO roles (role_name, permissions, admin_role) VALUES(?,?,0)";
    private static final String TABLE__USERADMIN_ROLE_CROSSREF = "useradmin_role_crossref";

    private final ImcmsServices services;

    private UserRepository userRepository;

    /**
     * @since 4.0.7
     */
    private LoginPasswordManager loginPasswordManager;

    public ImcmsAuthenticatorAndUserAndRoleMapper(ImcmsServices services, LoginPasswordManager userLoginPasswordManager) {
        this.services = services;
        this.loginPasswordManager = userLoginPasswordManager;
        this.userRepository = services.getManagedBean(UserRepository.class);
    }

    /**
     * Authenticates internal user.
     * The user being authenticated must be internal and his account must be active.
     *
     * @param loginName user's login name
     * @param password  user's plain text or encrypted password
     * @return if user has been authenticated.
     */
    public boolean authenticate(String loginName, String password) {
        UserDomainObject user = getUser(loginName);

        return user != null && user.isActive() && !user.isImcmsExternal() &&
                (user.isPasswordEncrypted()
                        ? loginPasswordManager.validatePassword(password, user.getPassword())
                        : password.equals(user.getPassword()));
    }

    /**
     * @since 4.0.7
     */
    public UserDomainObject getUserByEmail(String email) {
        return email == null ? null : toDomainObject(userRepository.findByEmailUnique(email));
    }

    /**
     * Create and assign a new PasswordReset to the existing user.
     * User must exist and must not be default, external or superadmin when password reset is not allowed for superadmins.
     *
     * @param id existing user's id.
     * @return user with assigned PasswordReset
     * @throws IllegalStateException if PasswordReset can not be created.
     * @since 4.1.3
     */
    public UserDomainObject createPasswordReset(int id) {
        UserDomainObject user = getUser(id);

        if (user == null)
            throw new IllegalStateException(String.format("User with id %s does not exist.", id));

        String illegalState = user.isDefaultUser()
                ? "default"
                : user.isImcmsExternal()
                ? "external"
                : user.isSuperAdmin() && !services.getConfig().isSuperadminLoginPasswordResetAllowed()
                ? "superuser"
                : null;

        if (illegalState != null)
            throw new IllegalStateException(String.format(
                    "Can't create password reset for [%s] user %s. User must not be default, external or superadmin when password reset is not allowed for superadmins.",
                    illegalState, user));

        user.setPasswordReset(UUID.randomUUID().toString(), System.currentTimeMillis());
        saveUser(user);

        return user;
    }


    /**
     * @param resetId password reset id
     * @return user or null if user can not be found, password-reset has been expired, or user is superadmin when password reset is not allowed for superadmins.
     * @since 4.1.3
     */
    public UserDomainObject getUserByPasswordResetId(String resetId) {
        UserDomainObject user = toDomainObject(userRepository.findByPasswordResetId(resetId));

        return (user == null || isPasswordResetExpired(user.getPasswordReset().getTime())
                || (user.isSuperAdmin() && !services.getConfig().isSuperadminLoginPasswordResetAllowed()))
                ? null
                : user;
    }

    public UserDomainObject getUser(String loginName) {
        return StringUtils.isBlank(loginName) ? null : toDomainObject(userRepository.findByLogin(loginName));
    }

    /**
     * @param login
     * @return user or null if user can not be found.
     * @since 4.1.3
     */
    public UserDomainObject getUserByLoginIgnoreCase(String login) {
        return StringUtils.isBlank(login) ? null : toDomainObject(userRepository.findByLoginIgnoreCase(login));
    }

    /**
     * @param time password reset time
     * @return if password reset has been expired.
     * @since 4.0.7
     */
    private boolean isPasswordResetExpired(long time) {
        int interval = Hours.hoursBetween(new DateTime(time), new DateTime()).getHours();

        return interval > services.getSystemData().getUserLoginPasswordResetExpirationInterval();
    }

    private UserDomainObject toDomainObject(User user) {
        if (user == null) {
            return null;
        }

        UserDomainObject userDO = new UserDomainObject();
        userDO.setActive(user.isActive());
        userDO.setAddress(user.getAddress());
        //userDO.setAuthenticatedByIp();
        userDO.setCity(user.getCity());
        userDO.setCompany(user.getCompany());
        userDO.setCountry(user.getCountry());
        //todo: check date
        //DateFormat dateFormat = new SimpleDateFormat(DateConstants.DATETIME_FORMAT_STRING);
        userDO.setCreateDate(user.getCreateDate());
        //userDO.setCurrentContextPath();
        userDO.setEmailAddress(user.getEmail());
        userDO.setFirstName(user.getFirstName());
        userDO.setId(user.getId());
        userDO.setLoginName(user.getLogin());
        userDO.setImcmsExternal(user.isExternal());
        userDO.setLanguageIso639_2(ObjectUtils.defaultIfNull(user.getLanguageIso639_2(), services.getLanguageMapper().getDefaultLanguage()));
        userDO.setLastName(user.getLastName());
        userDO.setPassword(user.getPassword(), UserDomainObject.PasswordType.valueOf(user.getPasswordType().name()));
        Optional.ofNullable(user.getPasswordReset()).ifPresent(passwordReset ->
                        userDO.setPasswordReset(passwordReset.getId(), passwordReset.getTimestamp())
        );
        userDO.setProvince(user.getProvince());
        //userDO.setRoleIds();
        userDO.setSessionId(user.getSessionId());
        //userDO.setTemplateGroup();
        userDO.setTitle(user.getTitle());
        //userDO.setUserAdminRolesIds();
        userDO.setZip(user.getZip());

        initUserRoles(userDO);
        initUserPhoneNumbers(userDO);
        initUserUserAdminRoles(userDO);

        return userDO;
    }

    private RoleId[] getRoleReferencesForUser(UserDomainObject user) {
        try {
            String sqlStr = SQL_SELECT_ALL_ROLES + ", user_roles_crossref"
                    + " WHERE user_roles_crossref.role_id = roles.role_id"
                    + " AND user_roles_crossref.user_id = ?";
            final Object[] parameters = new String[]{"" + user.getId()};
            String[][] sqlResult = (String[][]) services.getDatabase().execute(new SqlQueryCommand(sqlStr, parameters, Utility.STRING_ARRAY_ARRAY_HANDLER));
            RoleId[] roleReferences = new RoleId[sqlResult.length];
            for (int i = 0; i < sqlResult.length; i++) {
                String[] sqlRow = sqlResult[i];
                roleReferences[i] = getRoleReferenceFromSqlResult(sqlRow);
            }
            return roleReferences;
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    private RoleId getRoleReferenceFromSqlResult(String[] sqlRow) {
        return new RoleId(Integer.parseInt(sqlRow[0]));
    }

    /**
     * @return An object representing the user with the given id.
     */
    public UserDomainObject getUser(int userId) {
        return toDomainObject(userRepository.findById(userId));
    }


    public void saveUser(String loginName, UserDomainObject userToSave) {
        UserDomainObject imcmsUser = getUser(loginName);
        userToSave.setId(imcmsUser.getId());
        userToSave.setLoginName(loginName);
        saveUser(userToSave);
    }


    public void updateUserSessionId(final UserDomainObject loggedInUser) {
        userRepository.updateSessionId(loggedInUser.getId(), loggedInUser.getSessionId());
    }


    public String getUserSessionId(final UserDomainObject loggedInUser) {
        return userRepository.findSessionId(loggedInUser.getId());
    }

    /**
     * If user is external sets the password to blank.
     * Otherwise encrypts the password if it was modified.
     *
     * @param user
     * @since 4.0.7
     */
    private void modifyPasswordIfNecessary(UserDomainObject user) {
        if (user.isImcmsExternal()) {
            user.setPassword("");
        } else if (!user.isPasswordEncrypted() && services.getConfig().isLoginPasswordEncryptionEnabled()) {
            String password = user.getPassword();
            if (StringUtils.isNotBlank(password)) {
                user.setPassword(loginPasswordManager.encryptPassword(password), UserDomainObject.PasswordType.ENCRYPTED);
            }
        }
    }

    public void saveUser(UserDomainObject user) {
        modifyPasswordIfNecessary(user);

        String[] params = {
                user.getLoginName(),
                null == user.getPassword() ? "" : user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getTitle(),
                user.getCompany(),
                user.getAddress(),
                user.getCity(),
                user.getZip(),
                user.getCountry(),
                user.getProvince(),
                user.getEmailAddress(),
                user.isImcmsExternal() ? "1" : "0",
                user.isActive() ? "1" : "0",
                user.getLanguageIso639_2(),
                user.isPasswordEncrypted() ? "1" : "0",
                user.hasPasswordReset() ? user.getPasswordReset().getId() : null,
                user.hasPasswordReset() ? Long.toString(user.getPasswordReset().getTime()) : null,
                "" + user.getId(),
        };
        try {
            services.getDatabase().execute(new SqlUpdateCommand("UPDATE users \n"
                    + "SET login_name = ?,\n"
                    + "login_password = ?,\n"
                    + "first_name = ?,\n"
                    + "last_name = ?,\n"
                    + "title = ?,\n"
                    + "company = ?,\n"
                    + "address =  ?,\n"
                    + "city = ?,\n"
                    + "zip = ?,\n"
                    + "country = ?,\n"
                    + "county_council = ?,\n"
                    + "email = ?,\n"
                    + "external = ?,\n"
                    + "active = ?,\n"
                    + "language = ?,\n"
                    + "login_password_is_encrypted = ?,\n"
                    + "login_password_reset_id = ?,\n"
                    + "login_password_reset_ts = ?\n"
                    + "WHERE user_id = ?", params));
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }

        updateUserRoles(user);
        removePhoneNumbers(user);
        addPhoneNumbers(user);
    }

    private void updateUserRoles(UserDomainObject newUser) {
        Set<RoleId> newUserRoleIds = new HashSet<>(Arrays.asList(newUser.getRoleIds()));
        newUserRoleIds.add(RoleId.USERS);
        CompositeDatabaseCommand updateUserRolesCommand = new CompositeDatabaseCommand(new DeleteWhereColumnsEqualDatabaseCommand("user_roles_crossref", "user_id", newUser.getId()));
        for (RoleId roleId : newUserRoleIds) {
            updateUserRolesCommand.add(new InsertIntoTableDatabaseCommand("user_roles_crossref", new String[][]{
                    {"user_id", "" + newUser.getId()},
                    {"role_id", "" + roleId.intValue()}
            }));
        }
        services.getDatabase().execute(updateUserRolesCommand);
        sqlUpdateUserUserAdminRoles(newUser);
    }

    private void sqlUpdateUserUserAdminRoles(UserDomainObject user) {
        DeleteWhereColumnsEqualDatabaseCommand deleteAllUserAdminRolesForUserCommand = new DeleteWhereColumnsEqualDatabaseCommand(TABLE__USERADMIN_ROLE_CROSSREF, "user_id",
                ""
                        + user.getId()
        );
        CompositeDatabaseCommand updateUserAdminRolesCommand = new CompositeDatabaseCommand(deleteAllUserAdminRolesForUserCommand);
        RoleId[] userAdminRolesReferences = user.getUserAdminRoleIds();
        for (RoleId userAdminRoleId : userAdminRolesReferences) {
            updateUserAdminRolesCommand.add(new InsertIntoTableDatabaseCommand(TABLE__USERADMIN_ROLE_CROSSREF, new String[][]{
                    {"user_id", "" + user.getId()},
                    {"role_id", "" + userAdminRoleId.intValue()}
            }));
        }
        services.getDatabase().execute(updateUserAdminRolesCommand);
    }

    public synchronized void addUser(UserDomainObject user) throws UserAlreadyExistsException {
        if (null != getUser(user.getLoginName())) {
            throw new UserAlreadyExistsException(
                    "A user with the name \"" + user.getLoginName() + "\" already exists.");
        }
        try {
            modifyPasswordIfNecessary(user);

            Number newUserId = (Number) services.getDatabase().execute(new InsertIntoTableDatabaseCommand("users", new String[][]{
                    {"login_name", user.getLoginName()},
                    {"login_password", user.getPassword()},
                    {"first_name", user.getFirstName()},
                    {"last_name", user.getLastName()},
                    {"title", user.getTitle()},
                    {"company", user.getCompany()},
                    {"address", user.getAddress()},
                    {"city", user.getCity()},
                    {"zip", user.getZip()},
                    {"country", user.getCountry()},
                    {"county_council", user.getProvince()},
                    {"email", user.getEmailAddress()},
                    {"external", user.isImcmsExternal() ? "1" : "0"},
                    {"active", user.isActive() ? "1" : "0"},
                    {"language", user.getLanguageIso639_2()},
                    {"create_date", Utility.makeSqlStringFromDate(new Date())},

                    {"login_password_is_encrypted", BooleanUtils.toString(user.isPasswordEncrypted(), "1", "0")},
                    {"login_password_reset_id", user.hasPasswordReset() ? user.getPasswordReset().getId() : null},
                    {"login_password_reset_ts", user.hasPasswordReset() ? Long.toString(user.getPasswordReset().getTime()) : null}
            }));
            int newIntUserId = newUserId.intValue();
            user.setId(newIntUserId);

            updateUserRoles(user);
            addPhoneNumbers(user);
        } catch (IntegrityConstraintViolationException e) {
            throw new UserAlreadyExistsException(e);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    private void removePhoneNumbers(UserDomainObject newUser) {
        String[] sprocParameters = new String[]{String.valueOf(newUser.getId())};
        try {
            services.getProcedureExecutor().executeUpdateProcedure(SPROC_DEL_PHONE_NR, sprocParameters);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    private void addPhoneNumbers(UserDomainObject newUser) {
        CompositeDatabaseCommand addPhoneNumbersCommand = new CompositeDatabaseCommand();
        Set phoneNumbers = newUser.getPhoneNumbers();
        for (Iterator iterator = phoneNumbers.iterator(); iterator.hasNext(); ) {
            PhoneNumber phoneNumber = (PhoneNumber) iterator.next();
            addPhoneNumbersCommand.add(new InsertIntoTableDatabaseCommand("phones", new String[][]{
                    {"user_id", "" + newUser.getId()},
                    {"number", phoneNumber.getNumber()},
                    {"phonetype_id", "" + phoneNumber.getType().getId()}
            }));
        }
        services.getDatabase().execute(addPhoneNumbersCommand);
    }

    /**
     * @deprecated
     */
    public String[] getRoleNames(UserDomainObject user) {
        try {
            final Object[] parameters = new String[]{"" + user.getId()};
            return (String[]) services.getProcedureExecutor().executeProcedure(SPROC_GET_USER_ROLES, parameters, new StringArrayResultSetHandler());
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public String[] getAllRoleNames() {
        try {
            final Object[] parameters = new String[]{};
            String[] roleNamesMinusUsers = (String[]) services.getProcedureExecutor().executeProcedure(SPROC_GET_ALL_ROLES, parameters, new StringArrayResultSetHandler());

            Set roleNamesSet = new HashSet();
            for (int i = 0; i < roleNamesMinusUsers.length; i += 2) {
                String roleName = roleNamesMinusUsers[i + 1];
                roleNamesSet.add(roleName);
            }

            roleNamesSet.add(getRole(RoleId.USERS).getName());

            String[] roleNames = (String[]) roleNamesSet.toArray(new String[roleNamesSet.size()]);
            Arrays.sort(roleNames);

            return roleNames;
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public void addRoleNames(String[] externalRoleNames) {
        for (int i = 0; i < externalRoleNames.length; i++) {
            String externalRoleName = externalRoleNames[i];
            addRole(externalRoleName);
        }
    }

    public UserDomainObject[] getUsers(boolean includeUserExtern, boolean includeInactiveUsers) {
        return userRepository.findAll(includeUserExtern, includeInactiveUsers).stream()
                .map(this::toDomainObject)
                .toArray(UserDomainObject[]::new);
    }

    /**
     * @since 4.1.3
     */
    public UserDomainObject[] getUsersByEmail(String email) {
        return userRepository.findByEmail(email).stream()
                .map(this::toDomainObject)
                .toArray(UserDomainObject[]::new);
    }

    public UserDomainObject[] getAllUsersWithRole(RoleDomainObject role) {
        try {
            if (null == role) {
                return new UserDomainObject[]{};
            }
            final Object[] parameters = new String[]{"" + role.getId()};
            String[] usersWithRole = (String[]) services.getProcedureExecutor().executeProcedure(SPROC_GET_USERS_WHO_BELONGS_TO_ROLE, parameters, new StringArrayResultSetHandler());
            UserDomainObject[] result = new UserDomainObject[usersWithRole.length / 2];

            for (int i = 0; i < result.length; i++) {
                String userIdStr = usersWithRole[i * 2];
                result[i] = getUser(Integer.parseInt(userIdStr));
            }
            return result;
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public synchronized RoleDomainObject addRole(String roleName) {
        RoleDomainObject role = getRoleByName(roleName);
        if (null == role) {
            role = new RoleDomainObject(roleName);
            try {
                addRole(role);
            } catch (UserAndRoleRegistryException e) {
                throw new UnhandledException(e);
            }
        }
        return role;
    }

    void addRole(final RoleDomainObject role) throws RoleAlreadyExistsException, NameTooLongException {
        try {
            final int unionOfPermissionSetIds = getUnionOfRolePermissionIds(role);
            final int newRoleId = ((Number) services.getDatabase().execute(new TransactionDatabaseCommand() {
                public Object executeInTransaction(DatabaseConnection connection) throws DatabaseException {
                    return connection.executeUpdateAndGetGeneratedKey(SQL_INSERT_INTO_ROLES, new String[]{
                            role.getName(),
                            ""
                                    + unionOfPermissionSetIds});
                }
            })).intValue();
            role.setId(new RoleId(newRoleId));
        } catch (IntegrityConstraintViolationException icvse) {
            throw new RoleAlreadyExistsException("A role with the name \"" + role.getName()
                    + "\" already exists.");
        } catch (StringTruncationException stse) {
            throw new NameTooLongException("Role name too long: " + role.getName());
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    private int getUnionOfRolePermissionIds(RoleDomainObject role) {
        int unionOfPermissionSetIds = 0;
        RolePermissionDomainObject[] rolePermissions = role.getPermissions();
        for (int i = 0; i < rolePermissions.length; i++) {
            RolePermissionDomainObject rolePermission = rolePermissions[i];
            unionOfPermissionSetIds |= rolePermission.getId();
        }
        return unionOfPermissionSetIds;
    }

    public void deleteRole(RoleDomainObject role) {
        if (null == role) {
            return;
        }
        try {
            DatabaseCommand databaseCommand = new CompositeDatabaseCommand(new DatabaseCommand[]{
                    new DeleteWhereColumnsEqualDatabaseCommand("roles_rights", "role_id", "" + role.getId()),
                    new DeleteWhereColumnsEqualDatabaseCommand("user_roles_crossref", "role_id", "" + role.getId()),
                    new DeleteWhereColumnsEqualDatabaseCommand("roles", "role_id", "" + role.getId()),
            });
            services.getDatabase().execute(databaseCommand);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public RoleDomainObject[] getAllRoles() {
        return getRoles(SQL_SELECT_ALL_ROLES);
    }

    public RoleDomainObject[] getAllRolesExceptUsersRole() {
        return getRoles(SQL_SELECT_ALL_ROLES_EXCEPT_USERS_ROLE);
    }

    private RoleDomainObject[] getRoles(String rolesSql) {
        try {
            final Object[] parameters = new String[0];
            String[][] sqlRows = (String[][]) services.getDatabase().execute(new SqlQueryCommand(rolesSql, parameters, Utility.STRING_ARRAY_ARRAY_HANDLER));
            RoleDomainObject[] roles = new RoleDomainObject[sqlRows.length];
            for (int i = 0; i < sqlRows.length; i++) {
                roles[i] = getRoleFromSqlResult(sqlRows[i]);
            }
            return roles;
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public RoleDomainObject getRoleById(int roleId) {
        try {
            final Object[] parameters = new String[]{"" + roleId};
            String[] sqlResult = (String[]) services.getDatabase().execute(new SqlQueryCommand(SQL_SELECT_ROLE_BY_ID, parameters, Utility.STRING_ARRAY_HANDLER));
            return getRoleFromSqlResult(sqlResult);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public RoleDomainObject getRoleByName(String wantedRoleName) {
        try {
            final Object[] parameters = new String[]{wantedRoleName};
            String[] sqlResult = (String[]) services.getDatabase().execute(new SqlQueryCommand(SQL_SELECT_ROLE_BY_NAME, parameters, Utility.STRING_ARRAY_HANDLER));
            return getRoleFromSqlResult(sqlResult);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public RoleDomainObject getRoleFromSqlResult(String[] sqlResult) {
        RoleDomainObject role = null;
        if (sqlResult.length > 0) {
            int roleId = Integer.parseInt(sqlResult[0]);
            String roleName = sqlResult[1];
            int adminRoleId = Integer.parseInt(sqlResult[2]);
            int unionOfRolePermissionIds = Integer.parseInt(sqlResult[3]);
            role = new RoleDomainObject(new RoleId(roleId), roleName, adminRoleId);
            role.addUnionOfPermissionIdsToRole(unionOfRolePermissionIds);
        }
        return role;
    }

    public UserDomainObject[] getAllUsers() {
        return getUsers(true, true);
    }

    public UserDomainObject[] findUsersByNamePrefix(String namePrefix, boolean includeInactiveUsers) {
        return userRepository.findByNamePrefix(namePrefix, includeInactiveUsers).stream()
                .map(this::toDomainObject)
                .toArray(UserDomainObject[]::new);
    }


    public void initUserPhoneNumbers(UserDomainObject user) {
        PhoneNumber[] phoneNbr = getUserPhoneNumbers(user.getId());
        for (int i = 0; i < phoneNbr.length; i++) {
            PhoneNumberType type = phoneNbr[i].getType();
            String number = phoneNbr[i].getNumber();

            user.addPhoneNumber(new PhoneNumber(number, type));
        }
    }

    public void initUserRoles(UserDomainObject user) {
        user.setRoleIds(getRoleReferencesForUser(user));
    }

    public void initUserUserAdminRoles(UserDomainObject user) {
        user.setUserAdminRolesIds(getUserAdminRolesReferencesForUser(user));
    }

    public void saveRole(RoleDomainObject role) throws NameTooLongException, RoleAlreadyExistsException {
        if (0 == role.getId().intValue()) {
            addRole(role);
        } else {
            saveExistingRole(role);
        }
    }

    private void saveExistingRole(RoleDomainObject role) {
        int unionOfRolePermissionIds = getUnionOfRolePermissionIds(role);
        try {
            final Object[] parameters = new String[]{
                    role.getName(),
                    "" + unionOfRolePermissionIds, "" + role.getId()};
            services.getDatabase().execute(new SqlUpdateCommand("UPDATE roles SET role_name = ?, permissions = ? WHERE role_id = ?", parameters));
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public PhoneNumber[] getUserPhoneNumbers(int userToChangeId) {
        try {
            final Object[] parameters = new String[]{
                    "" + userToChangeId};
            String[][] phoneNumberData = (String[][]) services.getDatabase().execute(new SqlQueryCommand(
                    "SELECT   phones.number, phones.phonetype_id\n"
                            + "FROM   phones\n"
                            + "WHERE  phones.user_id = ?", parameters, Utility.STRING_ARRAY_ARRAY_HANDLER
            ));
            List phoneNumbers = new ArrayList();
            for (int i = 0; i < phoneNumberData.length; i++) {
                String[] row = phoneNumberData[i];
                String phoneNumberString = row[0];
                int phoneTypeId = Integer.parseInt(row[1]);
                PhoneNumberType phoneNumberType = PhoneNumberType.getPhoneNumberTypeById(phoneTypeId);
                PhoneNumber phoneNumber = new PhoneNumber(phoneNumberString, phoneNumberType);
                phoneNumbers.add(phoneNumber);
            }
            return (PhoneNumber[]) phoneNumbers.toArray(new PhoneNumber[phoneNumbers.size()]);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    private RoleId[] getUserAdminRolesReferencesForUser(UserDomainObject loggedOnUser) {
        try {
            final Object[] parameters = new String[]{"" + loggedOnUser.getId()};

            String[] roleIds = (String[]) services.getDatabase().execute(new SqlQueryCommand("SELECT role_id\n"
                    + "FROM useradmin_role_crossref\n"
                    + "WHERE user_id = ?", parameters, Utility.STRING_ARRAY_HANDLER));

            List<RoleId> useradminPermissibleRolesList = new ArrayList<>(roleIds.length);
            for (String roleId : roleIds) {
                useradminPermissibleRolesList.add(new RoleId(Integer.parseInt(roleId)));
            }
            return useradminPermissibleRolesList.toArray(new RoleId[useradminPermissibleRolesList.size()]);
        } catch (DatabaseException e) {
            throw new UnhandledException(e);
        }
    }

    public UserDomainObject getDefaultUser() {
        return getUser(UserDomainObject.DEFAULT_USER_ID);
    }

    public RoleDomainObject getRole(RoleId roleId) {
        return getRoleById(roleId.intValue());
    }

    public UserDomainObject getUserByIpAddress(String ipAddress) {
        long ip;
        try {
            ip = Utility.ipStringToLong(ipAddress);
        } catch (IllegalArgumentException nfe) {
            log.debug("Failed to parse ip address " + ipAddress);
            return null;
        }

        String sqlStr = "select users.user_id from users,ip_accesses"
                + " where users.user_id = ip_accesses.user_id"
                + " and ip_accesses.ip_start <= ?"
                + " and ip_accesses.ip_end >= ?";

        final Object[] parameters = new String[]{"" + ip,
                "" + ip};
        String userIdString = (String) services.getDatabase().execute(new SqlQueryCommand(sqlStr, parameters, Utility.SINGLE_STRING_HANDLER));

        if (null != userIdString) {
            UserDomainObject user = getUser(Integer.parseInt(userIdString));
            if (user != null) {
                user.setAuthenticatedByIp(true);
            }

            return user;
        }
        return null;
    }

    /**
     * @since 4.0.17
     */
    public LoginPasswordManager getLoginPasswordManager() {
        return loginPasswordManager;
    }


    /**
     * Encrypts every internal user's unencrypted non-blank login password if encryption is enabled.
     *
     * @since 4.0.17
     */
    public void encryptUnencryptedUsersLoginPasswords() {
        if (services.getConfig().isLoginPasswordEncryptionEnabled()) {
            for (UserDomainObject user : getAllUsers()) {
                if (!user.isImcmsExternal() && !user.isPasswordEncrypted() && StringUtils.isNotBlank(user.getPassword())) {
                    saveUser(user);
                }
            }
        }
    }
}