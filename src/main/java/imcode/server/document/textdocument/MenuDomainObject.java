package imcode.server.document.textdocument;

import imcode.server.user.UserDomainObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Menu is a one-level navigation control between documents.
 * A menu can contain any number of items - links to other documents.
 */
@Entity(name = "Menu")
@Table(name = "imcms_text_doc_menus")
public class MenuDomainObject implements Cloneable, Serializable {

    public final static int MENU_SORT_ORDER__BY_HEADLINE = 1;
    public final static int MENU_SORT_ORDER__BY_MANUAL_ORDER_REVERSED = 2;
    public final static int MENU_SORT_ORDER__BY_MODIFIED_DATETIME_REVERSED = 3;
    public final static int MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER = 4;
    public final static int MENU_SORT_ORDER__BY_PUBLISHED_DATETIME_REVERSED = 5;
    public final static int MENU_SORT_ORDER__DEFAULT = MENU_SORT_ORDER__BY_HEADLINE;

    public final static int DEFAULT_SORT_KEY = 500;

    private static final int DEFAULT_SORT_KEY_INCREMENT = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "no")
    private Integer no;

    private DocRef docRef;


    /**
     * Map of included meta_id to included DocumentDomainObject.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "imcms_text_doc_menu_items",
            joinColumns = @JoinColumn(name = "menu_id"))
    @MapKeyColumn(name = "to_doc_id")
    private Map<Integer, MenuItemDomainObject> menuItems = new HashMap<Integer, MenuItemDomainObject>();

    public MenuDomainObject() {
        this(null, MENU_SORT_ORDER__DEFAULT);
    }

    public MenuDomainObject clone() {
        try {
            MenuDomainObject clone = (MenuDomainObject) super.clone();
            clone.menuItems = new HashMap<Integer, MenuItemDomainObject>();
            for (Map.Entry<Integer, MenuItemDomainObject> entry : menuItems.entrySet()) {
                clone.menuItems.put(entry.getKey(), entry.getValue().clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new UnhandledException(e);
        }
    }

    public MenuDomainObject(Long id, int sortOrder) {
        this.id = id;
        this.sortOrder = sortOrder;
        menuItems = new HashMap<Integer, MenuItemDomainObject>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public MenuItemDomainObject[] getMenuItemsUserCanSee(UserDomainObject user) {
        List menuItemsUserCanSee = getMenuItemsVisibleToUser(user);
        return (MenuItemDomainObject[]) menuItemsUserCanSee.toArray(new MenuItemDomainObject[menuItemsUserCanSee.size()]);
    }

    List getMenuItemsVisibleToUser(UserDomainObject user) {
        MenuItemDomainObject[] menuItemsArray = getMenuItems();
        List menuItemsUserCanSee = new ArrayList(this.menuItems.size());
        for (int i = 0; i < menuItemsArray.length; i++) {
            MenuItemDomainObject menuItem = menuItemsArray[i];
            if (user.canSeeDocumentWhenEditingMenus(menuItem.getDocument())) {
                menuItemsUserCanSee.add(menuItem);
            }
        }
        Collections.sort(menuItemsUserCanSee, getMenuItemComparatorForSortOrder(sortOrder));
        return menuItemsUserCanSee;
    }

    /**
     * @return Menu items pointing to active documents.
     */
    public MenuItemDomainObject[] getPublishedMenuItemsUserCanSee(UserDomainObject user) {
        List menuItems = getMenuItemsVisibleToUser(user);
        CollectionUtils.filter(menuItems, new Predicate() {
            public boolean evaluate(Object object) {
                return ((MenuItemDomainObject) object).getDocument().isActive();
            }
        });
        return (MenuItemDomainObject[]) menuItems.toArray(new MenuItemDomainObject[menuItems.size()]);
    }

    public MenuItemDomainObject[] getMenuItems() {
        Set menuItemsUnsorted = getMenuItemsUnsorted();
        MenuItemDomainObject[] menuItemsArray = (MenuItemDomainObject[]) menuItemsUnsorted.toArray(new MenuItemDomainObject[menuItemsUnsorted.size()]);
        Arrays.sort(menuItemsArray, getMenuItemComparatorForSortOrder(sortOrder));
        return menuItemsArray;
    }

    public Set getMenuItemsUnsorted() {
        HashSet set = new HashSet();
        for (Iterator iterator = menuItems.values().iterator(); iterator.hasNext(); ) {
            MenuItemDomainObject menuItem = (MenuItemDomainObject) iterator.next();
            if (null != menuItem.getDocument()) {
                set.add(menuItem);
            }
        }
        return set;
    }


    /**
     * Adds menu item to this menu only if it contains a document.
     *
     * @param menuItem
     */
    public void addMenuItem(MenuItemDomainObject menuItem) {
        if (null == menuItem.getSortKey()) {
            generateSortKey(menuItem);
        }
        if (null != menuItem.getDocument()) {
            addMenuItemUnchecked(menuItem);
        }
    }


    /**
     * Adds menu item to this menu without checking if it references a document.
     *
     * @param menuItem
     */
    public void addMenuItemUnchecked(MenuItemDomainObject menuItem) {
        if (null == menuItem.getSortKey()) {
            generateSortKey(menuItem);
        }

        menuItems.put(menuItem.getDocumentId(), menuItem);
    }

    private void generateSortKey(MenuItemDomainObject menuItem) {
        Integer maxSortKey = getMaxSortKey();
        Integer sortKey;
        if (null != maxSortKey) {
            sortKey = maxSortKey.intValue() + DEFAULT_SORT_KEY_INCREMENT;
        } else {
            sortKey = DEFAULT_SORT_KEY;
        }
        menuItem.setSortKey(sortKey);
    }

    private Integer getMaxSortKey() {
        Collection<Integer> menuItemSortKeys = CollectionUtils.collect(menuItems.values(), new Transformer() {
            public Integer transform(Object o) {
                return ((MenuItemDomainObject) o).getSortKey();
            }
        });

        return menuItemSortKeys.isEmpty() ? null : Collections.max(menuItemSortKeys);
    }

    private Comparator getMenuItemComparatorForSortOrder(int sortOrder) {

        Comparator comparator = MenuItemComparator.HEADLINE.chain(MenuItemComparator.ID);
        if (sortOrder == MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER) {
            comparator = MenuItemComparator.TREE_SORT_KEY.chain(comparator);
        } else if (sortOrder == MENU_SORT_ORDER__BY_MANUAL_ORDER_REVERSED) {
            comparator = MenuItemComparator.SORT_KEY.reversed().chain(comparator);
        } else if (sortOrder == MENU_SORT_ORDER__BY_MODIFIED_DATETIME_REVERSED) {
            comparator = MenuItemComparator.MODIFIED_DATETIME.reversed().chain(comparator);
        } else if (sortOrder == MENU_SORT_ORDER__BY_PUBLISHED_DATETIME_REVERSED) {
            comparator = MenuItemComparator.PUBLISHED_DATETIME.reversed().chain(comparator);
        }
        return comparator;
    }

    public void setSortOrder(int sortOrder) {
        switch (sortOrder) {
            case MENU_SORT_ORDER__BY_MANUAL_ORDER_REVERSED:
            case MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER:
            case MENU_SORT_ORDER__BY_MODIFIED_DATETIME_REVERSED:
            case MENU_SORT_ORDER__BY_PUBLISHED_DATETIME_REVERSED:
            case MENU_SORT_ORDER__BY_HEADLINE:
                this.sortOrder = sortOrder;
                break;
            default:
                throw new IllegalArgumentException("Bad sort order. Use one of the constants.");
        }
    }

    public void removeMenuItemByDocumentId(int childId) {
        menuItems.remove(new Integer(childId));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MenuDomainObject)) {
            return false;
        }
        MenuDomainObject otherMenu = (MenuDomainObject) obj;
        return new EqualsBuilder().append(sortOrder, otherMenu.sortOrder)
                .append(menuItems, otherMenu.menuItems).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(sortOrder).append(menuItems).toHashCode();
    }


    public Map<Integer, MenuItemDomainObject> getItemsMap() {
        return menuItems;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    /**
     * Use {@link MenuDomainObject#getNo()} instead.
     */
    @Deprecated
    public Integer getIndex() {
        return getNo();
    }

    /**
     * Use {@link MenuDomainObject#setNo(Integer)} instead.
     */
    @Deprecated
    public void setIndex(Integer index) {
        setNo(index);
    }

    public DocRef getDocRef() {
        return docRef;
    }

    public void setDocRef(DocRef docRef) {
        this.docRef = docRef;
    }
}