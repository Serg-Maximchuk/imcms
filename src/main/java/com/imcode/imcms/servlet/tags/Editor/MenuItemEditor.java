package com.imcode.imcms.servlet.tags.Editor;

/**
 * Created by Shadowgun on 12.01.2015.
 */
public class MenuItemEditor extends SupportEditor {
    private int id;
    private String name;
    private int position;
    private String treePosition;

    @Override
    public String getWrapperPre() {
        super.builder
                .addClass("menu-item")
                .addParam("menu-item-id", id)
                .addParam("menu-item-position", position)
                .addParam("menu-item-tree-position", treePosition)
                .addParam("menu-item-name", name);

        return super.getWrapperPre();
    }

    public MenuItemEditor setId(int id) {
        this.id = id;
        return this;
    }

    public MenuItemEditor setPosition(int position) {
        this.position = position;
        return this;
    }

    public MenuItemEditor setTreePosition(String treePosition) {
        this.treePosition = treePosition;
        return this;
    }

    public MenuItemEditor setMenuName(String name) {
        this.name = name;
        return this;
    }
}
