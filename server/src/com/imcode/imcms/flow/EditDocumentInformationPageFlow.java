package com.imcode.imcms.flow;

import com.imcode.imcms.servlet.WebComponent;
import com.imcode.imcms.servlet.admin.DocumentComposer;
import com.imcode.imcms.servlet.admin.ImageBrowser;
import com.imcode.imcms.servlet.admin.UserFinder;
import imcode.server.ApplicationServer;
import imcode.server.IMCServiceInterface;
import imcode.server.document.CategoryDomainObject;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentMapper;
import imcode.server.document.SectionDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.DateConstants;
import imcode.util.HttpSessionUtils;
import imcode.util.Utility;
import org.apache.commons.lang.ObjectUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditDocumentInformationPageFlow extends EditDocumentPageFlow {

    private final static String URL_I15D_PAGE__DOCINFO = "/jsp/docadmin/document_information.jsp";
    public static final String REQUEST_PARAMETER__HEADLINE = "headline";
    public static final String REQUEST_PARAMETER__MENUTEXT = "menutext";
    public static final String REQUEST_PARAMETER__COPY_HEADLINE_AND_TEXT_TO_TEXTFIELDS = "copy_headline_and_text_to_textfields";
    public static final String REQUEST_PARAMETER__IMAGE = "image";
    public static final String REQUEST_PARAMETER__PUBLICATION_START_DATE = "activated_date";
    public static final String REQUEST_PARAMETER__PUBLICATION_START_TIME = "activated_time";
    public static final String REQUEST_PARAMETER__ARCHIVED_DATE = "archived_date";
    public static final String REQUEST_PARAMETER__ARCHIVED_TIME = "archived_time";
    public static final String REQUEST_PARAMETER__SECTIONS = "change_section";
    public static final String REQUEST_PARAMETER__PUBLICATION_END_DATE = "publication_end_date";
    public static final String REQUEST_PARAMETER__PUBLICATION_END_TIME = "publication_end_time";
    public static final String REQUEST_PARAMETER__LANGUAGE = "lang_prefix";
    public static final String REQUEST_PARAMETER__CATEGORIES = "categories";
    public static final String REQUEST_PARAMETER__VISIBLE_IN_MENU_FOR_UNAUTHORIZED_USERS = "show_meta";
    public static final String REQUEST_PARAMETER__LINKABLE_BY_OTHER_USERS = "shared";
    public static final String REQUEST_PARAMETER__KEYWORDS = "classification";
    public static final String REQUEST_PARAMETER__SEARCH_DISABLED = "disable_search";
    public static final String REQUEST_PARAMETER__TARGET = "target";
    public static final String REQUEST_PARAMETER__CREATED_DATE = "date_created";
    public static final String REQUEST_PARAMETER__CREATED_TIME = "created_time";
    public static final String REQUEST_PARAMETER__MODIFIED_DATE = "date_modified";
    public static final String REQUEST_PARAMETER__MODIFIED_TIME = "modified_time";
    public static final String REQUEST_PARAMETER__PUBLISHER_ID = "publisher_id";
    public static final String REQUEST_PARAMETER__STATUS = "status";
    public static final String REQUEST_PARAMETER__GO_TO_PUBLISHER_BROWSER = "browseForPublisher";
    public static final String REQUEST_PARAMETER__GO_TO_CREATOR_BROWSER = "browseForCreator";
    public static final String REQUEST_PARAMETER__GO_TO_IMAGE_BROWSER = "browseForMenuImage";
    public static final String PAGE__DOCUMENT_INFORMATION = "document_information";

    public EditDocumentInformationPageFlow( DocumentDomainObject document ) {
        super( document );
    }

    protected void dispatchFromEditPage( HttpServletRequest request, HttpServletResponse response, String page ) throws IOException, ServletException {
        if ( PAGE__DOCUMENT_INFORMATION.equals( page ) ) {
            dispatchFromDocumentInformation( request, response );
        }
    }

    private void dispatchFromDocumentInformation( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        setDocumentAttributesFromRequestParameters( document, request );
        if ( null != request.getParameter( REQUEST_PARAMETER__GO_TO_PUBLISHER_BROWSER ) ) {
            dispatchToPublisherUserBrowser( request, response );
        } else if ( null != request.getParameter( REQUEST_PARAMETER__GO_TO_CREATOR_BROWSER ) ) {
            dispatchToCreatorUserBrowser( request, response );
        } else if ( null != request.getParameter( REQUEST_PARAMETER__GO_TO_IMAGE_BROWSER ) ) {
            dispatchToImageBrowser( request, response );
        }
    }

    private void dispatchToImageBrowser( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        ImageBrowser imageBrowser = new ImageBrowser();
        final String flowSessionAttributeName = HttpSessionUtils.getSessionAttributeNameFromRequest( request, DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW );
        imageBrowser.setCancelCommand( new WebComponent.CancelCommand() {
            public void cancel( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
                request.setAttribute( DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW, flowSessionAttributeName );
                dispatchToFirstPage( request, response );
            }
        } );
        imageBrowser.setSelectImageUrlCommand( new ImageBrowser.SelectImageCommand() {
            public void selectImageUrl( String imageUrl, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
                document.setMenuImage( "../images/" + imageUrl );
                request.setAttribute( DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW, flowSessionAttributeName );
                dispatchToFirstPage( request, response );
            }
        } );
        imageBrowser.forward( request, response );
    }

    private void dispatchToPublisherUserBrowser( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        final String flowSessionAttributeName = HttpSessionUtils.getSessionAttributeNameFromRequest( request, DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW );
        dispatchToUserBrowser( request, response, true, new UserFinder.SelectUserCommand() {
            public void selectUser( UserDomainObject selectedUser, HttpServletRequest request,
                                    HttpServletResponse response ) throws ServletException, IOException {
                document.setPublisher( selectedUser );
                request.setAttribute( DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW, flowSessionAttributeName );
                dispatchToFirstPage( request, response );
            }
        } );
    }

    private void dispatchToCreatorUserBrowser( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        final String flowSessionAttributeName = HttpSessionUtils.getSessionAttributeNameFromRequest( request, DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW );
        dispatchToUserBrowser( request, response, false, new UserFinder.SelectUserCommand() {
            public void selectUser( UserDomainObject selectedUser, HttpServletRequest request,
                                    HttpServletResponse response ) throws ServletException, IOException {
                document.setCreator( selectedUser );
                request.setAttribute( DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW, flowSessionAttributeName );
                dispatchToFirstPage( request, response );
            }
        } );
    }

    private void dispatchToUserBrowser( HttpServletRequest request, HttpServletResponse response,
                                        boolean nullSelectable, UserFinder.SelectUserCommand selectUserCommand ) throws IOException, ServletException {
        UserFinder userFinder = UserFinder.getInstance( request );
        userFinder.setSelectButton( UserFinder.SELECT_BUTTON__SELECT_USER );
        userFinder.setUsersAddable( false );
        userFinder.setNullSelectable( nullSelectable );
        userFinder.setSelectUserCommand( selectUserCommand );
        final String flowSessionAttributeName = HttpSessionUtils.getSessionAttributeNameFromRequest( request, DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW );
        userFinder.setCancelCommand( new WebComponent.CancelCommand() {
            public void cancel( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
                request.setAttribute( DocumentComposer.REQUEST_ATTRIBUTE_OR_PARAMETER__FLOW, flowSessionAttributeName );
                dispatchToFirstPage( request, response );
            }
        } );
        userFinder.forward( request, response );
    }

    protected void dispatchToFirstPage( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        UserDomainObject user = Utility.getLoggedOnUser( request );
        request.getRequestDispatcher( URL_I15D_PAGE__PREFIX + user.getLanguageIso639_2() + URL_I15D_PAGE__DOCINFO ).forward( request, response );
    }

    protected void dispatchOkFromEditPage( HttpServletRequest request, HttpServletResponse response ) throws IOException {
        dispatchOkFromDocumentInformation( request );
    }

    private void dispatchOkFromDocumentInformation( HttpServletRequest request ) {
        setDocumentAttributesFromRequestParameters( document, request );
    }

    public static void setDocumentAttributesFromRequestParameters( DocumentDomainObject document,
                                                                   HttpServletRequest request ) {

        final IMCServiceInterface service = ApplicationServer.getIMCServiceInterface();
        final DocumentMapper documentMapper = service.getDocumentMapper();

        String headline = request.getParameter( REQUEST_PARAMETER__HEADLINE );
        document.setHeadline( headline );

        String menuText = request.getParameter( REQUEST_PARAMETER__MENUTEXT );
        document.setMenuText( menuText );

        String imageUrl = request.getParameter( REQUEST_PARAMETER__IMAGE );
        document.setMenuImage( imageUrl );

        int status = Integer.parseInt( request.getParameter( REQUEST_PARAMETER__STATUS ) );
        document.setStatus( status );

        SimpleDateFormat dateFormat = new SimpleDateFormat( DateConstants.DATE_FORMAT_STRING );
        SimpleDateFormat timeFormat = new SimpleDateFormat( DateConstants.TIME_NO_SECONDS_FORMAT_STRING );

        Date publicationStartDatetime = parseDatetimeParameters( request, REQUEST_PARAMETER__PUBLICATION_START_DATE, REQUEST_PARAMETER__PUBLICATION_START_TIME, dateFormat,
                                                                 timeFormat );
        Date archivedDatetime = parseDatetimeParameters( request, REQUEST_PARAMETER__ARCHIVED_DATE, REQUEST_PARAMETER__ARCHIVED_TIME, dateFormat,
                                                         timeFormat );
        Date publicationEndDatetime = parseDatetimeParameters( request, REQUEST_PARAMETER__PUBLICATION_END_DATE, REQUEST_PARAMETER__PUBLICATION_END_TIME, dateFormat,
                                                               timeFormat );

        document.setPublicationStartDatetime( publicationStartDatetime );
        document.setArchivedDatetime( archivedDatetime );
        document.setPublicationEndDatetime( publicationEndDatetime );

        document.removeAllSections();
        String[] sectionIds = request.getParameterValues( REQUEST_PARAMETER__SECTIONS );
        for ( int i = 0; null != sectionIds && i < sectionIds.length; i++ ) {
            int sectionId = Integer.parseInt( sectionIds[i] );
            SectionDomainObject section = documentMapper.getSectionById( sectionId );
            document.addSection( section );
        }

        String languageIso639_2 = request.getParameter( REQUEST_PARAMETER__LANGUAGE );
        document.setLanguageIso639_2( languageIso639_2 );

        document.removeAllCategories();
        String[] categoryIds = request.getParameterValues( REQUEST_PARAMETER__CATEGORIES );
        for ( int i = 0; null != categoryIds && i < categoryIds.length; i++ ) {
            try {
                int categoryId = Integer.parseInt( categoryIds[i] );
                CategoryDomainObject category = documentMapper.getCategoryById( categoryId );
                document.addCategory( category );
            } catch ( NumberFormatException ignored ) {
                // OK, empty category id
            }
        }

        boolean visibleInMenuForUnauthorizedUsers = "1".equals( request.getParameter( REQUEST_PARAMETER__VISIBLE_IN_MENU_FOR_UNAUTHORIZED_USERS ) );
        document.setVisibleInMenusForUnauthorizedUsers( visibleInMenuForUnauthorizedUsers );

        boolean linkableByOtherUsers = "1".equals( request.getParameter( REQUEST_PARAMETER__LINKABLE_BY_OTHER_USERS ) );
        document.setLinkableByOtherUsers( linkableByOtherUsers );

        String keywordsString = request.getParameter( REQUEST_PARAMETER__KEYWORDS );
        String[] keywords = parseKeywords( keywordsString );
        document.setKeywords( keywords );

        boolean searchDisabled = "1".equals( request.getParameter( REQUEST_PARAMETER__SEARCH_DISABLED ) );
        document.setSearchDisabled( searchDisabled );

        String target = getTargetFromRequest( request );
        document.setTarget( target );

        Date createdDatetime = (Date)ObjectUtils.defaultIfNull( parseDatetimeParameters( request, REQUEST_PARAMETER__CREATED_DATE, REQUEST_PARAMETER__CREATED_TIME, dateFormat, timeFormat ), new Date() );

        Date modifiedDatetime = (Date)ObjectUtils.defaultIfNull( parseDatetimeParameters( request, REQUEST_PARAMETER__MODIFIED_DATE, REQUEST_PARAMETER__MODIFIED_TIME, dateFormat, timeFormat ), createdDatetime );

        document.setCreatedDatetime( createdDatetime );
        document.setModifiedDatetime( modifiedDatetime );

    }

    private static String[] parseKeywords( String keywordsString ) {
        List keywords = new ArrayList();
        StringBuffer currentKeyword = new StringBuffer();
        boolean insideString = false;
        for ( int i = 0; i < keywordsString.length(); ++i ) {
            char c = keywordsString.charAt( i );
            if ( '"' == c ) {
                insideString = !insideString;
            } else if ( Character.isLetterOrDigit( c ) || insideString ) {
                currentKeyword.append( c );
            } else if ( 0 < currentKeyword.length() ) {
                keywords.add( currentKeyword.toString() );
                currentKeyword.setLength( 0 );
            }
        }
        if ( 0 < currentKeyword.length() ) {
            keywords.add( currentKeyword.toString() );
        }
        return (String[])keywords.toArray( new String[keywords.size()] );
    }

    public static String getTargetFromRequest( HttpServletRequest request ) {
        String[] possibleTargets = request.getParameterValues( REQUEST_PARAMETER__TARGET );
        String target = null;
        for ( int i = 0; i < possibleTargets.length; i++ ) {
            target = possibleTargets[i];
            boolean targetIsPredefinedTarget
                    = "_self".equalsIgnoreCase( target )
                      || "_blank".equalsIgnoreCase( target )
                      || "_parent".equalsIgnoreCase( target )
                      || "_top".equalsIgnoreCase( target );
            if ( targetIsPredefinedTarget ) {
                break;
            }
        }
        return target;
    }

    private static Date parseDatetimeParameters( HttpServletRequest req, final String dateParameterName,
                                                 final String timeParameterName, DateFormat dateformat,
                                                 DateFormat timeformat ) {
        String dateStr = req.getParameter( dateParameterName );
        String timeStr = req.getParameter( timeParameterName );

        Date date;
        try {
            date = dateformat.parse( dateStr );
        } catch ( ParseException pe ) {
            return null;
        } catch ( NullPointerException npe ) {
            return null;
        }

        Date time;
        try {
            timeformat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
            time = timeformat.parse( timeStr );
        } catch ( ParseException pe ) {
            return date;
        } catch ( NullPointerException npe ) {
            return date;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        calendar.add( Calendar.MILLISECOND, (int)time.getTime() );
        return calendar.getTime();
    }
}
