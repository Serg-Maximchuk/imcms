package com.imcode.imcms.api;

import imcode.server.document.textdocument.DocI18nItem;
import imcode.server.document.textdocument.DocVersionItem;

import javax.persistence.*;

/**
 * Labels is a set of texts and images associated with a document.
 * Labels is both i18n and version item.
 */
@Entity
@Table(name="imcms_doc_labels")
public class DocumentLabels implements Cloneable, DocVersionItem, DocI18nItem {

	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
	private Integer id;

	@Column(name="doc_id")
    private Integer docId;

    @Column(name="doc_version_no")
    private Integer docVersionNo;

	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="language_id", referencedColumnName="id")
    private I18nLanguage language;

    /**
     * Doc's headline label. Mainly used as HTML page title.
     */
	@Column(name="headline")
    private String headline;

    /**
     * Menu item label.
     * Used when a doc is included in other doc's menu (as a menu item).
     */
	@Column(name="menu_text")
    private String menuText;

    /**
     * Menu item image.
     */
	@Column(name="menu_image_url")
    private String menuImageURL;

    @Override
    public DocumentLabels clone() {
        try {
            return (DocumentLabels)super.clone();   
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getMenuText() {
        return menuText;
    }

    public void setMenuText(String menuText) {
        this.menuText = menuText;
    }

    public String getMenuImageURL() {
        return menuImageURL;
    }

    public void setMenuImageURL(String menuImageURL) {
        this.menuImageURL = menuImageURL;
    }

    public Integer getDocVersionNo() {
        return docVersionNo;
    }

    public void setDocVersionNo(Integer docVersionNo) {
        this.docVersionNo = docVersionNo;
    }

    public I18nLanguage getLanguage() {
        return language;
    }

    public void setLanguage(I18nLanguage language) {
        this.language = language;
    }
}
