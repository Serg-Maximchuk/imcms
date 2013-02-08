package imcode.server.user;

import com.imcode.imcms.api.ContentLanguage;
import com.imcode.imcms.api.DocGetterCallback;
import imcode.server.Imcms;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentPermissionSetDomainObject;
import imcode.server.document.DocumentPermissionSetTypeDomainObject;
import imcode.server.document.RoleIdToDocumentPermissionSetTypeMappings;
import imcode.server.document.TemplateGroupDomainObject;
import imcode.server.document.TextDocumentPermissionSetDomainObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.apache.commons.lang.UnhandledException;

import com.imcode.imcms.api.Meta;

import javax.persistence.*;

/**
 * JPA mapping (currently) does not replace or extend functionality provided by ImcmsAuthenticatorAndUserAndRoleMapper.
 * Must not be used for updates.
 */
@Entity(name = "User")
@Table(name = "users")
public class UserDomainObject implements Cloneable, Serializable {

    /**
     * @since 4.0.7
     */
    enum PasswordType {
        UNENCRYPTED, ENCRYPTED
    }

    /**
     * @since 4.0.7
     */
    public static final class PasswordReset {
        private final String id;
        private final long time;

        private PasswordReset(String id, long time) {
            this.id = id;
            this.time = time;
        }

        public String getId() {
            return id;
        }

        public long getTime() {
            return time;
        }

        @Override
        public String toString() {
            return String.format("User.PasswordReset(id=%s, time=%s)", id, time);
        }
    }

    public static final int DEFAULT_USER_ID = 2;

    @Id
    @Column(name = "user_id")
    protected int id;

    @Column(name = "login_name")
    private String loginName = "" ;

    @Column(name = "login_password")
    private String password;

    @Column(name = "first_name")
    private String firstName = "";

    @Column(name = "last_name")
    private String lastName = "";

    private String title = "";
    private String company = "";
    private String address = "";
    private String city = "";
    private String zip = "";
    private String country = "";

    @Transient
    private String province = "";

    @Column(name = "email")
    private String emailAddress = "";
    private boolean active = true;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "language")
    private String languageIso639_2;

    @Transient
    private TemplateGroupDomainObject templateGroup;

    @Column(name = "external")
    private boolean imcmsExternal;

    @Transient
    private HashSet phoneNumbers = new HashSet();

    @Transient
    RoleIds roleIds = UserDomainObject.createRolesSetWithUserRole();

    @Transient
    protected RoleIds userAdminRoleIds = new RoleIds();

    /** Http session id.*/
    @Column(name = "session_id")
    private String sessionId;

    @Transient
    private boolean authenticatedByIp;

    /**
     * FIXME - Kludge to get context path into template methods *
     */
    @Transient
    private String currentContextPath;

    /**
     * @since 4.0.7
     */
    @Enumerated
    @Column(name = "login_password_is_encrypted")
    private PasswordType passwordType = PasswordType.UNENCRYPTED;

    /**
     * @since 4.0.7
     */
    @Transient
    private PasswordReset passwordReset = null;

    @Transient
    private volatile DocGetterCallback docGetterCallbackRef;

    public UserDomainObject() {}

    public UserDomainObject(int id) {
        this.id = id;
    }

    private static RoleIds createRolesSetWithUserRole() {
        RoleIds newRoleIds = new RoleIds();
        newRoleIds.add(RoleId.USERS);

        return newRoleIds;
    }

    public Object clone() {
        try {
            UserDomainObject clone = (UserDomainObject)super.clone();
            clone.roleIds = (RoleIds) roleIds.clone();
            clone.userAdminRoleIds = (RoleIds)userAdminRoleIds.clone();
            clone.phoneNumbers = (HashSet)phoneNumbers.clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new UnhandledException(e);
        }
    }

    /**
     * get user-id
     */
    public int getId() {
        return id;
    }

    /**
     * set user-id
     */
    public void setId( int id ) {
        this.id = id;
    }

    /**
     * get login name (username)
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * set login name (username)
     */
    public void setLoginName( String loginName ) {
        this.loginName = loginName;
    }

    /**
     * get full name
     */
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    /**
     * get first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * set first name
     */
    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    /**
     * get last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * set last name
     */
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    /**
     * set title
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * get title
     */
    public String getTitle() {
        return title;
    }

    /**
     * set company
     */
    public void setCompany( String company ) {
        this.company = company;
    }

    /**
     * get company
     */
    public String getCompany() {
        return company;
    }

    /**
     * set address
     */
    public void setAddress( String address ) {
        this.address = address;
    }

    /**
     * get address
     */
    public String getAddress() {
        return address;
    }

    /**
     * set city
     */
    public void setCity( String city ) {
        this.city = city;
    }

    /**
     * get city
     */
    public String getCity() {
        return city;
    }

    /**
     * set zip
     */
    public void setZip( String zip ) {
        this.zip = zip;
    }

    /**
     * get zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * set country
     */
    public void setCountry( String country ) {
        this.country = country;
    }

    /**
     * get country
     */
    public String getCountry() {
        return country;
    }

    public void setProvince( String province ) {
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    /**
     * Return the users e-mail address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Set the users e-mail address
     */
    public void setEmailAddress( String emailAddress ) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return document getter callback associated with this user.
     */
    public DocGetterCallback getDocGetterCallback() {
        return docGetterCallbackRef;
    }

    public void setDocGetterCallback(DocGetterCallback callback) {
        this.docGetterCallbackRef  = callback;
    }    

    /**
     * Get the users workphone
     */
    @Deprecated
    public String getWorkPhone() {
        return getFirstPhoneNumberOfTypeAsString(PhoneNumberType.WORK);
    }

    private String getFirstPhoneNumberOfTypeAsString(PhoneNumberType phoneNumberType) {
        PhoneNumber firstPhoneNumberOfType = getFirstPhoneNumberOfType(phoneNumberType);
        String number = null ;
        if (null != firstPhoneNumberOfType) {
            number = firstPhoneNumberOfType.getNumber();
        }
        return number;
    }

    private PhoneNumber getFirstPhoneNumberOfType(PhoneNumberType phoneNumberType) {
        Collection phoneNumbersOfType = getPhoneNumbersOfType(phoneNumberType);
        Iterator iterator = phoneNumbersOfType.iterator() ;
        if (iterator.hasNext()) {
            return (PhoneNumber) iterator.next() ;
        }
        return null ;
    }

    public Set getPhoneNumbersOfType(final PhoneNumberType phoneNumberType) {
        return new HashSet(CollectionUtils.select(phoneNumbers, new PhoneNumberOfTypePredicate(phoneNumberType)));
    }

    /**
     * Set the users workphone
     * @deprecated Use {@link #addPhoneNumber(PhoneNumber)}
     */
    public void setWorkPhone( String workphone ) {
        replacePhoneNumbersOfType(workphone, PhoneNumberType.WORK);
    }

    public void replacePhoneNumbersOfType(String number, PhoneNumberType type) {
        removePhoneNumbersOfType(type);
        addPhoneNumber( new PhoneNumber(number, type));
    }

    private void removePhoneNumbersOfType(PhoneNumberType phoneNumberType) {
        CollectionUtils.filter(phoneNumbers, new NotPredicate(new PhoneNumberOfTypePredicate(phoneNumberType)));
    }

    /**
     * Get the users mobilephone
     * @deprecated Use {@link #getPhoneNumbersOfType(PhoneNumberType)}
     */
    public String getMobilePhone() {
        return getFirstPhoneNumberOfTypeAsString(PhoneNumberType.MOBILE) ;
    }

    /**
     * Set the users mobilephone
     * @deprecated Use {@link #addPhoneNumber(PhoneNumber)}
     */
    public void setMobilePhone( String mobilephone ) {
        replacePhoneNumbersOfType(mobilephone, PhoneNumberType.MOBILE);
    }

    /**
     * Get the users homephone
     * @deprecated Use {@link #getPhoneNumbersOfType(PhoneNumberType)}
     */
    public String getHomePhone() {
        return getFirstPhoneNumberOfTypeAsString(PhoneNumberType.HOME);
    }

    /**
     * Set the users homephone
     * @deprecated Use {@link #addPhoneNumber(PhoneNumber)}
     */
    public void setHomePhone( String homephone ) {
        replacePhoneNumbersOfType(homephone, PhoneNumberType.HOME);
    }

    /**
     * Get the users faxphone
     * @deprecated Use {@link #getPhoneNumbersOfType(PhoneNumberType)}
     */
    public String getFaxPhone() {
        return getFirstPhoneNumberOfTypeAsString(PhoneNumberType.FAX);
    }

    /**
     * Set the users faxpohne
     * @deprecated Use {@link #addPhoneNumber(PhoneNumber)}
     */
    public void setFaxPhone( String faxphone ) {
        replacePhoneNumbersOfType(faxphone, PhoneNumberType.FAX);
    }

    /**
     * Get the users otherphone
     * @deprecated Use {@link #getPhoneNumbersOfType(PhoneNumberType)}
     */
    public String getOtherPhone() {
        return getFirstPhoneNumberOfTypeAsString(PhoneNumberType.OTHER);
    }

    /**
     * Set the users otherpohne
     * @deprecated Use {@link #addPhoneNumber(PhoneNumber)}
     */
    public void setOtherPhone( String otherphone ) {
        replacePhoneNumbersOfType(otherphone, PhoneNumberType.OTHER);
    }

    /**
     * Set whether the user is allowed to log in
     */
    public void setActive( boolean active ) {
        this.active = active;
    }

    /**
     * Check whether the user is allowed to log in
     */
    public boolean isActive() {
        return active;
    }

    /**
     * set create_date
     * @param createDate
     */
    public void setCreateDate( Date createDate ) {
        this.createDate = (Date) createDate.clone();
    }

    /**
     * get create_date
     */
    public Date getCreateDate() {
        return (Date) ( null == createDate ? null : createDate.clone() ) ;
    }

    /**
     * set template group
     */
    public void setTemplateGroup( TemplateGroupDomainObject templateGroup ) {
        this.templateGroup = templateGroup;
    }

    /**
     * get template group
     */
    public TemplateGroupDomainObject getTemplateGroup() {
        return templateGroup;
    }

    /**
     * Return the users language
     */
    public String getLanguageIso639_2() {
        return languageIso639_2;
    }

    /**
     * Set the users language
     */
    public void setLanguageIso639_2( String languageIso639_2 ) {
        this.languageIso639_2 = languageIso639_2;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public boolean isImcmsExternal() {
        return imcmsExternal;
    }

    public void setImcmsExternal( boolean imcmsExternal ) {
        this.imcmsExternal = imcmsExternal;
    }

    public void addRoleId( RoleId role ) {
        roleIds.add( role );
    }

    public void removeRoleId( RoleId roleId ) {
        if ( !RoleId.USERS.equals( roleId ) ) {
            roleIds.remove( roleId );
        }
    }

    public void setRoleIds( RoleId[] roleIds ) {
        this.roleIds = new RoleIds(roleIds) ;
        this.roleIds.add( RoleId.USERS );
    }



    public boolean hasRoleId( RoleId roleId ) {
        return roleIds.contains( roleId );
    }

    public RoleId[] getRoleIds() {
        return roleIds.toArray() ;
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof UserDomainObject ) ) {
            return false;
        }

        final UserDomainObject userDomainObject = (UserDomainObject)o;

        return id == userDomainObject.id;

    }

    public int hashCode() {
        return id;
    }

    public boolean isDefaultUser() {
        return DEFAULT_USER_ID == id;
    }

    public boolean isSuperAdmin() {
        return hasRoleId( RoleId.SUPERADMIN );
    }

    public boolean isUserAdminAndCanEditAtLeastOneRole() {
        return isUserAdmin() && !userAdminRoleIds.isEmpty() ;
    }

    public boolean canEdit( DocumentDomainObject document ) {
        return hasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject.RESTRICTED_2, document );
    }

    public boolean canAccess( DocumentDomainObject document ) {
        return hasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject.READ, document );
    }

    public boolean isSuperAdminOrHasFullPermissionOn( DocumentDomainObject document ) {
        return isSuperAdminOrHasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject.FULL, document );
    }

    public boolean canDefineRestrictedOneFor( DocumentDomainObject document ) {
        return isSuperAdminOrHasFullPermissionOn( document );
    }

    public boolean canDefineRestrictedTwoFor( DocumentDomainObject document ) {
        boolean hasFullPermission = isSuperAdminOrHasFullPermissionOn( document );
        boolean canEditPermissionsForDocument = canEditPermissionsFor( document );
        boolean hasAtLeastRestrictedOne = hasAtLeastRestrictedOnePermissionOn( document );
        boolean hasAtLeastRestrictedOnePermissionAndIsMorePrivilegedThanRestrictedTwo = hasAtLeastRestrictedOne
                                                                                        && document.isRestrictedOneMorePrivilegedThanRestrictedTwo();
        return hasFullPermission
               || canEditPermissionsForDocument
                  && hasAtLeastRestrictedOnePermissionAndIsMorePrivilegedThanRestrictedTwo;
    }

    private boolean hasAtLeastRestrictedOnePermissionOn( DocumentDomainObject document ) {
        return hasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject.RESTRICTED_1, document );
    }

    public String toString() {
        return "(user " + id + " \"" + loginName + "\")";
    }

    public void addPhoneNumber(PhoneNumber number) {
        phoneNumbers.add(number) ;
    }

    public void removePhoneNumber(PhoneNumber number) {
        phoneNumbers.remove(number);
    }

    /* FIXME: Current context path should be sent in a HttpServletRequest, not in an UserDomainObject. */
    public void setCurrentContextPath( String currentContextPath ) {
        this.currentContextPath = currentContextPath;
    }

    public String getCurrentContextPath() {
        return currentContextPath;
    }

    public boolean isSuperAdminOrHasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject documentPermissionSetType, DocumentDomainObject document ) {
        return isSuperAdmin() || hasAtLeastPermissionSetIdOn( documentPermissionSetType, document );
    }

    public boolean canEditPermissionsFor( DocumentDomainObject document ) {
        return getPermissionSetFor( document ).getEditPermissions();
    }

    public boolean canSetDocumentPermissionSetTypeForRoleIdOnDocument( DocumentPermissionSetTypeDomainObject documentPermissionSetType, RoleId roleId,
                                                                       DocumentDomainObject document ) {
        if ( !canEditPermissionsFor( document ) ) {
            return false;
        }
        DocumentPermissionSetTypeDomainObject currentPermissionSetType = document.getDocumentPermissionSetTypeForRoleId( roleId );
        boolean userIsSuperAdminOrHasAtLeastTheCurrentPermissionSet = isSuperAdminOrHasAtLeastPermissionSetIdOn( currentPermissionSetType, document );
        boolean userIsSuperAdminOrHasAtLeastTheWantedPermissionSet = isSuperAdminOrHasAtLeastPermissionSetIdOn( documentPermissionSetType, document );
        boolean userHasAtLeastRestrictedOne = hasAtLeastRestrictedOnePermissionOn( document );
        boolean changingRestrictedTwo = DocumentPermissionSetTypeDomainObject.RESTRICTED_2.equals(documentPermissionSetType)
                                        || DocumentPermissionSetTypeDomainObject.RESTRICTED_2.equals(currentPermissionSetType);
        boolean canDefineRestrictedTwoForDocument = canDefineRestrictedTwoFor( document );

        return userIsSuperAdminOrHasAtLeastTheWantedPermissionSet
               && userIsSuperAdminOrHasAtLeastTheCurrentPermissionSet
               && ( !changingRestrictedTwo || !userHasAtLeastRestrictedOne
                    || canDefineRestrictedTwoForDocument );

    }

    public boolean canCreateDocumentOfTypeIdFromParent( int documentTypeId, DocumentDomainObject parent ) {
        TextDocumentPermissionSetDomainObject documentPermissionSet = (TextDocumentPermissionSetDomainObject)getPermissionSetFor( parent );
        Set allowedDocumentTypeIds = documentPermissionSet.getAllowedDocumentTypeIds();
        return allowedDocumentTypeIds.contains(new Integer(documentTypeId) );
    }

    public DocumentPermissionSetDomainObject getPermissionSetFor( DocumentDomainObject document ) {
        DocumentPermissionSetTypeDomainObject permissionSetId = getDocumentPermissionSetTypeFor( document );
        if ( DocumentPermissionSetTypeDomainObject.FULL.equals(permissionSetId) ) {
            return DocumentPermissionSetDomainObject.FULL;
        } else if ( DocumentPermissionSetTypeDomainObject.READ.equals(permissionSetId) ) {
            return DocumentPermissionSetDomainObject.READ;
        } else if ( DocumentPermissionSetTypeDomainObject.RESTRICTED_1.equals(permissionSetId) ) {
            return document.getPermissionSets().getRestricted1();
        } else if ( DocumentPermissionSetTypeDomainObject.RESTRICTED_2.equals(permissionSetId) ) {
            return document.getPermissionSets().getRestricted2();
        } else {
            return DocumentPermissionSetDomainObject.NONE;
        }
    }

    /**
     * Returns most privileged permission set type for the provided doc.
     *
     * If doc is null returns {@link DocumentPermissionSetTypeDomainObject#NONE}
     * If user is in a SUPER_ADMIN role returns {@link DocumentPermissionSetTypeDomainObject#FULL}
     * Otherwise searches for most privileged perm set in the intersection of user roles and doc's roles.
     *
     * @param document
     *
     * @return most privileged permission set for the provided doc.
     */
    public DocumentPermissionSetTypeDomainObject getDocumentPermissionSetTypeFor(DocumentDomainObject document) {
        if (null == document)
            return DocumentPermissionSetTypeDomainObject.NONE;

        if (isSuperAdmin())
            return DocumentPermissionSetTypeDomainObject.FULL;

        RoleIdToDocumentPermissionSetTypeMappings roleIdsMappedToDocumentPermissionSetTypes =
                document.getRoleIdsMappedToDocumentPermissionSetTypes();

        DocumentPermissionSetTypeDomainObject mostPrivilegedPermissionSetIdFoundYet =
                DocumentPermissionSetTypeDomainObject.NONE;

        for (RoleId roleId: getRoleIds()) {
            DocumentPermissionSetTypeDomainObject documentPermissionSetType =
                    roleIdsMappedToDocumentPermissionSetTypes.getPermissionSetTypeForRole(roleId);

            if (documentPermissionSetType.isMorePrivilegedThan(mostPrivilegedPermissionSetIdFoundYet)) {
                mostPrivilegedPermissionSetIdFoundYet = documentPermissionSetType;

                if (mostPrivilegedPermissionSetIdFoundYet == DocumentPermissionSetTypeDomainObject.FULL) {
                    break;
                }
            }
        }

        return mostPrivilegedPermissionSetIdFoundYet;
    }

    public boolean hasAtLeastPermissionSetIdOn( DocumentPermissionSetTypeDomainObject leastPrivilegedPermissionSetIdWanted,
                                                DocumentDomainObject document ) {
        DocumentPermissionSetTypeDomainObject usersDocumentPermissionSetType = getDocumentPermissionSetTypeFor(document);
        return usersDocumentPermissionSetType.isAtLeastAsPrivilegedAs(leastPrivilegedPermissionSetIdWanted);
    }

    public boolean canAddDocumentToAnyMenu( DocumentDomainObject document ) {
        if (null == document) {
            return false ;
        }
        boolean canEdit = canEdit(document);
        boolean linkableByOtherUsers = document.isLinkableByOtherUsers();
        return canEdit || linkableByOtherUsers;
    }

    public boolean canSearchFor( DocumentDomainObject document ) {
        boolean canSearchForDocument = false;
        if ( document.isSearchDisabled() ) {
            if ( isSuperAdmin() ) {
                canSearchForDocument = true;
            }
        } else {
            if ( document.isPublished() ) {
                canSearchForDocument = document.isLinkedForUnauthorizedUsers() || canAccess( document );
            } else {
                canSearchForDocument = canEdit( document );
            }
        }
        return canSearchForDocument;
    }

    public boolean canEditDocumentInformationFor( DocumentDomainObject document ) {
        return getPermissionSetFor( document ).getEditDocumentInformation();
    }

    public boolean canAccessAdminPages() {
        RolePermissionDomainObject rolePermissionToAccessAdminPages = RoleDomainObject.ADMIN_PAGES_PERMISSION;
        return isSuperAdmin() || isUserAdminAndCanEditAtLeastOneRole() || hasRoleWithPermission( rolePermissionToAccessAdminPages );
    }

    public boolean hasRoleWithPermission( RolePermissionDomainObject rolePermission ) {
        ImcmsAuthenticatorAndUserAndRoleMapper imcmsAuthenticatorAndUserAndRoleMapper = Imcms.getServices().getImcmsAuthenticatorAndUserAndRoleMapper();
        RoleId[] roleReferencesArray = roleIds.toArray();
        for ( int i = 0; i < roleReferencesArray.length; i++ ) {
            RoleId roleId = roleReferencesArray[i];
            if ( imcmsAuthenticatorAndUserAndRoleMapper.getRole(roleId).hasPermission( rolePermission ) ) {
                return true;
            }
        }
        return false;
    }

    public boolean canSeeDocumentInMenus( DocumentDomainObject document ) {
        return document.isActive() && canSeeDocumentWhenEditingMenus( document )
        	&& languageIsActive(document);
    }
    
    private boolean languageIsActive(DocumentDomainObject document) {
    	ContentLanguage currentLanguage = getDocGetterCallback().contentLanguages().preferred();
    	Meta meta = document.getMeta();
    	boolean enabled = meta.getEnabledLanguages().contains(currentLanguage);
    	
    	return enabled ||
    		meta.getDisabledLanguageShowSetting() == Meta.DisabledLanguageShowSetting.SHOW_IN_DEFAULT_LANGUAGE;
    } 

    public boolean canSeeDocumentWhenEditingMenus( DocumentDomainObject document ) {
        return document.isLinkedForUnauthorizedUsers() || canAccess( document );
    }

    public Set getPhoneNumbers() {
        return Collections.unmodifiableSet(phoneNumbers);
    }

    public RoleId[] getUserAdminRoleIds() {
        return userAdminRoleIds.toArray();
    }

    public void setUserAdminRolesIds(RoleId[] userAdminRoleReferences) {
        userAdminRoleIds = new RoleIds(userAdminRoleReferences);
    }

    public boolean isUserAdminAndNotSuperAdmin() {
        return isUserAdmin() && !isSuperAdmin() ;
    }

    public boolean canEditRolesFor(UserDomainObject editedUser) {
        return isSuperAdmin() || canEditAsUserAdmin(editedUser) && !equals(editedUser) ;
    }

    public void removeUserAdminRoleId(RoleId role) {
        userAdminRoleIds.remove(role) ;
    }

    public boolean canEdit(UserDomainObject editedUser) {
        return equals(editedUser) || isSuperAdmin() || canEditAsUserAdmin(editedUser) ;
    }

    public boolean canEditAsUserAdmin(UserDomainObject editedUser) {
        return isUserAdminAndNotSuperAdmin() && (editedUser.isNew() || canEditRolesAccordingToUserAdminRoles(editedUser) ) ;
    }

    public boolean canEditRolesAccordingToUserAdminRoles(UserDomainObject editedUser) {
        return CollectionUtils.containsAny(editedUser.roleIds.asSet(), userAdminRoleIds.asSet());
    }

    public boolean isUserAdmin() {
        return hasRoleId( RoleId.USERADMIN ) ;
    }

    public boolean isNew() {
        return 0 == id;
    }

    private static class PhoneNumberOfTypePredicate implements Predicate {
        private final PhoneNumberType phoneNumberType;

        PhoneNumberOfTypePredicate(PhoneNumberType phoneNumberType) {
            this.phoneNumberType = phoneNumberType;
        }

        public boolean evaluate(Object object) {
            PhoneNumber phoneNumber = (PhoneNumber) object ;
            return phoneNumber.getType().equals(phoneNumberType) ;
        }
    }
    
    public boolean hasAdminPanelForDocument(DocumentDomainObject document) {
        return !(null == document || !(canEdit(document) || isUserAdminAndCanEditAtLeastOneRole() || canAccessAdminPages()));
    }

    /**
     * @return if this user was authenticated by IP.
     *
     * @see imcode.server.Config#isDenyMultipleUserLogin()
     * @see com.imcode.imcms.servlet.ImcmsFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public boolean isAuthenticatedByIp() {
        return authenticatedByIp;
    }

    public void setAuthenticatedByIp(boolean authenticatedByIp) {
        this.authenticatedByIp = authenticatedByIp;
    }

    /**
     * @since 4.0.7
     */
    public boolean isPasswordEncrypted() {
        return passwordType == PasswordType.ENCRYPTED;
    }

    /**
     * @since 4.0.7
     */
    public PasswordReset getPasswordReset() {
        return passwordReset;
    }

    /**
     * @since 4.0.7
     */
    public boolean hasPasswordReset() {
        return passwordReset != null;
    }

    /**
     * Returns password.
     * The password might be a plain text or encrypted.
     * Use {@link #isPasswordEncrypted()} to test if password is encrypted.
     *
     * @since 4.0.7
     */
    public String getPassword() {
        return password;
    }

    /**
     * Assign a new unencrypted password to the user and clears password reset (if present).
     *
     * @see #setPassword(String, PasswordType)
     *
     * @param password plain text password.
     *
     * @since 4.0.7
     */
    public void setPassword(String password) {
        setPassword(password, PasswordType.UNENCRYPTED);
        this.passwordReset = null;
    }

    //------------------------------------------------------------------------------------------------------------------
    // The following package private methods are used internally by ImcmsAuthenticatorAndUserAndRoleMapper
    //------------------------------------------------------------------------------------------------------------------

    /**
     * @since 4.0.7
     */
    void setPassword(String password, PasswordType passwordType) {
        this.password = password;
        this.passwordType = passwordType;
    }

    /**
     * @since 4.0.7
     */
    void setPasswordReset(String resetId, long time) {
        this.passwordReset = new PasswordReset(resetId, time);
    }
}
