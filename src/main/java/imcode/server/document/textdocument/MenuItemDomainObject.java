package imcode.server.document.textdocument;

import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentReference;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Menu item descriptor.
 *
 * @see imcode.server.document.textdocument.MenuDomainObject
 */

public class MenuItemDomainObject implements Cloneable, Serializable {

    private Integer sortKey;

    private TreeSortKeyDomainObject treeSortKey = new TreeSortKeyDomainObject("");

    private DocumentReference documentReference;

    public MenuItemDomainObject() {
    }

    public MenuItemDomainObject(DocumentReference documentReference,
                                Integer sortKey, TreeSortKeyDomainObject treeSortKey) {
        this.documentReference = documentReference;
        this.sortKey = sortKey;
        this.treeSortKey = treeSortKey;
    }

    public MenuItemDomainObject(DocumentReference documentReference) {
        this(documentReference, null, new TreeSortKeyDomainObject(""));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MenuItemDomainObject)) {
            return false;
        }

        final MenuItemDomainObject otherMenuItem = (MenuItemDomainObject) o;

        return new EqualsBuilder()
                .append(sortKey, otherMenuItem.sortKey)
                .append(documentReference, otherMenuItem.documentReference)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 7).append(documentReference).toHashCode();
    }

    public MenuItemDomainObject clone() throws CloneNotSupportedException {
        MenuItemDomainObject clone = (MenuItemDomainObject) super.clone();
        if (treeSortKey != null) {
            clone.treeSortKey = new TreeSortKeyDomainObject(treeSortKey.getTreeSortKey());
        }

        if (documentReference != null) {
            clone.documentReference = documentReference.clone();
        }

        return clone;
    }

    public DocumentDomainObject getDocument() {
        if (documentReference == null)
            throw new IllegalStateException("Document reference is not initialized.");

        return documentReference.getDocument();
    }

    public TreeSortKeyDomainObject getTreeSortKey() {
        return treeSortKey;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public int getDocumentId() {
        return documentReference.getDocumentId();
    }

    public Integer getSortKey() {
        return sortKey;
    }

    public String getTreeSortIndex() {
        return treeSortKey.getTreeSortKey();
    }

    public void setSortKey(Integer sortKey) {
        this.sortKey = sortKey;
    }

    public void setTreeSortKey(TreeSortKeyDomainObject treeSortKey) {
        this.treeSortKey = treeSortKey;
    }

    public void setTreeSortIndex(String treeSortIndex) {
        setTreeSortKey(new TreeSortKeyDomainObject(treeSortIndex));
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }
}