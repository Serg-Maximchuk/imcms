package imcode.server.document;

import imcode.server.IMCConstants;

/** Stores all info about a text-document. **/

public class DocumentDomainObject implements IMCConstants {

    // If an field is added, make sure to update DocumentMapper
    int metaId;
    int documentType;
    boolean archived;
    java.util.Date createdDatetime;
    java.util.Date modifiedDatetime;
    java.util.Date activatedDatetime;
    java.util.Date archivedDatetime;
    String headline;
    String text;
    String image;
    String target;
    String section;
    // todo: classification/Search words is missing

    /* Filedocs only */
    String filename;

    /* Textdocs only */
    TemplateDomainObject template;
    int templateGroupId;
    int menuSortOrder;
    // If an field is added, make sure to update DocumentMapper

    public final static int DOCTYPE_TEXT       = 2 ;
    public final static int DOCTYPE_URL        = 5 ;
    public final static int DOCTYPE_BROWSER    = 6 ;
    public final static int DOCTYPE_HTML       = 7 ;
    public final static int DOCTYPE_FILE       = 8 ;
    public final static int DOCTYPE_DIAGRAM    = 101 ;
    public final static int DOCTYPE_CONFERENCE = 102 ;
    public final static int DOCTYPE_CHAT       = 103 ;
    public final static int DOCTYPE_BILLBOARD  = 104 ;
    public final static int DOCTYPE_POSTCARD   = 105 ;
    public final static int DOCTYPE_FORTUNES   = 106 ;
    public final static int DOCTYPE_CALENDER   = 107 ;

    /**
     * Get the value of metaId.
     * @return value of metaId.
     */
    public int getMetaId() {
        return metaId;
    }

    /**
     * Set the value of metaId.
     * @param v  Value to assign to metaId.
     */
    public void setMetaId( int v ) {
        this.metaId = v;
    }

    /**
     * Get the value of documentType.
     * @return value of documentType.
     */
    public int getDocumentType() {
        return documentType;
    }

    /**
     * Set the value of documentType.
     * @param v  Value to assign to documentType.
     */
    public void setDocumentType( int v ) {
        this.documentType = v;
    }

    /**
     * Check whether this document is active.
     * A document is active if it isn't archived, and if activatedDatetime is in the past.
     * @return value of archived.
     */
    public boolean isActive() {
        java.util.Date now = new java.util.Date();
        return (activatedDatetime == null || activatedDatetime.before( now )) && !isArchivedAtTime( now );
    }

    /**
     * Check whether this document is archived.
     * A document is archived if either 'archived' is true, or archiveDatetime is in the past.
     * @return value of archived.
     */
    public boolean isArchived() {
        java.util.Date now = new java.util.Date();
        return isArchivedAtTime( now );
    }

    private boolean isArchivedAtTime( java.util.Date time ) {
        return archived || (archivedDatetime != null && archivedDatetime.before( time ));
    }


    /**
     * Set the value of archived.
     * @param v  Value to assign to archived.
     */
    public void setArchived( boolean v ) {
        this.archived = v;
    }

    /**
     * Get the value of createdDatetime.
     * @return value of createdDatetime.
     */
    public java.util.Date getCreatedDateTime() {
        return createdDatetime;
    }

    /**
     * Set the value of createdDatetime.
     * @param v  Value to assign to createdDatetime.
     */
    public void setCreatedDatetime( java.util.Date v ) {
        this.createdDatetime = v;
    }

    /**
     * Get the value of modifiedDatetime.
     * @return value of modifiedDatetime.
     */
    public java.util.Date getModifiedDateTime() {
        return modifiedDatetime;
    }

    /**
     * Set the value of modifiedDatetime.
     * @param v  Value to assign to modifiedDatetime.
     */
    public void setModifiedDatetime( java.util.Date v ) {
        this.modifiedDatetime = v;
    }

    /**
     * Get the value of archivedDatetime.
     * @return value of archivedDatetime.
     */
    public java.util.Date getArchivedDateTime() {
        return archivedDatetime;
    }

    /**
     * Set the value of archivedDatetime.
     * @param v  Value to assign to archivedDatetime.
     */
    public void setArchivedDatetime( java.util.Date v ) {
        this.archivedDatetime = v;
    }

    /**
     * Get the value of activatedDatetime.
     * @return value of activatedDatetime.
     */
    public java.util.Date getActivatedDateTime() {
        return activatedDatetime;
    }

    /**
     * Set the value of archivedDatetime.
     * @param v  Value to assign to archivedDatetime.
     */
    public void setActivatedDatetime( java.util.Date v ) {
        this.activatedDatetime = v;
    }

    /**
     * Get the value of headline.
     * @return value of headline.
     */
    public String getHeadline() {
        return headline;
    }

    /**
     * Set the value of headline.
     * @param v  Value to assign to headline.
     */
    public void setHeadline( String v ) {
        this.headline = v;
    }

    /**
     * Get the value of text.
     * @return value of text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set the value of text.
     * @param v  Value to assign to text.
     */
    public void setText( String v ) {
        this.text = v;
    }

    /**
     * Get the value of image.
     * @return value of image.
     */
    public String getImage() {
        return image;
    }

    /**
     * Set the value of image.
     * @param v  Value to assign to image.
     */
    public void setImage( String v ) {
        this.image = v;
    }

    /**
     * Get the value of target.
     * @return value of target.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Set the value of target.
     * @param v  Value to assign to target.
     */
    public void setTarget( String v ) {
        this.target = v;
    }

    /**
     * Get the value of filename.
     * @return value of filename.
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Set the value of filename.
     * @param v  Value to assign to filename.
     */
    public void setFilename( String v ) {
        this.filename = v;
    }

    /**
     * Get the value of section.
     * @return value of section.
     */
    public String getSection() {
        return section;
    }

    /**
     * Set the value of section.
     * @param v  Value to assign to section.
     */
    public void setSection( String v ) {
        this.section = v;
    }

    /**
     * Get the value of template.
     * @return value of template.
     */
    public TemplateDomainObject getTemplate() {
        return template;
    }

    /**
     * Set the value of template.
     * @param v  Value to assign to template.
     */
    public void setTemplate( TemplateDomainObject v ) {
        this.template = v;
    }

    /**
     * Get the value of menuSortOrder.
     * @return value of menuSortOrder.
     */
    public int getMenuSortOrder() {
        return menuSortOrder;
    }

    /**
     * Set the value of menuSortOrder.
     * @param v  Value to assign to menuSortOrder.
     */
    public void setMenuSortOrder( int v ) {
        this.menuSortOrder = v;
    }

    /**
     * Get the value of templateGroupId.
     * @return value of templateGroupId.
     */
    public int getTemplateGroupId() {
        return templateGroupId;
    }

    /**
     * Set the value of templateGroupId.
     * @param v  Value to assign to templateGroupId.
     */
    public void setTemplateGroupId( int v ) {
        this.templateGroupId = v;
    }

}
