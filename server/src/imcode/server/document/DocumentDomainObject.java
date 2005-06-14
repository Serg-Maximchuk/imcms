package imcode.server.document;

import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.user.RoleDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.LocalizedMessage;
import org.apache.commons.collections.map.TypedMap;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

public abstract class DocumentDomainObject implements Cloneable, Serializable {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_PUBLICATION_DISAPPROVED = 1;
    public static final int STATUS_PUBLICATION_APPROVED = 2;

    public static final int ID_NEW = 0;

    protected Attributes attributes;
    private static Logger log = Logger.getLogger( DocumentDomainObject.class );

    protected DocumentDomainObject() {
        attributes = new Attributes();
        attributes.permissionSetForRestrictedOne = new TextDocumentPermissionSetDomainObject( DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_1 );
        attributes.permissionSetForRestrictedTwo = new TextDocumentPermissionSetDomainObject( DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_2 );
        attributes.permissionSetForRestrictedOneForNewDocuments = new TextDocumentPermissionSetDomainObject( DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_1 );
        attributes.permissionSetForRestrictedTwoForNewDocuments = new TextDocumentPermissionSetDomainObject( DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_2 );
    }

    public Object clone() throws CloneNotSupportedException {
        DocumentDomainObject clone = (DocumentDomainObject)super.clone();
        if ( null != attributes ) {
            clone.attributes = (Attributes)attributes.clone();
        }
        return clone;
    }

    public static DocumentDomainObject fromDocumentTypeId( int documentTypeId ) {
        DocumentDomainObject document;

        switch ( documentTypeId ) {
            case DocumentTypeDomainObject.TEXT_ID :
                document = new TextDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.URL_ID:
                document = new UrlDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.BROWSER_ID:
                document = new BrowserDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.FILE_ID:
                document = new FileDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.HTML_ID:
                document = new HtmlDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.CHAT_ID:
                document = new ChatDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.CONFERENCE_ID:
                document = new ConferenceDocumentDomainObject();
                break;
            case DocumentTypeDomainObject.BILLBOARD_ID:
                document = new BillboardDocumentDomainObject();
                break;
            default:
                String errorMessage = "Unknown document-type-id: " + documentTypeId;
                log.error( errorMessage );
                throw new IllegalArgumentException( errorMessage );
        }

        return document;
    }

    public Date getArchivedDatetime() {
        return attributes.archivedDatetime;
    }

    public void setArchivedDatetime( Date v ) {
        attributes.archivedDatetime = v;
    }

    public CategoryDomainObject[] getCategories() {
        return (CategoryDomainObject[])attributes.categories.toArray( new CategoryDomainObject[attributes.categories.size()] );
    }

    public Date getCreatedDatetime() {
        return attributes.createdDatetime;
    }

    public void setCreatedDatetime( Date v ) {
        attributes.createdDatetime = v;
    }

    public UserDomainObject getCreator() {
        return attributes.creator;
    }

    public void setCreator( UserDomainObject creator ) {
        attributes.creator = creator;
    }

    public void setAttributes( Attributes attributes ) {
        this.attributes = attributes;
    }

    public String getHeadline() {
        return attributes.headline;
    }

    public void setHeadline( String v ) {
        attributes.headline = v;
    }

    public int getId() {
        return attributes.id;
    }

    public void setId( int v ) {
        attributes.id = v;
    }

    public String getMenuImage() {
        return attributes.image;
    }

    public void setMenuImage( String v ) {
        attributes.image = v;
    }

    public Set getKeywords() {
        return Collections.unmodifiableSet(attributes.keywords) ;
    }

    public void setKeywords( Set keywords ) {
        attributes.keywords = new HashSet( keywords );
    }

    public String getLanguageIso639_2() {
        return attributes.languageIso639_2;
    }

    public void setLanguageIso639_2( String languageIso639_2 ) {
        attributes.languageIso639_2 = languageIso639_2;
    }

    public String getMenuText() {
        return attributes.menuText;
    }

    public void setMenuText( String v ) {
        attributes.menuText = v;
    }

    public Date getModifiedDatetime() {
        return attributes.modifiedDatetime;
    }

    public void setModifiedDatetime( Date v ) {
        attributes.modifiedDatetime = v;
    }

    void setLastModifiedDatetime( Date modifiedDatetime ) {
        this.attributes.lastModifiedDatetime = modifiedDatetime;
    }

    Date getLastModifiedDatetime() {
        return attributes.lastModifiedDatetime;
    }

    public Date getPublicationEndDatetime() {
        return attributes.publicationEndDatetime;
    }

    public void setPublicationEndDatetime( Date datetime ) {
        attributes.publicationEndDatetime = datetime;
    }

    public Date getPublicationStartDatetime() {
        return attributes.publicationStartDatetime;
    }

    public void setPublicationStartDatetime( Date v ) {
        attributes.publicationStartDatetime = v;
    }

    public UserDomainObject getPublisher() {
        return attributes.publisher;
    }

    public void setPublisher( UserDomainObject user ) {
        attributes.publisher = user;
    }

    public Map getRolesMappedToPermissionSetIds() {
        return Collections.unmodifiableMap( attributes.rolesMappedToDocumentPermissionSetIds );
    }

    public void setRolesMappedToPermissionSetIds( Map rolesMappedToPermissionSetIds ) {
        attributes.rolesMappedToDocumentPermissionSetIds = TypedMap.decorate( new HashMap(), RoleDomainObject.class, Integer.class );
        attributes.rolesMappedToDocumentPermissionSetIds.putAll( rolesMappedToPermissionSetIds );
    }

    public SectionDomainObject[] getSections() {
        return (SectionDomainObject[])attributes.sections.toArray( new SectionDomainObject[attributes.sections.size()] );
    }

    public void setSections( SectionDomainObject[] sections ) {
        attributes.sections = new HashSet( Arrays.asList( sections ) );
    }

    public int getStatus() {
        return attributes.status;
    }

    public void setStatus( int status ) {
        switch ( status ) {
            case STATUS_NEW:
            case STATUS_PUBLICATION_APPROVED:
            case STATUS_PUBLICATION_DISAPPROVED:
                attributes.status = status;
                break;
            default:
                throw new IllegalArgumentException( "Bad status." );
        }
    }

    public String getTarget() {
        return attributes.target;
    }

    public void setTarget( String v ) {
        attributes.target = v;
    }

    public boolean isArchived() {
        return isArchivedAtTime( new Date() );
    }

    public boolean isLinkableByOtherUsers() {
        return attributes.linkableByOtherUsers;
    }

    public void setLinkableByOtherUsers( boolean linkableByOtherUsers ) {
        attributes.linkableByOtherUsers = linkableByOtherUsers;
    }

    public boolean isRestrictedOneMorePrivilegedThanRestrictedTwo() {
        return attributes.restrictedOneMorePrivilegedThanRestrictedTwo;
    }

    public void setRestrictedOneMorePrivilegedThanRestrictedTwo( boolean b ) {
        attributes.restrictedOneMorePrivilegedThanRestrictedTwo = b;
    }

    public boolean isPublished() {
        return isPublishedAtTime( new Date() );
    }

    public boolean isActive() {
        return isPublished() && !isArchived();
    }

    public boolean isNoLongerPublished() {
        return isNoLongerPublishedAtTime( new Date() );
    }

    private boolean isNoLongerPublishedAtTime( Date date ) {
        Date publicationEndDatetime = attributes.publicationEndDatetime;
        return publicationEndDatetime != null && publicationEndDatetime.before( date );
    }

    public boolean isSearchDisabled() {
        return attributes.searchDisabled;
    }

    public void setSearchDisabled( boolean searchDisabled ) {
        attributes.searchDisabled = searchDisabled;
    }

    public boolean isVisibleInMenusForUnauthorizedUsers() {
        return attributes.visibleInMenusForUnauthorizedUsers;
    }

    public void setVisibleInMenusForUnauthorizedUsers( boolean visibleInMenusForUnauthorizedUsers ) {
        attributes.visibleInMenusForUnauthorizedUsers = visibleInMenusForUnauthorizedUsers;
    }

    public void addCategory( CategoryDomainObject category ) {
        attributes.categories.add( category );
    }

    public void addSection( SectionDomainObject section ) {
        attributes.sections.add( section );
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof DocumentDomainObject ) ) {
            return false;
        }

        final DocumentDomainObject document = (DocumentDomainObject)o;

        if ( attributes.id != document.attributes.id ) {
            return false;
        }

        return true;
    }

    public CategoryDomainObject[] getCategoriesOfType( CategoryTypeDomainObject type ) {
        CategoryDomainObject[] categories = (CategoryDomainObject[])attributes.categories.toArray( new CategoryDomainObject[attributes.categories.size()] );
        List categoriesOfType = new ArrayList();
        for ( int i = 0; i < categories.length; i++ ) {
            CategoryDomainObject category = categories[i];
            if ( type.equals( category.getType() ) ) {
                categoriesOfType.add( category );
            }
        }
        final CategoryDomainObject[] arrayOfCategoriesOfType = new CategoryDomainObject[categoriesOfType.size()];
        return (CategoryDomainObject[])categoriesOfType.toArray( arrayOfCategoriesOfType );
    }

    public abstract DocumentTypeDomainObject getDocumentType();

    public final int getDocumentTypeId() {
        return getDocumentType().getId();
    }

    public final LocalizedMessage getDocumentTypeName() {
        return getDocumentType().getName();
    }

    public int hashCode() {
        return attributes.id;
    }

    private boolean isArchivedAtTime( Date time ) {
        Attributes documentProperties = this.attributes;
        return documentProperties.archivedDatetime != null && documentProperties.archivedDatetime.before( time );
    }

    public void removeAllCategories() {
        attributes.categories.clear();
    }

    public void removeAllSections() {
        attributes.sections.clear();
    }

    public void removeCategory( CategoryDomainObject category ) {
        attributes.categories.remove( category );
    }

    public void setPermissionSetIdForRole( RoleDomainObject role, int permissionSetId ) {
        attributes.rolesMappedToDocumentPermissionSetIds.put( role, new Integer( permissionSetId ) );
    }

    public int getPermissionSetIdForRole( RoleDomainObject role ) {
        int permissionSetId = DocumentPermissionSetDomainObject.TYPE_ID__NONE;
        Integer permissionSetIdInteger = (Integer)attributes.rolesMappedToDocumentPermissionSetIds.get( role );
        if ( null != permissionSetIdInteger ) {
            permissionSetId = permissionSetIdInteger.intValue();
        }
        return permissionSetId;
    }

    private boolean isPublishedAtTime( Date date ) {
        Attributes documentProperties = this.attributes;
        boolean publicationStartDatetimeIsNotNullAndInThePast = documentProperties.publicationStartDatetime != null
                                                                && documentProperties.publicationStartDatetime.before( date );
        boolean publicationEndDatetimeIsNullOrInTheFuture = documentProperties.publicationEndDatetime == null
                                                            || documentProperties.publicationEndDatetime.after( date );
        boolean statusIsApproved = documentProperties.status == STATUS_PUBLICATION_APPROVED;
        boolean isPublished = statusIsApproved && publicationStartDatetimeIsNotNullAndInThePast
                              && publicationEndDatetimeIsNullOrInTheFuture;
        return isPublished;
    }

    public void setPermissionSetForRestrictedOne( DocumentPermissionSetDomainObject permissionSetForRestrictedOne ) {
        this.attributes.permissionSetForRestrictedOne = permissionSetForRestrictedOne;
    }

    public void setPermissionSetForRestrictedTwo( DocumentPermissionSetDomainObject permissionSetForRestrictedTwo ) {
        this.attributes.permissionSetForRestrictedTwo = permissionSetForRestrictedTwo;
    }

    public void setPermissionSetForRestrictedOneForNewDocuments(
            DocumentPermissionSetDomainObject permissionSetForRestrictedOneForNewDocuments ) {
        this.attributes.permissionSetForRestrictedOneForNewDocuments = permissionSetForRestrictedOneForNewDocuments;
    }

    public void setPermissionSetForRestrictedTwoForNewDocuments(
            DocumentPermissionSetDomainObject permissionSetForRestrictedTwoForNewDocuments ) {
        this.attributes.permissionSetForRestrictedTwoForNewDocuments = permissionSetForRestrictedTwoForNewDocuments;
    }

    public DocumentPermissionSetDomainObject getPermissionSetForRestrictedOne() {
        return this.attributes.permissionSetForRestrictedOne;
    }

    public DocumentPermissionSetDomainObject getPermissionSetForRestrictedOneForNewDocuments() {
        return this.attributes.permissionSetForRestrictedOneForNewDocuments;
    }

    public DocumentPermissionSetDomainObject getPermissionSetForRestrictedTwo() {
        return this.attributes.permissionSetForRestrictedTwo;
    }

    public DocumentPermissionSetDomainObject getPermissionSetForRestrictedTwoForNewDocuments() {
        return this.attributes.permissionSetForRestrictedTwoForNewDocuments;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public abstract void accept( DocumentVisitor documentVisitor );

    public String getUrl( HttpServletRequest request ) {
        return request.getContextPath() + "/servlet/GetDoc?meta_id=" + getId();
    }

    public LifeCyclePhase getLifeCyclePhase() {
        DocumentDomainObject.LifeCyclePhase lifeCyclePhase = null ;
        if ( DocumentDomainObject.STATUS_NEW == getStatus() ) {
            lifeCyclePhase = LifeCyclePhase.NEW;
        } else if ( DocumentDomainObject.STATUS_PUBLICATION_DISAPPROVED == getStatus() ) {
            lifeCyclePhase = LifeCyclePhase.DISAPPROVED;
        } else if ( isActive() ) {
            lifeCyclePhase = LifeCyclePhase.PUBLISHED;
        } else if ( isNoLongerPublished() ) {
            lifeCyclePhase = LifeCyclePhase.UNPUBLISHED;
        } else if ( isArchived() ) {
            lifeCyclePhase = LifeCyclePhase.ARCHIVED;
        } else {
            lifeCyclePhase = LifeCyclePhase.APPROVED;
        }
        return lifeCyclePhase ;
    }

    public void removeNonInheritedCategories() {
        for ( Iterator iterator = attributes.categories.iterator(); iterator.hasNext(); ) {
            CategoryDomainObject category = (CategoryDomainObject)iterator.next();
            if (!category.getType().isInherited()) {
                iterator.remove();
            }
        }
    }

    public static class Attributes implements Cloneable, Serializable {

        private Date archivedDatetime;
        private Date createdDatetime;
        private UserDomainObject creator;
        private String headline;
        private String image;
        private String languageIso639_2;
        private boolean linkableByOtherUsers;
        private String menuText;
        private int id;
        private Date modifiedDatetime;
        private Date lastModifiedDatetime;
        private boolean restrictedOneMorePrivilegedThanRestrictedTwo;
        private Date publicationStartDatetime;
        private Date publicationEndDatetime;
        private UserDomainObject publisher;
        private boolean searchDisabled;
        private int status;
        private String target;
        private boolean visibleInMenusForUnauthorizedUsers;

        private Set categories = new HashSet();
        private Set keywords = new HashSet();
        private Set sections = new HashSet();
        public DocumentPermissionSetDomainObject permissionSetForRestrictedOne ;
        public DocumentPermissionSetDomainObject permissionSetForRestrictedTwo ;
        public DocumentPermissionSetDomainObject permissionSetForRestrictedOneForNewDocuments ;
        public DocumentPermissionSetDomainObject permissionSetForRestrictedTwoForNewDocuments ;

        private Map rolesMappedToDocumentPermissionSetIds = new HashMap();

        public Object clone() throws CloneNotSupportedException {
            Attributes clone = (Attributes)super.clone();
            clone.keywords = new HashSet( keywords );
            clone.sections = new HashSet( sections );
            clone.categories = new HashSet( categories );
            clone.rolesMappedToDocumentPermissionSetIds = new HashMap( rolesMappedToDocumentPermissionSetIds );
            return clone;
        }

    }

    public static class LifeCyclePhase {

        public static final LifeCyclePhase NEW = new LifeCyclePhase("new");
        public static final LifeCyclePhase DISAPPROVED = new LifeCyclePhase("disapproved");
        public static final LifeCyclePhase PUBLISHED = new LifeCyclePhase("published");
        public static final LifeCyclePhase UNPUBLISHED = new LifeCyclePhase("unpublished");
        public static final LifeCyclePhase ARCHIVED = new LifeCyclePhase("archived");
        public static final LifeCyclePhase APPROVED = new LifeCyclePhase("approved");

        private final String name;

        private LifeCyclePhase( String name ) {
            this.name = name ;
        }

        public String toString() {
            return name ;
        }
    }
}
