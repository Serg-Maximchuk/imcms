package com.imcode.imcms.servlet;

import imcode.server.Imcms;
import imcode.server.ImcmsServices;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.index.DefaultQueryParser;
import imcode.server.document.index.DocumentIndex;
import imcode.server.document.index.QueryParser;
import com.imcode.imcms.util.l10n.LocalizedMessage;
import imcode.util.Utility;
import org.apache.commons.collections.SetUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class DocumentFinder extends WebComponent {

    private SelectDocumentCommand selectDocumentCommand;
    private Query restrictingQuery;
    private QueryParser queryParser = new DefaultQueryParser();
    private Set extraSearchResultColumns = SetUtils.orderedSet( new HashSet() ) ;
    private SearchDocumentsPage page ;
    private Comparator documentComparator ;

    public DocumentFinder() {
        this(new SearchDocumentsPage());
    }

    public DocumentFinder(SearchDocumentsPage page) {
        this.page = page ;
        page.setDocumentFinder(this);
    }

    public void selectDocument( DocumentDomainObject selectedDocument, HttpServletRequest request,
                                HttpServletResponse response ) throws IOException, ServletException {
        selectDocumentCommand.selectDocument( selectedDocument, request, response );
    }

    public void forward( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        forwardWithPage(request, response, page);
    }

    void forwardWithPage(HttpServletRequest request, HttpServletResponse response, SearchDocumentsPage page) throws IOException, ServletException {
        ImcmsServices service = Imcms.getServices();
        DocumentIndex index = service.getDocumentMapper().getDocumentIndex();
        BooleanQuery booleanQuery = new BooleanQuery();
        if ( null != page.getQuery() ) {
            booleanQuery.add( page.getQuery(), true, false );
        }
        if ( null != restrictingQuery ) {
            booleanQuery.add( restrictingQuery, true, false );
        }
        if ( booleanQuery.getClauses().length > 0 ) {
            List documentsFound = index.search( booleanQuery, null, Utility.getLoggedOnUser( request ) );
            if (null != documentComparator) {
                Collections.sort(documentsFound, documentComparator) ;
            }
            page.setDocumentsFound( documentsFound );
        }
        page.forward( request, response );
    }

    public boolean isDocumentsSelectable() {
        return null != selectDocumentCommand;
    }

    public void setSelectDocumentCommand( SelectDocumentCommand selectDocumentCommand ) {
        this.selectDocumentCommand = selectDocumentCommand;
    }

    public void setRestrictingQuery( Query restrictingQuery ) {
        this.restrictingQuery = restrictingQuery;
    }

    public void setQueryParser( QueryParser queryParser ) {
        this.queryParser = queryParser;
    }

    public Query parse( String queryString ) throws ParseException {
        return queryParser.parse( queryString );
    }

    public void addExtraSearchResultColumn( SearchResultColumn searchResultColumn ) {
        extraSearchResultColumns.add(searchResultColumn) ;
    }

    public SearchResultColumn[] getExtraSearchResultColumns() {
        return (SearchResultColumn[])extraSearchResultColumns.toArray( new SearchResultColumn[extraSearchResultColumns.size()] );
    }

    public void setDocumentComparator( Comparator documentComparator ) {
        this.documentComparator = documentComparator;
    }

    public interface SelectDocumentCommand extends Serializable {

        void selectDocument( DocumentDomainObject document, HttpServletRequest request,
                             HttpServletResponse response ) throws IOException, ServletException;
    }

    public interface SearchResultColumn extends Serializable {

        String render( DocumentDomainObject document, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException;

        LocalizedMessage getName();
    }
}
