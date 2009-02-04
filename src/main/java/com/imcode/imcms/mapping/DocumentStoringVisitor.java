package com.imcode.imcms.mapping;

import imcode.server.Imcms;
import imcode.server.ImcmsServices;
import imcode.server.document.DocumentVisitor;
import imcode.server.document.FileDocumentDomainObject;
import imcode.server.document.textdocument.ImageDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.document.textdocument.TextDomainObject;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

import com.imcode.imcms.api.ContentLoop;
import com.imcode.imcms.dao.ContentLoopDao;
import com.imcode.imcms.dao.ImageDao;
import com.imcode.imcms.dao.MenuDao;
import com.imcode.imcms.dao.MetaDao;
import com.imcode.imcms.dao.TextDao;
import com.imcode.imcms.mapping.orm.FileReference;
import com.imcode.imcms.mapping.orm.Include;
import com.imcode.imcms.mapping.orm.TemplateNames;

public class DocumentStoringVisitor extends DocumentVisitor {
	
    protected ImcmsServices services;

    private static final int FILE_BUFFER_LENGTH = 2048;
    private static final int DB_FIELD_MAX_LENGTH__FILENAME = 255;    

    public DocumentStoringVisitor(ImcmsServices services) {
        this.services = services ;
    }

    protected void saveFileDocumentFile( int fileDocumentId, FileDocumentDomainObject.FileDocumentFile fileDocumentFile,
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

            File file = getFileForFileDocumentFile( fileDocumentId, fileId );

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

    public static File getFileForFileDocumentFile( int fileDocumentId, String fileId ) {
        File filePath = Imcms.getServices().getConfig().getFilePath();
        String filename = "" + fileDocumentId ;
        if (StringUtils.isNotBlank( fileId )) {
            filename += "."+FileUtility.escapeFilename(fileId) ;
        }
        return new File(filePath, filename);
    }

    static String makeSqlInsertString(String tableName, String[] columnNames) {
        return "INSERT INTO " + tableName + " (" + StringUtils.join(columnNames, ",") + ")"
                + "VALUES(?" + StringUtils.repeat(",?", columnNames.length - 1) + ")";
    }
   
    
    void updateTextDocumentTexts(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        TextDao textDao = (TextDao)services.getSpringBean("textDao");
        Integer documentId = textDocument.getMeta().getId();
        Integer documentVersion = textDocument.getMeta().getVersion().getVersion();

        for (Map<Integer, TextDomainObject> map: textDocument.getAllTexts().values()) {
        	for (TextDomainObject text: map.values()) {
                if (text.isModified()) {                	 
                	text.setMetaId(documentId);
                	text.setMetaVersion(documentVersion);
                    textDao.saveText(text);
                    textDao.saveTextHistory(documentId, text, user); 
                }        		
        	}
        }
    } 
    
    public void updateTextDocumentContentLoops(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        ContentLoopDao dao = (ContentLoopDao)services.getSpringBean("contentLoopDao");
        Integer documentId = textDocument.getMeta().getId();
        
        for (ContentLoop loop: textDocument.getContentLoopsMap().values()) {
        	loop.setMetaId(documentId);
        	dao.saveOrUpdate(loop);
        }  	
    }
    
    // Transactional
    public void updateTextDocumentText(TextDomainObject text, UserDomainObject user) {
        TextDao textDao = (TextDao)services.getSpringBean("textDao");

        textDao.saveText(text);
        textDao.saveTextHistory(text.getMetaId(), text, user); 
    }    
    
    void updateTextDocumentImages(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
        ImageDao imageDao = (ImageDao)services.getSpringBean("imageDao");
        Integer metaId = textDocument.getMeta().getId();
        Integer documentVersion = textDocument.getMeta().getVersion().getVersion();
        
        for (Map<Integer, ImageDomainObject> map: textDocument.getAllImages().values()) {
        	for (ImageDomainObject image: map.values()) {
                if (image.isModified()) {                	 
                	// TODO: remove
                	image.setMetaId(metaId);
                	image.setMetaVersion(documentVersion);
                    imageDao.saveImage(image);
                    //imageDao.saveImageHistory(metaId, text, user); 
                }        		
        	}
        }        
    }
    
    
    // TODO: transactional - new or can participate
    void updateTextDocumentIncludes(TextDocumentDomainObject textDocument) {
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
    	Set<Include> includes = new HashSet<Include>();
    	Integer documentId = textDocument.getMeta().getId();
    	
    	for (Map.Entry<Integer, Integer> entry: textDocument.getIncludesMap().entrySet()) {
    		Include include = new Include();
    		include.setMetaId(documentId);
    		include.setIndex(entry.getKey());
    		include.setIncludedDocumentId(entry.getValue());
    		
    		includes.add(include);
    	}
    	
    	dao.saveIncludes(documentId, includes);
    }
    
    // TODO: transactional - new or can participate
    void updateTextDocumentTemplateNames(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument, UserDomainObject user) {
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
    	TemplateNames templateNames = textDocument.getTemplateNames();
    	Integer documentId = textDocument.getMeta().getId();
    	    	
    	templateNames.setMetaId(documentId);
    	
    	dao.saveTemplateNames(documentId, templateNames);    	
    }    


    public void visitFileDocument( FileDocumentDomainObject fileDocument ) {    	
    	MetaDao dao = (MetaDao)services.getSpringBean("metaDao");
    	
        Map fileDocumentFiles = fileDocument.getFiles();

        // DELETE
        dao.deleteFileReferences(fileDocument.getMeta().getId());

        
        // Save point...
        for ( Iterator iterator = fileDocumentFiles.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String fileId = (String)entry.getKey();
            FileDocumentDomainObject.FileDocumentFile fileDocumentFile = (FileDocumentDomainObject.FileDocumentFile)entry.getValue();

            String filename = fileDocumentFile.getFilename();
            if ( filename.length() > DB_FIELD_MAX_LENGTH__FILENAME ) {
                filename = truncateFilename( filename, DB_FIELD_MAX_LENGTH__FILENAME );
            }

            boolean isDefaultFile = fileId.equals( fileDocument.getDefaultFileId());
            FileReference fileRef = new FileReference();
            fileRef.setMetaId(fileDocument.getMeta().getId());
            fileRef.setFileId(fileId);
            fileRef.setFilename(filename);
            fileRef.setDefaultFileId(isDefaultFile);
            fileRef.setMimeType(fileDocumentFile.getMimeType());
            fileRef.setCreatedAsImage(fileDocumentFile.isCreatedAsImage());
            
            
            dao.saveFileReference(fileRef);
            
            saveFileDocumentFile( fileDocument.getId(), fileDocumentFile, fileId );
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

    protected void updateTextDocumentMenus(final TextDocumentDomainObject textDocument, final TextDocumentDomainObject oldTextDocument, final UserDomainObject savingUser) {
    	MenuDao dao = (MenuDao)services.getSpringBean("menuDao");

    	dao.saveDocumentMenus(textDocument.getMeta().getId(), textDocument.getMenus());
    }
}