package com.imcode.imcms.mapping.container;

import imcode.server.document.textdocument.MenuDomainObject;

import java.util.Objects;

/**
 * Uniquely identifies menu in a text document.
 */
public class TextDocMenuContainer {

    public static TextDocMenuContainer of(VersionRef versionRef, int menuNo, MenuDomainObject menu) {
        return new TextDocMenuContainer(versionRef, menuNo, menu);
    }

    private final VersionRef versionRef;
    private final int menuNo;
    private final MenuDomainObject menu;

    public TextDocMenuContainer(VersionRef versionRef, int menuNo, MenuDomainObject menu) {
        this.versionRef = Objects.requireNonNull(versionRef);
        this.menu = Objects.requireNonNull(menu);
        this.menuNo = menuNo;
    }

    public int getMenuNo() {
        return menuNo;
    }

    public MenuDomainObject getMenu() {
        return menu;
    }

    public VersionRef getVersionRef() {
        return versionRef;
    }

    public int getDocId() {
        return versionRef.getDocId();
    }

    public int getVersionNo() {
        return versionRef.getNo();
    }
}

