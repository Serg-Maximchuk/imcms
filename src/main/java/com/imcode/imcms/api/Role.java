package com.imcode.imcms.api;

import imcode.server.user.RoleDomainObject;
import imcode.server.user.RoleId;

/**
 * @since 2.0
 */
public class Role implements Comparable<Role> {

    public static final int SUPERADMIN_ID = RoleId.SUPERADMIN_ID;
    public static final int USERADMIN_ID = RoleId.USERADMIN_ID;
    public static final int USERS_ID = RoleId.USERS_ID;

    private final RoleDomainObject internalRole;

    Role(RoleDomainObject role) {
        this.internalRole = role;
    }

    RoleDomainObject getInternal() {
        return internalRole;
    }

    public int getId() {
        return internalRole.getId().intValue();
    }

    public String getName() {
        return internalRole.getName();
    }

    public void setName(String name) {
        internalRole.setName(name);
    }

    public boolean equals(Object o) {
        return internalRole.equals(((Role) o).internalRole);
    }

    public int hashCode() {
        return internalRole.hashCode();
    }

    public String toString() {
        return getName();
    }

    public void setPasswordMailPermission(boolean passwordMailPermission) {
        if (passwordMailPermission) {
            internalRole.addPermission(RoleDomainObject.PASSWORD_MAIL_PERMISSION);
        } else {
            internalRole.removePermission(RoleDomainObject.PASSWORD_MAIL_PERMISSION);
        }
    }

    public boolean hasPasswordMailPermission() {
        return internalRole.hasPermission(RoleDomainObject.PASSWORD_MAIL_PERMISSION);
    }

    public void setUseImagesInArchivePermission(boolean useImages) {
        if (useImages) {
            internalRole.addPermission(RoleDomainObject.USE_IMAGES_IN_ARCHIVE_PERMISSION);
        } else {
            internalRole.removePermission(RoleDomainObject.USE_IMAGES_IN_ARCHIVE_PERMISSION);
        }
    }

    public boolean hasUseImagesInArchivePermission() {
        return internalRole.hasPermission(RoleDomainObject.USE_IMAGES_IN_ARCHIVE_PERMISSION);
    }

    public void setChangeImagesInArchivePermission(boolean changeImages) {
        if (changeImages) {
            internalRole.addPermission(RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);
        } else {
            internalRole.removePermission(RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);
        }
    }

    public boolean hasChangeImagesInArchivePermission() {
        return internalRole.hasPermission(RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);
    }

    public int compareTo(Role role) {
        return internalRole.compareTo(role.internalRole);
    }
}
