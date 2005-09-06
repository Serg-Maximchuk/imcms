package com.imcode.imcms.servlet.superadmin;

import com.imcode.imcms.api.NoPermissionException;
import com.imcode.imcms.flow.DispatchCommand;
import com.imcode.imcms.flow.OkCancelPage;
import imcode.server.Imcms;
import imcode.server.ImcmsServices;
import imcode.server.user.PhoneNumber;
import imcode.server.user.PhoneNumberType;
import imcode.server.user.RoleDomainObject;
import imcode.server.user.RoleId;
import imcode.server.user.UserDomainObject;
import imcode.util.*;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserEditorPage extends OkCancelPage {
    public static final String REQUEST_PARAMETER__LOGIN_NAME = "login_name";
    public static final String REQUEST_PARAMETER__PASSWORD1 = "password1";
    public static final String REQUEST_PARAMETER__FIRST_NAME = "first_name";
    public static final String REQUEST_PARAMETER__LAST_NAME = "last_name";
    public static final String REQUEST_PARAMETER__TITLE = "title";
    public static final String REQUEST_PARAMETER__COMPANY = "company";
    public static final String REQUEST_PARAMETER__ADDRESS = "address";
    public static final String REQUEST_PARAMETER__CITY = "city";
    public static final String REQUEST_PARAMETER__ZIP = "zip";
    public static final String REQUEST_PARAMETER__COUNTRY = "country";
    public static final String REQUEST_PARAMETER__DISTRICT = "county";
    public static final String REQUEST_PARAMETER__EMAIL = "email";
    public static final String REQUEST_PARAMETER__LANGUAGE = "lang_id";
    public static final String REQUEST_PARAMETER__ACTIVE = "active";
    public static final String REQUEST_PARAMETER__PASSWORD2 = "password2";
    public static final String REQUEST_PARAMETER__ROLE_IDS = "role_ids";
    public static final String REQUEST_PARAMETER__USER_ADMIN_ROLE_IDS = "user_admin_role_ids";

    public static final String REQUEST_PARAMETER__ADD_PHONE_NUMBER = "add_phone_number";
    public static final String REQUEST_PARAMETER__EDIT_PHONE_NUMBER = "edit_phone_number";
    public static final String REQUEST_PARAMETER__REMOVE_PHONE_NUMBER = "delete_phone_number";

    public static final String REQUEST_PARAMETER__PHONE_NUMBER_TYPE_ID = "phone_number_type_id";
    public static final String REQUEST_PARAMETER__EDITED_PHONE_NUMBER = "edited_phone_number";
    public static final String REQUEST_PARAMETER__SELECTED_PHONE_NUMBER = "selected_phone_number";

    private static final LocalizedMessage ERROR__PASSWORDS_DID_NOT_MATCH = new LocalizedMessage("error/passwords_did_not_match");
    private static final LocalizedMessage ERROR__PASSWORD_LENGTH = new LocalizedMessage("error/password_length");

    private UserDomainObject editedUser;
    private PhoneNumber currentPhoneNumber = new PhoneNumber("", PhoneNumberType.OTHER);
    private LocalizedMessage errorMessage;
    private static final int MAXIMUM_PASSWORD_LENGTH = 15;
    private static final int MINIMUM_PASSWORD_LENGTH = 4;

    public UserEditorPage(UserDomainObject user, DispatchCommand okDispatchCommand,
                          DispatchCommand cancelDispatchCommand) {
        super(okDispatchCommand, cancelDispatchCommand);
        this.editedUser = user;
    }

    protected void updateFromRequest(HttpServletRequest request) {
        updateUserFromRequest(editedUser, request);
    }

    private void updateUserFromRequest(UserDomainObject user, HttpServletRequest request) {
        errorMessage = null;
        updateUserPasswordFromRequest(user, request);
        user.setLoginName(request.getParameter(REQUEST_PARAMETER__LOGIN_NAME));
        user.setFirstName(request.getParameter(REQUEST_PARAMETER__FIRST_NAME));
        user.setLastName(request.getParameter(REQUEST_PARAMETER__LAST_NAME));
        user.setTitle(request.getParameter(REQUEST_PARAMETER__TITLE));
        user.setCompany(request.getParameter(REQUEST_PARAMETER__COMPANY));
        user.setAddress(request.getParameter(REQUEST_PARAMETER__ADDRESS));
        user.setCity(request.getParameter(REQUEST_PARAMETER__CITY));
        user.setZip(request.getParameter(REQUEST_PARAMETER__ZIP));
        user.setCountry(request.getParameter(REQUEST_PARAMETER__COUNTRY));
        user.setDistrict(request.getParameter(REQUEST_PARAMETER__DISTRICT));
        user.setEmailAddress(request.getParameter(REQUEST_PARAMETER__EMAIL));
        user.setLanguageIso639_2(request.getParameter(REQUEST_PARAMETER__LANGUAGE));
        user.setActive(null != request.getParameter(REQUEST_PARAMETER__ACTIVE));

        updateUserRolesFromRequest(user, request);
        updateUserAdminRolesFromRequest(user, request);
    }

    private void updateUserAdminRolesFromRequest(UserDomainObject user, HttpServletRequest request) {
        if ( Utility.getLoggedOnUser(request).isSuperAdmin() && user.isUserAdmin() ) {
            user.setUserAdminRolesIds(getRoleIdsFromRequestParameterValues(request, REQUEST_PARAMETER__USER_ADMIN_ROLE_IDS));
            user.removeUserAdminRoleId(RoleId.SUPERADMIN) ;
            user.removeUserAdminRoleId(RoleId.USERADMIN) ;
        }
    }

    private RoleId[] getRoleIdsFromRequestParameterValues(HttpServletRequest request, String requestParameter) {
        Set roleIds = new HashSet();
        String[] roleIdStrings = request.getParameterValues(requestParameter);
        if ( null != roleIdStrings ) {
            for ( int i = 0; i < roleIdStrings.length; i++ ) {
                RoleId roleId = new RoleId(Integer.parseInt(roleIdStrings[i]));
                roleIds.add(roleId);
            }
        }
        return (RoleId[]) roleIds.toArray(new RoleId[roleIds.size()]);
    }

    private ImcmsServices getImcmsServices() {
        return Imcms.getServices() ;
    }

    private void updateUserRolesFromRequest(UserDomainObject user, HttpServletRequest request) {
        if ( Utility.getLoggedOnUser(request).canEditRolesFor(user) ) {
            user.setRoleIds(getRoleIdsFromRequestParameterValues(request, REQUEST_PARAMETER__ROLE_IDS));
        }
    }

    private void updateUserPasswordFromRequest(UserDomainObject user, HttpServletRequest request) {
        String password1 = getPassword1FromRequest(request);
        if ( StringUtils.isNotBlank(password1) ) {
            if ( !passwordPassesLengthRequirements(password1) ) {
                errorMessage = ERROR__PASSWORD_LENGTH;
            } else if ( !passwordsMatch(request) ) {
                errorMessage = ERROR__PASSWORDS_DID_NOT_MATCH;
            } else {
                user.setPassword(password1);
            }
        }
    }

    private boolean passwordPassesLengthRequirements(String password1) {
        return password1.length() >= MINIMUM_PASSWORD_LENGTH
               && password1.length() <= MAXIMUM_PASSWORD_LENGTH;
    }

    public String getPath(HttpServletRequest request) {
        UserDomainObject loggedOnUser = Utility.getLoggedOnUser(request);
        return "/imcms/" + loggedOnUser.getLanguageIso639_2() + "/jsp/usereditor.jsp";
    }

    protected void dispatchOther(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, ServletException {
        PhoneNumber editedPhoneNumber = getEditedPhoneNumberFromRequest(request);
        PhoneNumber selectedPhoneNumber = getSelectedPhoneNumberFromRequest(request);

        if ( null != request.getParameter(REQUEST_PARAMETER__ADD_PHONE_NUMBER) && null != editedPhoneNumber ) {
            if ( !editedPhoneNumber.equals(currentPhoneNumber) ) {
                editedUser.removePhoneNumber(currentPhoneNumber);
            }
            editedUser.removePhoneNumber(editedPhoneNumber);
            editedUser.addPhoneNumber(editedPhoneNumber);
            currentPhoneNumber = new PhoneNumber("", PhoneNumberType.OTHER);
        } else if ( null != request.getParameter(REQUEST_PARAMETER__REMOVE_PHONE_NUMBER)
                    && null != selectedPhoneNumber ) {
            editedUser.removePhoneNumber(selectedPhoneNumber);
            currentPhoneNumber = selectedPhoneNumber;
        } else if ( null != request.getParameter(REQUEST_PARAMETER__EDIT_PHONE_NUMBER)
                    && null != selectedPhoneNumber ) {
            currentPhoneNumber = selectedPhoneNumber;
        }
        forward(request, response);
    }

    protected void dispatchOk(HttpServletRequest request,
                              HttpServletResponse response) throws IOException, ServletException {
        if ( null == errorMessage ) {
            if ( StringUtils.isBlank(editedUser.getPassword()) ) {
                errorMessage = ERROR__PASSWORD_LENGTH;
            } else {
                super.dispatchOk(request, response);
                return;
            }
        }
        forward(request, response);
    }

    private boolean passwordsMatch(HttpServletRequest request) {
        String password1 = getPassword1FromRequest(request);
        String password2 = request.getParameter(REQUEST_PARAMETER__PASSWORD2);
        return password1.equals(password2);
    }

    private String getPassword1FromRequest(HttpServletRequest request) {
        return request.getParameter(REQUEST_PARAMETER__PASSWORD1);
    }

    private PhoneNumber getEditedPhoneNumberFromRequest(HttpServletRequest request) {
        PhoneNumber editedPhoneNumber = null;
        String editedPhoneNumberString = request.getParameter(REQUEST_PARAMETER__EDITED_PHONE_NUMBER);
        if ( StringUtils.isNotBlank(editedPhoneNumberString) ) {
            int editedPhoneNumberTypeId = Integer.parseInt(request.getParameter(REQUEST_PARAMETER__PHONE_NUMBER_TYPE_ID));
            PhoneNumberType editedPhoneNumberType = PhoneNumberType.getPhoneNumberTypeById(editedPhoneNumberTypeId);
            editedPhoneNumber = new PhoneNumber(editedPhoneNumberString, editedPhoneNumberType);
        }
        return editedPhoneNumber;
    }

    private PhoneNumber getSelectedPhoneNumberFromRequest(HttpServletRequest request) {
        PhoneNumber selectedPhoneNumber = null;
        String selectedPhoneNumberString = request.getParameter(REQUEST_PARAMETER__SELECTED_PHONE_NUMBER);
        if ( StringUtils.isNotBlank(selectedPhoneNumberString) ) {
            Matcher matcher = Pattern.compile("(\\d+) (.*)").matcher(selectedPhoneNumberString);
            if ( matcher.matches() ) {
                int selectedPhoneNumberTypeId = Integer.parseInt(matcher.group(1));
                PhoneNumberType selectedPhoneNumberType = PhoneNumberType.getPhoneNumberTypeById(selectedPhoneNumberTypeId);
                selectedPhoneNumber = new PhoneNumber(matcher.group(2), selectedPhoneNumberType);
            }
        }
        return selectedPhoneNumber;
    }

    public UserDomainObject getEditedUser() {
        return editedUser;
    }

    public String createLanguagesHtmlOptionList(UserDomainObject user,
                                                UserDomainObject userToChange) {
        return getImcmsServices().getLanguageMapper().createLanguagesOptionList(user, userToChange.getLanguageIso639_2());
    }

    public String createPhoneTypesHtmlOptionList(final UserDomainObject loggedOnUser, PhoneNumberType selectedType) {
        return Html.createOptionList(Arrays.asList(PhoneNumberType.getAllPhoneNumberTypes()), selectedType, new ToStringPairTransformer() {
            public String[] transformToStringPair(Object object) {
                PhoneNumberType phoneType = (PhoneNumberType) object;
                return new String[] { "" + phoneType.getId(), phoneType.getName().toLocalizedString(loggedOnUser) };
            }
        });
    }

    public PhoneNumber getCurrentPhoneNumber() {
        return currentPhoneNumber;
    }

    public String getUserPhoneNumbersHtmlOptionList(final HttpServletRequest request) {
        Set phoneNumbers = editedUser.getPhoneNumbers();
        return Html.createOptionList(phoneNumbers, currentPhoneNumber, new ToStringPairTransformer() {
            protected String[] transformToStringPair(Object object) {
                PhoneNumber phoneNumber = (PhoneNumber) object;
                return new String[] { phoneNumber.getType().getId() + " " + phoneNumber.getNumber(), "("
                                                                                                     + phoneNumber.getType().getName().toLocalizedString(request)
                                                                                                     + ") "
                                                                                                     + phoneNumber.getNumber() };
            }
        });
    }

    public LocalizedMessage getErrorMessage() {
        return errorMessage;
    }

    public String createRolesHtmlOptionList(HttpServletRequest request) {
        UserDomainObject loggedOnUser = Utility.getLoggedOnUser(request) ;
        RoleDomainObject[] roles = loggedOnUser.isUserAdminOnly() ? getRoles(loggedOnUser.getUserAdminRoleIds()) : getAllRolesExceptUsersRole();
        RoleDomainObject[] usersRoles = getRoles(editedUser.getRoleIds());
        return createRolesHtmlOptionList(roles, usersRoles);
    }

    private RoleDomainObject[] getRoles(RoleId[] roleIds) {
        RoleDomainObject[] roles = new RoleDomainObject[roleIds.length];
        for ( int i = 0; i < roleIds.length; i++ ) {
            roles[i] = Imcms.getServices().getImcmsAuthenticatorAndUserAndRoleMapper().getRole(roleIds[i]) ;
        }
        return roles ;
    }

    private String createRolesHtmlOptionList(RoleDomainObject[] allRoles, RoleDomainObject[] usersRoles) {
        return Html.createOptionList(Arrays.asList(allRoles), new ArraySet(usersRoles), new RoleToStringPairTransformer());
    }

    public String createUserAdminRolesHtmlOptionList() {
        RoleDomainObject[] allRoles = getAllRolesExceptUsersRole();
        Set allRolesSet = new HashSet(Arrays.asList(allRoles)) ;
        allRolesSet.remove(RoleId.SUPERADMIN) ;
        allRolesSet.remove(RoleId.USERADMIN) ;
        allRoles = (RoleDomainObject[]) allRolesSet.toArray(new RoleDomainObject[allRolesSet.size()]);
        RoleDomainObject[] usersUserAdminRoles = getRoles(editedUser.getUserAdminRoleIds());

        return createRolesHtmlOptionList(allRoles, usersUserAdminRoles);
    }

    private RoleDomainObject[] getAllRolesExceptUsersRole() {
        RoleDomainObject[] allRoles = getImcmsServices().getImcmsAuthenticatorAndUserAndRoleMapper().getAllRolesExceptUsersRole();
        Arrays.sort(allRoles);
        return allRoles;
    }

    public void setErrorMessage(LocalizedMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setOkCommand(DispatchCommand okCommand) {
        this.okCommand = okCommand;
    }

    private static class RoleToStringPairTransformer extends ToStringPairTransformer {
        protected String[] transformToStringPair(Object object) {
            RoleDomainObject role = (RoleDomainObject) object;
            return new String[] { "" + role.getId(), role.getName() };
        }
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDomainObject loggedOnUser = Utility.getLoggedOnUser(request);
        if (!loggedOnUser.canEdit(editedUser)) {
            throw new ShouldHaveCheckedPermissionsEarlierException(new NoPermissionException("User "+loggedOnUser+" does not have the permission to edit "+editedUser));
        }

        super.forward(request, response);
    }
}