package imcode.server.document.textdocument;

import imcode.server.Imcms;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentMapper;
import imcode.server.document.DocumentVisitor;
import imcode.server.document.TemplateDomainObject;

import java.io.Serializable;
import java.util.*;

public class TextDocumentDomainObject extends DocumentDomainObject {

    private LazilyLoadedTextDocumentAttributes lazilyLoadedTextDocumentAttributes = null;

    public Object clone() throws CloneNotSupportedException {
        TextDocumentDomainObject clone = (TextDocumentDomainObject)super.clone();
        if ( null != lazilyLoadedTextDocumentAttributes ) {
            clone.lazilyLoadedTextDocumentAttributes = (LazilyLoadedTextDocumentAttributes)lazilyLoadedTextDocumentAttributes.clone();
        }
        return clone;
    }

    public Set getChildDocuments() {
        Set childDocuments = new HashSet() ;
        Map menus = getMenus() ;
        for ( Iterator iterator = menus.values().iterator(); iterator.hasNext(); ) {
            MenuDomainObject menu = (MenuDomainObject)iterator.next();
            MenuItemDomainObject[] menuItems = menu.getMenuItems() ;
            for ( int i = 0; i < menuItems.length; i++ ) {
                MenuItemDomainObject menuItem = menuItems[i];
                childDocuments.add( menuItem.getDocument() ) ;
            }
        }
        return childDocuments ;
    }

    public ImageDomainObject getImage( int imageIndex ) {
        ImageDomainObject image = (ImageDomainObject)getLazilyLoadedTextDocumentAttributes().images.get( new Integer( imageIndex )) ;
        if (null == image) {
            image = new ImageDomainObject() ;
        }
        return image ;
    }

    public Integer getIncludedDocumentId( int includeIndex ) {
        return (Integer)getLazilyLoadedTextDocumentAttributes().includes.get( new Integer( includeIndex ) );
    }

    public MenuDomainObject getMenu( int menuIndex ) {
        MenuDomainObject menu = (MenuDomainObject)getLazilyLoadedTextDocumentAttributes().menus.get( new Integer( menuIndex ) );
        if (null == menu) {
            menu = new MenuDomainObject() ;
            setMenu( menuIndex, menu );
        }
        return menu;
    }

    public TextDomainObject getText( int textFieldIndex ) {
        return (TextDomainObject)getLazilyLoadedTextDocumentAttributes().texts.get( new Integer( textFieldIndex ) );
    }

    public void initDocument( DocumentMapper documentMapper ) {
        // lazily loaded
    }

    public void accept( DocumentVisitor documentVisitor ) {
        documentVisitor.visitTextDocument(this) ;
    }

    public void removeAllImages() {
        getLazilyLoadedTextDocumentAttributes().images.clear();
    }

    public void removeImage(int imageIndex ) {
        getLazilyLoadedTextDocumentAttributes().images.remove(new Integer( imageIndex ));
    }

    public void removeAllIncludes() {
        getLazilyLoadedTextDocumentAttributes().includes.clear();
    }

    public void removeAllMenus() {
        getLazilyLoadedTextDocumentAttributes().menus.clear();
    }

    public void removeAllTexts() {
        getLazilyLoadedTextDocumentAttributes().texts.clear();
    }

    public void setInclude( int includeIndex, int includedDocumentId ) {
        getLazilyLoadedTextDocumentAttributes().includes.put( new Integer( includeIndex ), new Integer( includedDocumentId ) );
    }

    public void setMenu( int menuIndex, MenuDomainObject menu ) {
        getLazilyLoadedTextDocumentAttributes().menus.put( new Integer( menuIndex ), menu );
    }

    public void setText( int textIndex, TextDomainObject text ) {
        getLazilyLoadedTextDocumentAttributes().texts.put( new Integer( textIndex ), text );
    }

    public int getDefaultTemplateIdForRestrictedPermissionSetOne() {
        return getLazilyLoadedTextDocumentAttributes().defaultTemplateIdForRestrictedPermissionSetOne;
    }

    public int getDefaultTemplateIdForRestrictedPermissionSetTwo() {
        return getLazilyLoadedTextDocumentAttributes().defaultTemplateIdForRestrictedPermissionSetTwo;
    }

    public int getDocumentTypeId() {
        return DOCTYPE_TEXT;
    }

    /**
     * @return Map<Integer, {@link ImageDomainObject} *
     */
    public Map getImages() {
        return Collections.unmodifiableMap( getLazilyLoadedTextDocumentAttributes().images );
    }

    public Map getIncludes() {
        return Collections.unmodifiableMap( getLazilyLoadedTextDocumentAttributes().includes );
    }

    public Map getMenus() {
        return Collections.unmodifiableMap( getLazilyLoadedTextDocumentAttributes().menus );
    }

    public TemplateDomainObject getTemplate() {
        return getLazilyLoadedTextDocumentAttributes().template;
    }

    public int getTemplateGroupId() {
        return getLazilyLoadedTextDocumentAttributes().templateGroupId;
    }

    /**
     * @return Map<Integer, {@link TextDomainObject}>
     */
    public Map getTexts() {
        return Collections.unmodifiableMap( getLazilyLoadedTextDocumentAttributes().texts );
    }

    public void setDefaultTemplateIdForRestrictedPermissionSetOne( int defaultTemplateIdForRestrictedPermissionSetOne ) {
        this.getLazilyLoadedTextDocumentAttributes().defaultTemplateIdForRestrictedPermissionSetOne = defaultTemplateIdForRestrictedPermissionSetOne;
    }

    public void setDefaultTemplateIdForRestrictedPermissionSetTwo( int defaultTemplateIdForRestrictedPermissionSetTwo ) {
        this.getLazilyLoadedTextDocumentAttributes().defaultTemplateIdForRestrictedPermissionSetTwo = defaultTemplateIdForRestrictedPermissionSetTwo;
    }

    public void setImages( Map images ) {
        this.getLazilyLoadedTextDocumentAttributes().images = new TreeMap(images);
    }

    public void setTemplate( TemplateDomainObject v ) {
        this.getLazilyLoadedTextDocumentAttributes().template = v;
    }

    public void setTemplateGroupId( int v ) {
        this.getLazilyLoadedTextDocumentAttributes().templateGroupId = v;
    }

    private synchronized LazilyLoadedTextDocumentAttributes getLazilyLoadedTextDocumentAttributes() {
        if ( null == lazilyLoadedTextDocumentAttributes ) {
            lazilyLoadedTextDocumentAttributes = new LazilyLoadedTextDocumentAttributes();
            DocumentMapper documentMapper = Imcms.getServices().getDocumentMapper();
            documentMapper.initLazilyLoadedTextDocumentAttributes( this );
        }
        return lazilyLoadedTextDocumentAttributes;
    }

    protected void loadAllLazilyLoadedDocumentTypeSpecificAttributes() {
        getLazilyLoadedTextDocumentAttributes();
    }

    public void setImage( int imageIndex, ImageDomainObject image ) {
        getLazilyLoadedTextDocumentAttributes().images.put( new Integer( imageIndex ), image ) ;
    }

    private class LazilyLoadedTextDocumentAttributes implements Serializable, Cloneable {

        private TemplateDomainObject template;
        private int templateGroupId;
        private int defaultTemplateIdForRestrictedPermissionSetOne;
        private int defaultTemplateIdForRestrictedPermissionSetTwo;
        private TreeMap texts = new TreeMap();
        private TreeMap images = new TreeMap();
        private TreeMap includes = new TreeMap();
        private TreeMap menus = new TreeMap();

        public Object clone() throws CloneNotSupportedException {
            LazilyLoadedTextDocumentAttributes clone = (LazilyLoadedTextDocumentAttributes)super.clone();
            clone.texts = (TreeMap)texts.clone();
            clone.images = (TreeMap)images.clone();
            clone.includes = (TreeMap)includes.clone();
            clone.menus = (TreeMap)menus.clone();
            return clone;
        }
    }

}