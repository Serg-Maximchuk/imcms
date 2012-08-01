package imcode.server.user;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.imcode.imcms.api.P;
import imcode.server.user.ldap.MappedRoles;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;

import javax.naming.directory.SearchControls;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.imcode.net.ldap.LdapClient;
import com.imcode.net.ldap.LdapConnection;
import com.imcode.net.ldap.LdapClientException;

/**
 * The documentMapper maps LDAP attributes to Imcms internal user object.
 * The default mapping uses the popular inetOrgPerson (2.16.840.1.113730.3.2.2) schema found in Netscape Directory Server <br>
 * The inetOrgPerson is based on organizationalPerson (2.5.6.7) that is based on person (2.5.6.6) that is based on top (2.5.6.0)<br>
 * See for example * @http://www.cio.ufl.edu/projects/directory/ldap-schema/oc-INETORGPERSON.html
 * or @link http://ldap.akbkhome.com/objectclasstree/inetOrgPerson.html for details witch attributes that exists.
 */

public class LdapUserAndRoleRegistry implements Authenticator, UserAndRoleRegistry {

    private final static Logger LOG = Logger.getLogger(LdapUserAndRoleRegistry.class);

    public static final String DEFAULT_LDAP_ROLE = "LDAP";

    /** The following constanst are mapped to the Imcms internal user tables.
     /* Where the loginname is mapped to the attribute attribute
     /* and the password is ignored */

    /**
     * From person oid=2.5.6.6
     */
    private static final String PERSON_SURNAME = "sn";
    private static final String PERSON_TELEPHONE_NUMBER = "telephoneNumber";

    /**
     * From organizationalPerson oid=2.5.6.7
     */
    private static final String ORGANIZATIONALPERSON_TITLE = "title";
    private static final String ORGANIZATIONALPERSON_STATE_OR_PROVINCE_NAME = "st";
    private static final String ORGANIZATIONALPERSON_POSTAL_CODE = "postalCode";
    private static final String ORGANIZATIONALPERSON_STREET_ADRESS = "streetAddress";
    //   static final String ORGANIZATIONALPERSON_ORGANIZATIONAL_UNIT_NAME = "ou";

    /**
     * From inetOrgPerson oid=2.16.840.1.113730.3.2.2
     */
    private static final String INETORGPERSON_GIVEN_NAME = "givenName";
    private static final String INETORGPERSON_MAIL = "mail";
    private static final String INETORGPERSON_HOME_PHONE = "homePhone";
    private static final String INETORGPERSON_MOBILE = "mobile";
    private static final String INETORGPERSON_LOCALITY_NAME = "l";
    private static final String INETORGPERSON_ORGANIZATION = "o";
    static final String INETORGPERSON_USER_IDENTITY = "uid";

    private final LdapClient ldapClient;
    private LdapConnection ldapConnection;

    private final String ldapUserObjectClass;
    private final String[] ldapAttributesAutoMappedToRoles;
    private MappedRoles mappedRoles;

    private Properties userPropertyNameToLdapAttributeNameMap = new Properties();

    private final static Map DEFAULT_USER_PROPERTY_NAME_TO_LDAP_ATTRIBUTE_NAME_MAP = ArrayUtils.toMap(new String[][]{
            {"LoginName", INETORGPERSON_USER_IDENTITY},
            {"FirstName", INETORGPERSON_GIVEN_NAME},
            {"LastName", PERSON_SURNAME},
            {"Title", ORGANIZATIONALPERSON_TITLE},
            {"Company", INETORGPERSON_ORGANIZATION},
            {"Address", ORGANIZATIONALPERSON_STREET_ADRESS},
            {"City", INETORGPERSON_LOCALITY_NAME},
            {"Zip", ORGANIZATIONALPERSON_POSTAL_CODE},
            {"Province", ORGANIZATIONALPERSON_STATE_OR_PROVINCE_NAME},
            {"EmailAddress", INETORGPERSON_MAIL},
            {"WorkPhone", PERSON_TELEPHONE_NUMBER},
            {"MobilePhone", INETORGPERSON_MOBILE},
            {"HomePhone", INETORGPERSON_HOME_PHONE},
    });

    private String ldapUsername;
    private String ldapPassword;

    private static final String LDAP_USER_OBJECTCLASS__INETORGPERSON = "inetOrgPerson";
    private static final String LDAP_USER_OBJECTCLASS_DEFAULT = LDAP_USER_OBJECTCLASS__INETORGPERSON;

    public LdapUserAndRoleRegistry(Properties ldapConfig, MappedRoles mappedRoles) throws LdapClientException {
        this(ldapConfig.getProperty("LdapUrl", "ldap://localhost/"),
                ldapConfig.getProperty("LdapUserObjectClass", LDAP_USER_OBJECTCLASS_DEFAULT),
                ldapConfig.getProperty("LdapBindDn", ""),
                ldapConfig.getProperty("LdapPassword", ""),
                buildAttributesMappedToRoles(ldapConfig),
                buildUserAttributes(ldapConfig), mappedRoles);
    }

    private static String[] buildAttributesMappedToRoles(Properties ldapConfig) {
        String ldapStringOfAttributesMappedToRoles = ldapConfig.getProperty("LdapAttributesMappedToRoles", "");
        return splitStringOnCommasAndSpaces(ldapStringOfAttributesMappedToRoles);
    }

    private static Properties buildUserAttributes(Properties ldapConfig) {
        Properties ldapUserAttributes = new Properties();
        ExtendedProperties ldapUserAttributesSubset = ExtendedProperties.convertProperties(ldapConfig).subset("LdapUserAttribute");
        if (null != ldapUserAttributesSubset) {
            ldapUserAttributes.putAll(ldapUserAttributesSubset);
        }
        return ldapUserAttributes;
    }

    /**
     * @param ldapUserAttributes
     * @param ldapUrl            The full path to where the ldap-service is located _and_ the node where to start the searches, e.g.  "ldap://computername:389/CN=Users,DC=companyName,DC=com"
     * @param ldapUserName       A name that is used to log in (bind) to the ldap server
     * @param ldapPassword       A password that i used to log in (bind) to the ldap server
     */

    public LdapUserAndRoleRegistry(String ldapUrl,
                                   String ldapUserObjectClass,
                                   String ldapUserName,
                                   String ldapPassword,
                                   String[] ldapAttributesMappedToRoles,
                                   Properties ldapUserAttributes,
                                   MappedRoles mappedRoles
    ) throws LdapClientException {
        ldapAttributesAutoMappedToRoles = ldapAttributesMappedToRoles;
        initLdapUserAttributesMap(ldapUserAttributes);

        this.ldapUserObjectClass = ldapUserObjectClass;
        ldapClient = new LdapClient(ldapUrl);

        this.ldapUsername = ldapUserName;
        this.ldapPassword = ldapPassword;

        this.mappedRoles = mappedRoles;

        createLdapConnection();
    }

    private void createLdapConnection()
            throws LdapClientException {
        ldapConnection = ldapClient.bind(ldapUsername, ldapPassword);
    }

    public String[] getAllRoleNames() {
        Set<String> names = new HashSet<String>(mappedRoles.rolesNames());

        names.add(DEFAULT_LDAP_ROLE);

        return names.toArray(new String[]{});
    }

    private void initLdapUserAttributesMap(Properties ldapUserAttributes) throws LdapClientException {
        userPropertyNameToLdapAttributeNameMap.putAll(DEFAULT_USER_PROPERTY_NAME_TO_LDAP_ATTRIBUTE_NAME_MAP);
        userPropertyNameToLdapAttributeNameMap.putAll(ldapUserAttributes);
        Set badUserAttributes = new TreeSet(userPropertyNameToLdapAttributeNameMap.keySet());
        String[] capitalizedSettableUserPropertyNames = getCapitalizedSettableBeanPropertyNames(UserDomainObject.class);
        badUserAttributes.removeAll(Arrays.asList(capitalizedSettableUserPropertyNames));
        if (!badUserAttributes.isEmpty()) {
            throw new LdapClientException("Unrecognized LdapUserAttributes: "
                    + StringUtils.join(badUserAttributes.iterator(), ", "));
        }
    }

    public boolean authenticate(String loginName, String password) {
        try {
            Map userAttributes = searchForUserAttributes(loginName,
                    new String[]{LdapConnection.DISTINGUISHED_NAME});
            if (null != userAttributes) {
                String userDistinguishedName = (String) userAttributes.get(LdapConnection.DISTINGUISHED_NAME);
                return ldapClient.canBind(userDistinguishedName, password);
            }
        } catch (LdapClientException ex) {
            LOG.warn("Failed to get ldap context.", ex);
        }
        return false;
    }

    public UserDomainObject getUser(String loginName) {
        UserDomainObject result = null;

        Map<String, String> attributeMap = searchForUserAttributes(loginName, null);

        if (null != attributeMap) {
            result = createUserFromLdapAttributes(attributeMap);

            result.setLoginName(loginName);
            result.setActive(true);
        }
        return result;
    }

    private UserDomainObject createUserFromLdapAttributes(Map<String, String> ldapAttributeValues) {
        UserDomainObject newlyFoundLdapUser = new UserDomainObject();

        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(newlyFoundLdapUser);
        try {
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (null == propertyDescriptor.getWriteMethod()) {
                    continue;
                }
                String uncapitalizedPropertyName = propertyDescriptor.getName();
                String capitalizedPropertyName = StringUtils.capitalize(uncapitalizedPropertyName);
                String ldapAttributeName = userPropertyNameToLdapAttributeNameMap.getProperty(capitalizedPropertyName);
                if (null != ldapAttributeName) {
                    String ldapAttributeValue = ldapAttributeValues.get(ldapAttributeName);
                    if (null != ldapAttributeValue) {
                        BeanUtils.setProperty(newlyFoundLdapUser, uncapitalizedPropertyName, ldapAttributeValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new UnhandledException(e);
        } catch (InvocationTargetException e) {
            throw new UnhandledException(e);
        }

        return newlyFoundLdapUser;
    }

    public String[] getRoleNames(UserDomainObject user) {
        String loginName = user.getLoginName();

        Map attributeMappedRoles = searchForUserAttributes(loginName, ldapAttributesAutoMappedToRoles);
        Set<String> roles = new HashSet<String>(attributeMappedRoles.values());

        Set<String> mappedRolesNames = getMappedRoleNames(loginName);
        if (mappedRolesNames.size() == 0) {
            LOG.debug(String.format("User %s does not have LDAP attributes mapped to roles.", loginName));
        } else {
            LOG.debug(String.format("User %s have %d LDAP roles mapped to attributes: %s.", loginName,
                    mappedRolesNames.size(), Joiner.on(", ").join(mappedRolesNames)));
        }

        roles.add(DEFAULT_LDAP_ROLE);
        roles.addAll(mappedRolesNames);

        return roles.toArray(new String[roles.size()]);
    }


    /**
     * @since 4.1.6
     */
    private Set<String> getMappedRoleNames(String loginName) {
        Map<String, Set<String>> mappedAttributes = searchForUserMultiAttributes(
                loginName, mappedRoles.rolesToAttributes().attributesNames().toArray(new String[]{}));

        List<P.P2<String, String>> attributeNameValuePairs = Lists.newLinkedList();

        for (Map.Entry<String, Set<String>> mappedAttribute : mappedAttributes.entrySet()) {
            String attributeName = mappedAttribute.getKey();
            for (String attributeValue : mappedAttribute.getValue()) {
                attributeNameValuePairs.add(P.of(attributeName, attributeValue));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %s LDAP roles mapped to attributes for user %s: %s.",
                    attributeNameValuePairs.size(), loginName, Joiner.on(", ").join(attributeNameValuePairs)));
        }

        Set<String> usersAdGroupsDns = searchForUserAdGroups(loginName, mappedRoles.rolesToAdGroups().groupsDns());
        Set<String> rolesNamesMappedToAttributes = mappedRoles.rolesToAttributes().rolesNames(attributeNameValuePairs);
        Set<String> rolesNamesMappedToAdGroupsDns = mappedRoles.rolesToAdGroups().rolesNames(usersAdGroupsDns);
        ImmutableSet.Builder<String> rolesNamesBuilder = ImmutableSet.builder();

        rolesNamesBuilder.addAll(rolesNamesMappedToAttributes);
        rolesNamesBuilder.addAll(rolesNamesMappedToAdGroupsDns);

        return rolesNamesBuilder.build();
    }


    private Map<String, String> searchForUserAttributes(String loginName, String[] attributesToReturn) {
        Map<String, String> attributeMap = null;

        try {
            String ldapUserIdentifyingAttribute = userPropertyNameToLdapAttributeNameMap.getProperty("LoginName");

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(attributesToReturn);
            searchControls.setReturningObjFlag(true);
            Iterator<Map<String, String>> searchResult = ldapConnection.search("(&(objectClass={0})({1}={2}))",
                    new Object[]{ldapUserObjectClass, ldapUserIdentifyingAttribute, loginName},
                    searchControls);

            if (searchResult.hasNext()) attributeMap = searchResult.next();
        } catch (LdapClientException e) {
            LOG.warn("Could not find user " + loginName, e);
        }

        return attributeMap;
    }


    /**
     * @return a map containing attribute name mapped to values.
     * @since 4.1.6
     */
    private Map<String, Set<String>> searchForUserMultiAttributes(String loginName, String[] attributesToReturn) {
        Map<String, Set<String>> attributeMap = null;

        try {
            String ldapUserIdentifyingAttribute = userPropertyNameToLdapAttributeNameMap.getProperty("LoginName");

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(attributesToReturn);

            Iterator<Map<String, Set<String>>> searchResult = ldapConnection.searchMultivalues(
                    "(&(objectClass={0})({1}={2}))",
                    new Object[]{ldapUserObjectClass, ldapUserIdentifyingAttribute, loginName},
                    searchControls);

            if (searchResult.hasNext()) attributeMap = searchResult.next();
        } catch (LdapClientException e) {
            LOG.error("LDAP search error. User: " + loginName, e);
        }

        return attributeMap;
    }

    /**
     * Returns user's Active Directory groups dns.
     * If LDAP service provider is not an AD then the empty set will be returned.
     *
     * @param groupsDns groups dns to consider.
     * @since 4.1.11
     */
    private Set<String> searchForUserAdGroups(String loginName, Set<String> groupsDns) {
        if (userPropertyNameToLdapAttributeNameMap.getProperty("LoginName").trim().compareToIgnoreCase("sAMAccountName") != 0) {
            LOG.debug("LDAP service is not Active Directory based. No search will be performed.");
            return Collections.emptySet();
        }

        ImmutableSet.Builder<String> groupsDnsBuilder = ImmutableSet.builder();

        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{"sAMAccountName"});

            for (String groupsDn : groupsDns) {
                boolean userInRole = ldapConnection.search(
                        "(&(objectClass=user)(sAMAccountName={0})(memberOf:1.2.840.113556.1.4.1941:={1}))",
                        new Object[]{loginName, groupsDn},
                        searchControls).hasNext();

                if (userInRole) {
                    groupsDnsBuilder.add(groupsDn);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.info(String.format("User '%s' is a member of AD group '%s' - %s.",
                            loginName, groupsDn, userInRole));
                }
            }
        } catch (LdapClientException e) {
            LOG.error("LDAP search error. User: " + loginName, e);
        }

        return groupsDnsBuilder.build();
    }


    private static String[] splitStringOnCommasAndSpaces(String stringToSplit) {
        StringTokenizer attributesTokenizer = new StringTokenizer(stringToSplit, ", ");
        String[] tokens = new String[attributesTokenizer.countTokens()];
        for (int i = 0; i < tokens.length; ++i) {
            tokens[i] = attributesTokenizer.nextToken();
        }
        return tokens;
    }

    private static String[] getCapitalizedSettableBeanPropertyNames(Class beanClass) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        List settableBeanPropertyNames = new ArrayList();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (null == propertyDescriptor.getWriteMethod()) {
                continue;
            }
            String uncapitalizedPropertyName = propertyDescriptor.getName();
            String capitalizedPropertyName = StringUtils.capitalize(uncapitalizedPropertyName);
            settableBeanPropertyNames.add(capitalizedPropertyName);
        }
        return (String[]) settableBeanPropertyNames.toArray(new String[settableBeanPropertyNames.size()]);
    }

    public void setUserPropertyLdapAttribute(String userPropertyName, String ldapAttribute) {
        userPropertyNameToLdapAttributeNameMap.put(userPropertyName, ldapAttribute);
    }
}
