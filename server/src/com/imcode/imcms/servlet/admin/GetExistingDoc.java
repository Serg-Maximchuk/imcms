package com.imcode.imcms.servlet.admin;

import imcode.server.ApplicationServer;
import imcode.server.IMCConstants;
import imcode.server.IMCServiceInterface;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentMapper;
import imcode.server.document.TextDocumentPermissionSetDomainObject;
import imcode.server.document.index.DocumentIndex;
import imcode.server.document.index.DefaultQueryParser;
import imcode.server.document.textdocument.MenuItemDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.DateConstants;
import imcode.util.Html;
import imcode.util.Parser;
import imcode.util.Utility;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.DateField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetExistingDoc extends HttpServlet {

    private final static Logger log = Logger.getLogger( GetExistingDoc.class.getName() );
    private static final String ONE_SEARCH_HIT = "existing_doc_hit.html";
    private static final String SEARCH_RESULTS = "existing_doc_res.html";
    private static final String ADMIN_TEMPLATE_EXISTING_DOC = "existing_doc.html";

    public void doPost( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface();

        Utility.setDefaultHtmlContentType( res );
        Writer out = res.getWriter();

        DocumentMapper documentMapper = imcref.getDocumentMapper();
        TextDocumentDomainObject parentDocument = (TextDocumentDomainObject)documentMapper.getDocument( Integer.parseInt( req.getParameter( "meta_id_value" ) ) );

        // Lets get the doc_menu_number
        int menuIndex = Integer.parseInt( req.getParameter( "doc_menu_no" ) );

        UserDomainObject user = Utility.getLoggedOnUser( req );
        if ( req.getParameter( "cancel" ) != null || req.getParameter( "cancel.x" ) != null ) {
            res.sendRedirect( "AdminDoc?meta_id=" + parentDocument.getId() + "&flags="
                              + IMCConstants.DISPATCH_FLAG__EDIT_MENU + "&editmenu=" + menuIndex );
            return;
        } else if ( req.getParameter( "search" ) != null || req.getParameter( "search.x" ) != null ) {
            // SEARCH
            // Lets do a search among existing documents.
            // Lets collect the parameters and build a sql searchstring
            final DocumentIndex index = documentMapper.getDocumentIndex();
            BooleanQuery query = new BooleanQuery();
            String searchString = req.getParameter( "searchstring" );
            String searchPrep = req.getParameter( "search_prep" );
            try {
                if ( "or".equalsIgnoreCase( searchPrep ) ) {
                    addStringToQuery( index, searchString, query );
                } else {
                    String[] searchStrings = searchString.split( "\\s+" );
                    for ( int i = 0; i < searchStrings.length; i++ ) {
                        String string = searchStrings[i];
                        addStringToQuery( index, string, query );
                    }
                }
            } catch ( org.apache.lucene.queryParser.ParseException pe ) {
                log.debug( "Bad query: " + searchString, pe );
            }

            String[] docTypes = req.getParameterValues( "doc_type" );
            addDocTypesToQuery( docTypes, query );

            DateFormat dateFormat = new SimpleDateFormat( DateConstants.DATE_FORMAT_STRING );
            Date startDate = null;
            Date endDate = null;
            try {
                String startDateString = req.getParameter( "start_date" );
                startDate = dateFormat.parse( startDateString );
            } catch ( ParseException ignored ) {
            }
            try {
                String endDateString = req.getParameter( "end_date" );
                endDate = dateFormat.parse( endDateString );
            } catch ( ParseException ignored ) {
            }

            addDateRangesToQuery( startDate, endDate, req, query );

            String sortBy = req.getParameter( "sortBy" );

            // Lets get the language prefix
            String langPrefix = user.getLanguageIso639_2();

            // Lets check that the sortby option is valid by run the method
            // "SortOrder_GetExistingDocs 'lang_prefix' wich will return
            // an array with all the document types. By adding the key-value pair
            // array into an hashtable and check if the sortorder exists in the hashtable.
            // we are able to determine if the sortorder is okay.

            // Lets fix the sortby list, first get the displaytexts from the database
            String[][] sortOrder = imcref.sqlProcedureMulti( "SortOrder_GetExistingDocs", new String[]{langPrefix} );
            Map sortOrderHash = convert2Hashtable( sortOrder );
            if ( !sortOrderHash.containsKey( sortBy ) ) {
                sortBy = "meta_id";
            }

            List sortOrderV = new ArrayList();
            for ( int i = 0; i < sortOrder.length; i++ ) {
                sortOrderV.add( sortOrder[i][0] );
                sortOrderV.add( sortOrder[i][1] );
            }

            log.debug( "Query: " + query );
            DocumentDomainObject[] searchResultDocuments = index.search( query, user );

            boolean onlyOneDocumentFound = 1 == searchResultDocuments.length;

            if ( onlyOneDocumentFound ) {
                DocumentDomainObject onlyDocumentFound = searchResultDocuments[0];
                if ( searchString.equals( "" + onlyDocumentFound.getId() ) ) {
                    addDocumentToMenu( onlyDocumentFound, parentDocument, menuIndex, user );
                    redirectBackToMenu( res, parentDocument, menuIndex );
                    return;
                }
            }
            createSearchResultsPage( imcref, user, langPrefix, searchResultDocuments, parentDocument, menuIndex, req, startDate,
                                     dateFormat, endDate, docTypes, sortBy, sortOrderV, out );

        } else {
            addDocumentsFromRequestToMenu( user, req, imcref, parentDocument, menuIndex, res );
            redirectBackToMenu( res, parentDocument, menuIndex );
        }
    }

    private class DocumentDomainObjectComparator implements Comparator {

        private String sortBy;

        private DocumentDomainObjectComparator( String sortBy ) {
            this.sortBy = sortBy;
        }

        public int compare( Object o1, Object o2 ) {
            DocumentDomainObject d1 = (DocumentDomainObject)o1;
            DocumentDomainObject d2 = (DocumentDomainObject)o2;
            if ( "meta_headline".equalsIgnoreCase( sortBy ) ) {
                return d1.getHeadline().compareToIgnoreCase( d2.getHeadline() );
            } else if ( "doc_type".equalsIgnoreCase( sortBy ) ) {
                return d1.getDocumentTypeId() - d2.getDocumentTypeId();
            } else if ( "date_modified".equalsIgnoreCase( sortBy ) ) {
                return Utility.compareDatesWithNullFirst( d1.getModifiedDatetime(), d2.getModifiedDatetime() );
            } else if ( "date_created".equalsIgnoreCase( sortBy ) ) {
                return Utility.compareDatesWithNullFirst( d1.getCreatedDatetime(), d2.getCreatedDatetime() );
            } else if ( "date_archived".equalsIgnoreCase( sortBy ) ) {
                return Utility.compareDatesWithNullFirst( d1.getArchivedDatetime(), d2.getArchivedDatetime() );
            } else if ( "date_activated".equalsIgnoreCase( sortBy ) ) {
                return Utility.compareDatesWithNullFirst( d1.getPublicationStartDatetime(), d2.getPublicationStartDatetime() );
            } else {
                return d1.getId() - d2.getId();
            }
        }

    }

    private void addDocumentsFromRequestToMenu( UserDomainObject user, HttpServletRequest req,
                                                IMCServiceInterface imcref, TextDocumentDomainObject parentDocument,
                                                int menuIndex, HttpServletResponse res ) throws IOException {
        user.put( "flags", new Integer( IMCConstants.PERM_EDIT_TEXT_DOCUMENT_MENUS ) );

        // get the seleced existing docs
        String[] values = req.getParameterValues( "existing_meta_id" );
        if ( values == null ) {
            values = new String[0];
        }

        DocumentMapper documentMapper = imcref.getDocumentMapper();

        // Lets loop through all the selected existsing meta ids and add them to the current menu
        for ( int m = 0; m < values.length; m++ ) {
            int existingDocumentId = Integer.parseInt( values[m] );

            DocumentDomainObject existingDocument = documentMapper.getDocument( existingDocumentId );
            // Add the document in menu if user is admin for the document OR the document is shared.
            addDocumentToMenu( existingDocument, parentDocument, menuIndex, user );

        }
    }

    private void redirectBackToMenu( HttpServletResponse res, TextDocumentDomainObject parentDocument, int menuIndex ) throws IOException {
        res.sendRedirect( "AdminDoc?meta_id=" + parentDocument.getId() + "&flags="
                          + IMCConstants.DISPATCH_FLAG__EDIT_MENU
                          + "&editmenu="
                          + menuIndex );
    }

    private Set getUsersAllowedDocumentTypeIdsOnDocument( UserDomainObject user,
                                                          TextDocumentDomainObject parentDocument ) {
        DocumentMapper documentMapper = ApplicationServer.getIMCServiceInterface().getDocumentMapper();
        int[] allowedDocumentTypeIdsArray = ( (TextDocumentPermissionSetDomainObject)documentMapper.getDocumentPermissionSetForUser( parentDocument, user ) ).getAllowedDocumentTypeIds();
        Set allowedDocumentTypeIds = new HashSet( Arrays.asList( ArrayUtils.toObject( allowedDocumentTypeIdsArray ) ) );
        return allowedDocumentTypeIds;
    }

    private void addDocumentToMenu( DocumentDomainObject document, TextDocumentDomainObject parentDocument,
                                    int menuIndex, UserDomainObject user ) {
        Set allowedDocumentTypeIds = getUsersAllowedDocumentTypeIdsOnDocument( user, parentDocument ) ;
        DocumentMapper documentMapper = ApplicationServer.getIMCServiceInterface().getDocumentMapper();
        boolean sharePermission = documentMapper.userHasPermissionToAddDocumentToAnyMenu( user, document );
        boolean canAddToMenu = allowedDocumentTypeIds.contains( new Integer( document.getDocumentTypeId() ) )
                               && sharePermission;
        if ( canAddToMenu ) {
            parentDocument.getMenu( menuIndex ).addMenuItem( new MenuItemDomainObject( document ) );
            documentMapper.saveDocument( parentDocument, user );
        }
    }

    private void createSearchResultsPage( IMCServiceInterface imcref, imcode.server.user.UserDomainObject user,
                                          String langPrefix, DocumentDomainObject[] searchResultDocuments, TextDocumentDomainObject parentDocument,
                                          int doc_menu_no, HttpServletRequest req, Date startDate,
                                          DateFormat dateFormat, Date endDate, String[] docTypes, String sortBy,
                                          List sortOrderV, Writer out ) throws IOException {
        Comparator searchResultsComparator = new DocumentDomainObjectComparator( sortBy );
        Arrays.sort( searchResultDocuments, searchResultsComparator );

        StringBuffer searchResults;
        List outVector = new ArrayList();

        // Lets get the resultpage fragment used for an result
        String oneRecHtmlSrc = imcref.getAdminTemplate( ONE_SEARCH_HIT, user, null );

        // Lets get all document types and put them in a hashTable
        String[][] allDocTypesArray = imcref.getAllDocumentTypes( langPrefix );
        Map allDocTypesHash = convert2Hashtable( allDocTypesArray );

        // Lets parse the searchresults
        searchResults = parseSearchResults( oneRecHtmlSrc, searchResultDocuments, allDocTypesHash );

        // Lets get the surrounding resultpage fragment used for all the result
        // and parse all the results into this summarize html template for all the results
        List tmpV = new ArrayList();
        tmpV.add( "#searchResults#" );
        tmpV.add( searchResults.toString() );
        searchResults.replace( 0, searchResults.length(), imcref.getAdminTemplate( SEARCH_RESULTS, user, tmpV ) );

        // Lets parse out hidden fields
        outVector.add( "#meta_id#" );
        outVector.add( "" + parentDocument.getId() );
        outVector.add( "#doc_menu_no#" );
        outVector.add( "" + doc_menu_no );

        // Lets get the searchstring and add it to the page
        outVector.add( "#searchstring#" );
        String searchStr = req.getParameter( "searchstring" ) == null ? "" : req.getParameter( "searchstring" );
        outVector.add( searchStr );

        outVector.add( "#start_date#" );
        if ( startDate == null ) {
            outVector.add( "" );
        } else {
            outVector.add( dateFormat.format( startDate ) );
        }

        outVector.add( "#end_date#" );
        outVector.add( dateFormat.format( endDate ) );

        if ( docTypes != null ) {
            // Lets take care of the document types. Get those who were selected
            // and select those again in the page to send back to the user.
            // First, put them in an hashtable for easy access.
            Map selectedDocTypes = new HashMap( docTypes.length );
            for ( int i = 0; i < docTypes.length; i++ ) {
                selectedDocTypes.put( docTypes[i], docTypes[i] );
            }

            // Lets get all possible values of for the documenttypes from database
            for ( int i = 0; i < allDocTypesArray.length; i++ ) {
                outVector.add( "#checked_" + allDocTypesArray[i][0] + "#" );
                if ( selectedDocTypes.containsKey( allDocTypesArray[i][0] ) ) {
                    outVector.add( "checked" );
                } else {
                    outVector.add( "" );
                }
            }

        }

        // Lets take care of the created, changed boxes.
        // first, getallchecked values and put them in a hashtable
        String[] includeDocs = req.getParameterValues( "include_doc" );
        if ( includeDocs == null ) {
            includeDocs = new String[0];
        }

        Set selectedIncludeDocs = new HashSet( includeDocs.length );
        for ( int i = 0; i < includeDocs.length; i++ ) {
            selectedIncludeDocs.add( includeDocs[i] );
        }

        // Lets create an array with all possible values.
        // in this case just changed resp. created
        String[] allPossibleIncludeDocsValues = {"created", "changed"};
        for ( int i = 0; i < allPossibleIncludeDocsValues.length; i++ ) {
            outVector.add( "#include_check_" + allPossibleIncludeDocsValues[i] + "#" );
            if ( selectedIncludeDocs.contains( allPossibleIncludeDocsValues[i] ) ) {
                outVector.add( "checked" );
            } else {
                outVector.add( "" );
            }
        }

        // Lets take care of the search_prep condition, eg and / or
        // first, getallchecked values and put them in a hashtable
        String[] searchPrepArr = req.getParameterValues( "search_prep" );
        if ( searchPrepArr == null ) {
            searchPrepArr = new String[0];
        }

        Hashtable selectedsearchPrep = new Hashtable( searchPrepArr.length );
        for ( int i = 0; i < searchPrepArr.length; i++ ) {
            selectedsearchPrep.put( searchPrepArr[i], searchPrepArr[i] );
        }
        // Lets create an array with all possible values.
        // in this case just changed resp. created
        String[] allPossibleSearchPreps = {"and", "or"};
        for ( int i = 0; i < allPossibleSearchPreps.length; i++ ) {
            outVector.add( "#search_prep_check_" + allPossibleSearchPreps[i] + "#" );
            if ( selectedsearchPrep.containsKey( allPossibleSearchPreps[i] ) ) {
                outVector.add( "checked" );
            } else {
                outVector.add( "" );
            }
        }

        String sortOrderStr = Html.createOptionList( sortBy, sortOrderV );
        outVector.add( "#sortBy#" );
        outVector.add( sortOrderStr );

        outVector.add( "#searchResults#" );
        outVector.add( searchResults.toString() );

        // Send page to browser
        // htmlOut = imcref.replaceTagsInStringWithData( htmlOut, outVector);
        String htmlOut = imcref.getAdminTemplate( ADMIN_TEMPLATE_EXISTING_DOC, user, outVector );
        out.write( htmlOut );
        return;
    }

    private void addDocTypesToQuery( String[] docTypes, BooleanQuery query ) {
        BooleanQuery docTypesQuery = new BooleanQuery();
        for ( int i = 0; null != docTypes && i < docTypes.length; i++ ) {
            String docType = docTypes[i];
            docTypesQuery.add( new TermQuery( new Term( "doc_type_id", docType ) ), false, false );
        }
        query.add( docTypesQuery, true, false );
    }

    private void addDateRangesToQuery( Date startDate, Date endDate, HttpServletRequest req, BooleanQuery query ) {
        if ( null != startDate || null != endDate ) {
            String[] wantedDateFields = req.getParameterValues( "include_doc" );
            for ( int i = 0; null != wantedDateFields && i < wantedDateFields.length; i++ ) {
                String wantedDateField = wantedDateFields[i];
                String wantedIndexDateField;
                if ( "created".equalsIgnoreCase( wantedDateField ) ) {
                    wantedIndexDateField = "created_datetime";
                } else if ( "changed".equalsIgnoreCase( wantedDateField ) ) {
                    wantedIndexDateField = "modified_datetime";
                } else {
                    continue;
                }
                Term startDateTerm = null != startDate
                                     ? new Term( wantedIndexDateField, DateField.dateToString( startDate ) ) : null;
                Term endDateTerm = null != endDate
                                   ? new Term( wantedIndexDateField,
                                               DateField.dateToString( addOneDayToDate( endDate ) ) )
                                   : null;
                RangeQuery dateRangeQuery = new RangeQuery( startDateTerm, endDateTerm, true );
                query.add( dateRangeQuery, true, false );
            }
        }
    }

    private Date addOneDayToDate( Date date ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        calendar.add( Calendar.DATE, 1 );
        return calendar.getTime();
    }

    private void addStringToQuery( final DocumentIndex index, String string, BooleanQuery query )
            throws org.apache.lucene.queryParser.ParseException {
        Query textQuery = new DefaultQueryParser().parse( string );
        query.add( textQuery, true, false );
    }

    /**
     * Local helpmehtod
     * Takes an array as argument and creates an hashtable the information.
     * Expects that the first element will be the key and the next element in the
     * array will be the value.
     */

    private static Hashtable convert2Hashtable( String[][] arr ) {

        Hashtable h = new Hashtable();
        for ( int i = 0; i < arr.length; i++ ) {
            h.put( arr[i][0], arr[i][1] );
        }
        return h;
    }

    /**
     * Local helpmehtod
     * Parses all the searchhits and returns an StringBuffer
     */

    private static StringBuffer parseSearchResults( String oneRecHtmlSrc,
                                                    DocumentDomainObject[] searchResultDocuments, Map docTypesHash ) {
        StringBuffer searchResults = new StringBuffer( 1024 );

        for ( int i = 0; i < searchResultDocuments.length; i++ ) {
            DocumentDomainObject document = searchResultDocuments[i];

            DateFormat dateFormat = new SimpleDateFormat( DateConstants.DATETIME_FORMAT_STRING );
            String[] data = {
                "#meta_id#", String.valueOf( document.getId() ),
                "#doc_type#", (String)docTypesHash.get( "" + document.getDocumentTypeId() ),
                "#meta_headline#", document.getHeadline(),
                "#meta_text#", document.getMenuText(),
                "#date_created#", formatDate( dateFormat, document.getCreatedDatetime() ),
                "#date_modified#", formatDate( dateFormat, document.getModifiedDatetime() ),
                "#date_activated#", formatDate( dateFormat, document.getPublicationStartDatetime() ),
                "#date_archived#", formatDate( dateFormat, document.getArchivedDatetime() ),
                "#archive#", document.isArchived() ? "1" : "0",
            };

            searchResults.append( Parser.parseDoc( oneRecHtmlSrc, data ) );
        }
        return searchResults;
    }

    private static String formatDate( DateFormat dateFormat, Date datetime ) {
        return null != datetime ? dateFormat.format( datetime ) : "&nbsp;";
    }

} // End class
