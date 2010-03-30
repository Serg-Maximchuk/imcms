package com.imcode.imcms.mapping;

import com.imcode.imcms.api.DocumentVersion;
import imcode.server.Imcms;
import imcode.server.ImcmsServices;
import imcode.server.document.DocumentVisitor;
import imcode.server.document.FileDocumentDomainObject;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.textdocument.*;
import imcode.server.user.UserDomainObject;
import imcode.util.io.FileInputStreamSource;
import imcode.util.io.FileUtility;
import imcode.util.io.InputStreamSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

import com.imcode.imcms.api.ContentLoop;
import com.imcode.imcms.api.DocumentLabels;
import com.imcode.imcms.dao.*;
import com.imcode.imcms.mapping.orm.FileReference;
import com.imcode.imcms.mapping.orm.Include;
import com.imcode.imcms.mapping.orm.TemplateNames;

/**
 * This class is not a part of public API. It's methods must not be called directly.
 *
 * @see com.imcode.imcms.mapping.DocumentSaver
 */
public class DocumentStoringVisitor extends DocumentVisitor {

    protected ImcmsServices services;

    private static final int FILE_BUFFER_LENGTH = 2048;
    private static final int DB_FIELD_MAX_LENGTH__FILENAME = 255;    

    public DocumentStoringVisitor(ImcmsServices services) {
        this.services = services ;
    }

    protected void saveFileDocumentFile(int fileDocumentId, Integer docVersionNo, FileDocumentDomainObject.FileDocumentFile fileDocumentFile,
                                         String fileId ) {
        try {
            InputStreamSource inputStreamSource = fileDocumentFile.getInputStreamSource();
            InputStream in;
            try {
                in = inputStreamSource.getInputStream();
            } catch (FileNotFoundException e) {
                throw new UnhandledException("The file for filedocument " + fileDocumentId
                        + " has disappeared.", e);
            }
            if (null == in) {
                return;
            }

            File file = getFileForFileDocumentFile(fileDocumentId, docVersionNo, fileId );

            FileInputStreamSource fileInputStreamSource = new FileInputStreamSource(file);
            boolean sameFileOnDisk = file.exists() && inputStreamSource.equals(fileInputStreamSource) ;
            if ( sameFileOnDisk ) {
                return;
            }

            byte[] buffer = new byte[FILE_BUFFER_LENGTH];
            final OutputStream out = new FileOutputStream(file);
            try {
                for (int bytesRead; -1 != (bytesRead = in.read(buffer));) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
                in.close();
            }
            fileDocumentFile.setInputStreamSource(fileInputStreamSource);
        } catch (IOException e) {
            throw new UnhandledException(e);
        }
    }

    public static File getFileForFileDocumentFile(int fileDocumentId, int docVersionNo, String fileId) {
        File filePath = Imcms.getServices().getConfig().getFilePath();
        String filename = "" + fileDocumentId ;

        if (docVersionNo != DocumentVersion.WORKING_VERSION_NO) {
            filename += ("_" + docVersionNo);    
        }

        if (StringUtils.isNotBlank( fileId )) {
            filename += "."+FileUtility.escapeFilename(fileId) ;
        }
        return new File(filePath, filename);
    }
   
    
    void updateTextDocumentTexts(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        TextDao textDao = (TextDao)services.getSpringBean("textDao");
        Integer docId = textDocument.getMeta().getId();
        Integer docVersionNo = textDocument.getVersion().getNo();

        textDao.deleteTexts(docId, docVersionNo);
        textDao.flush();

        for (TextDomainObject text: textDocument.getTexts().values()) {
            saveTextDocumentText(textDocument, text, user);
        }


        for (TextDomainObject text: textDocument.getLoopTexts().values()) {
            saveTextDocumentText(textDocument, text, user);
        }        
    }

    /**
     *
     * @param textDocument
     * @param oldTextDocument
     * @param user
     */
    public void updateTextDocumentContentLoops(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        ContentLoopDao dao = (ContentLoopDao)services.getSpringBean("contentLoopDao");
        Integer metaId = textDocument.getMeta().getId();
        Integer documentVersion = textDocument.getVersion().getNo();
        Integer documentVersionNumber = textDocument.getVersion().getNo();
        
        // delete all loops for meta and version
        dao.deleteLoops(metaId, documentVersionNumber);
        dao.flush();
        
        for (ContentLoop loop: textDocument.getContentLoops().values()) {
            loop.setId(null);
        	loop.setDocId(metaId);
        	loop.setDocVersionNo(documentVersion);
        	
        	dao.saveLoop(loop);
        }  	
    }

    // must be run inside transaction
    public void updateDocumentLabels(DocumentDomainObject doc, DocumentDomainObject oldDoc, UserDomainObject user) {
        //Integer docId, Integer docVersionNo, I18nLanguage language
        DocumentLabels labels = doc.getLabels().clone();
        MetaDao metaDao = (MetaDao)services.getSpringBean("metaDao");
        
        metaDao.saveLabels(labels);
    }

    /**
     * Saves text document's text.
     * 
     * @param doc
     * @param text
     * @param user
     */
    // must run inside transaction
    public void saveTextDocumentText(TextDocumentDomainObject doc, TextDomainObject text, UserDomainObject user) {
        TextDao textDao = (TextDao)services.getSpringBean("textDao");

        text.setDocId(doc.getId());
        text.setDocVersionNo(doc.getVersion().getNo());

        textDao.saveText(text);
        //textDao.saveTextHistory(text.getDocId(), text, user);
    }

    /**
     * Saves text document's image.
     */
    // must be executed within transaction
    public void saveTextDocumentImage(TextDocumentDomainObject doc, ImageDomainObject image, UserDomainObject user) {
        ImageDao imageDao = (ImageDao)services.getSpringBean("imageDao");
     
        image.setDocId(doc.getId());
        image.setDocVersionNo(doc.getVersion().getNo());

        image.setImageUrl(image.getSource().toStorageString());
        image.setType(image.getSource().getTypeId());
        
        imageDao.saveImage(image);
        //imageDao.saveImageHistory(doc.getId(), image, user); 
    }


    // must be executed within transaction
    void updateTextDocumentImages(TextDocumentDomainObject doc, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        ImageDao imageDao = (ImageDao)services.getSpringBean("imageDao");
        Integer docId = doc.getMeta().getId();
        Integer docVersionNo = doc.getVersion().getNo();

        imageDao.deleteImages(docId, docVersionNo);
        imageDao.flush();

        for (ImageDomainObject image: doc.getImages().values()) {
            saveTextDocumentImage(doc, image, user);
            //imageDao.saveImageHistory(doc.getId(), image, user);
        }


        for (ImageDomainObject image: doc.getLoopImages().values()) {
            saveTextDocumentImage(doc, image, user);
            //imageDao.saveImageHistory(doc.getId(), image, user);
        }
    }
    
    
    // must be executed within transaction
    public void updateTextDocumentIncludes(TextDocumentDomainObject doc) {
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
    	Set<Include> includes = new HashSet<Include>();
    	Integer docId = doc.getMeta().getId();
    	
    	for (Map.Entry<Integer, Integer> entry: doc.getIncludesMap().entrySet()) {
    		Include include = new Include();
            include.setId(null);
    		include.setMetaId(docId);
    		include.setIndex(entry.getKey());
    		include.setIncludedDocumentId(entry.getValue());
    		
    		includes.add(include);
    	}

        if (docId != null) {
            dao.deleteIncludes(docId, doc.getVersion().getNo());
        }
        
    	dao.saveIncludes(docId, includes);
    }
    
    // must be executed within transaction
    public void updateTextDocumentTemplateNames(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
    	TemplateNames templateNames = textDocument.getTemplateNames();
    	Integer documentId = textDocument.getMeta().getId();
    	    	
    	templateNames.setMetaId(documentId);
    	
    	dao.saveTemplateNames(documentId, templateNames);    	
    }    


    public void visitFileDocument( FileDocumentDomainObject fileDocument ) {    	
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
        dao.deleteFileReferences(fileDocument.getMeta().getId(), fileDocument.getVersionNo());
        
        for (Map.Entry<String, FileDocumentDomainObject.FileDocumentFile> entry: fileDocument.getFiles().entrySet()) {
            String fileId = entry.getKey();
            FileDocumentDomainObject.FileDocumentFile fileDocumentFile = entry.getValue();

            String filename = fileDocumentFile.getFilename();
            if ( filename.length() > DB_FIELD_MAX_LENGTH__FILENAME ) {
                filename = truncateFilename( filename, DB_FIELD_MAX_LENGTH__FILENAME );
            }

            boolean isDefaultFile = fileId.equals( fileDocument.getDefaultFileId());
            FileReference fileRef = new FileReference();
            fileRef.setDocId(fileDocument.getMeta().getId());
            fileRef.setDocVersionNo(fileDocument.getVersionNo());
            fileRef.setFileId(fileId);
            fileRef.setFilename(filename);
            fileRef.setDefaultFileId(isDefaultFile);
            fileRef.setMimeType(fileDocumentFile.getMimeType());
            fileRef.setCreatedAsImage(fileDocumentFile.isCreatedAsImage());
            
            
            dao.saveFileReference(fileRef);
            
            saveFileDocumentFile(fileDocument.getId(), fileDocument.getVersionNo(), fileDocumentFile, fileId );
        }
        
        DocumentMapper.deleteOtherFileDocumentFiles( fileDocument ) ;   
    }

    private String truncateFilename(String filename, int length) {
        String truncatedFilename = StringUtils.left(filename, length);
        String extensions = getExtensionsFromFilename(filename);
        if (extensions.length() > length) {
            return truncatedFilename;
        }
        String basename = StringUtils.chomp(filename, extensions);
        String truncatedBasename = StringUtils.substring(basename, 0, length - extensions.length());
        truncatedFilename = truncatedBasename + extensions;
        return truncatedFilename;
    }

    private String getExtensionsFromFilename(String filename) {
        String extensions = "";
        Matcher matcher = Pattern.compile("(?:\\.\\w+)+$").matcher(filename);
        if (matcher.find()) {
            extensions = matcher.group();
        }
        return extensions;
    }

    public void updateTextDocumentMenus(final TextDocumentDomainObject doc, final TextDocumentDomainObject oldDoc, final UserDomainObject user) {
    	MenuDao dao = (MenuDao)services.getSpringBean("menuDao");

        // delete menu/items?
        // add menus history?

        Integer docId = doc.getId();
        Integer docVersionNo = doc.getVersion().getNo();

		for (Map.Entry<Integer, MenuDomainObject> entry: doc.getMenus().entrySet()) {
			MenuDomainObject menu = entry.getValue();

			menu.setDocId(docId);
            menu.setDocVersionNo(docVersionNo);
			menu.setNo(entry.getKey());

			dao.saveMenu(menu);
		}
    }

    public void updateTextDocumentMenu(final TextDocumentDomainObject textDocument, final MenuDomainObject menu, final UserDomainObject user) {
    	MenuDao dao = (MenuDao)services.getSpringBean("menuDao");

        //delete menu first?
        
    	dao.saveMenu(menu);
    }
}