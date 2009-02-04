package com.imcode.imcms.mapping;

import imcode.server.document.DocumentVisitor;
import imcode.server.document.FileDocumentDomainObject;
import imcode.server.document.HtmlDocumentDomainObject;
import imcode.server.document.UrlDocumentDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.util.io.FileInputStreamSource;

import java.io.File;
import java.util.Collection;

import com.imcode.db.Database;
import com.imcode.imcms.dao.MetaDao;
import com.imcode.imcms.mapping.orm.FileReference;
import com.imcode.imcms.mapping.orm.HtmlReference;
import com.imcode.imcms.mapping.orm.UrlReference;

class DocumentInitializingVisitor extends DocumentVisitor {

    private final Database database;

    private TextDocumentInitializer textDocumentInitializer;
    
    private MetaDao metaDao;
    
    DocumentInitializingVisitor(DocumentGetter documentGetter, Collection documentIds,
                                DocumentMapper documentMapper, MetaDao metaDao) {
        this.database = documentMapper.getDatabase();
        this.metaDao = metaDao;
        
        textDocumentInitializer = new TextDocumentInitializer(database, documentGetter, documentIds);
    }

    public void visitFileDocument(final FileDocumentDomainObject document) {    	
    	Collection<FileReference> fileReferences = metaDao.getFileReferences(document.getMeta().getId());
    	
    	for (FileReference fileRef: fileReferences) {
            String fileId = fileRef.getFileId();           
            FileDocumentDomainObject.FileDocumentFile file = new FileDocumentDomainObject.FileDocumentFile();
            
            file.setFilename(fileRef.getFilename());
            file.setMimeType(fileRef.getMimeType());
            file.setCreatedAsImage(fileRef.getCreatedAsImage());
            
            File fileForFileDocument = DocumentStoringVisitor.getFileForFileDocumentFile(document.getId(), fileId);
            if ( !fileForFileDocument.exists() ) {
                File oldlyNamedFileForFileDocument = new File(fileForFileDocument.getParentFile(),
                                                              fileForFileDocument.getName()
                                                              + "_se");
                if ( oldlyNamedFileForFileDocument.exists() ) {
                    fileForFileDocument = oldlyNamedFileForFileDocument;
                }
            }
            
            file.setInputStreamSource(new FileInputStreamSource(fileForFileDocument));
            
            document.addFile(fileId, file);
            
            if (fileRef.isDefaultFileId()) {
                document.setDefaultFileId(fileId);
            }
    		
    	}
    }

    public void visitHtmlDocument(HtmlDocumentDomainObject document) {
    	HtmlReference html = metaDao.getHtmlReference(document.getMeta().getId());
    	document.setHtml(html.getHtml());    	
    }

    public void visitUrlDocument(UrlDocumentDomainObject document) {
    	UrlReference reference = metaDao.getUrlReference(document.getMeta().getId());
    	document.setUrl(reference.getUrl());    	
    }

    public void visitTextDocument(final TextDocumentDomainObject document) {
        textDocumentInitializer.initialize(document) ;
    }
}