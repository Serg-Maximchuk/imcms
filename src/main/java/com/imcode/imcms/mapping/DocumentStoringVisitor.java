package com.imcode.imcms.mapping;

import com.imcode.imcms.api.*;
import com.imcode.imcms.dao.ContentLoopDao;
import com.imcode.imcms.dao.ImageDao;
import com.imcode.imcms.dao.MenuDao;
import com.imcode.imcms.dao.MetaDao;
import com.imcode.imcms.dao.TextDao;
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

import com.imcode.imcms.mapping.orm.FileReference;
import com.imcode.imcms.mapping.orm.Include;
import com.imcode.imcms.mapping.orm.TemplateNames;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is not a part of public API. It's methods must not be called directly.
 *
 * @see com.imcode.imcms.mapping.DocumentSaver
 */

//hibernate.batch_update_versioned
public class DocumentStoringVisitor extends DocumentVisitor {

    protected ImcmsServices services;

    private static final int FILE_BUFFER_LENGTH = 2048;
    private static final int DB_FIELD_MAX_LENGTH__FILENAME = 255;

    protected MetaDao metaDao;

    public DocumentStoringVisitor(ImcmsServices services) {
        this.services = services;
        this.metaDao = services.getSpringBean(MetaDao.class);
    }

    /**
     * Saves (possibly rewrites) file if its InputStreamSource has been changed.
     *
     * @param fileDocumentFile
     * @param fileId
     */
    protected void saveFileDocumentFile(DocRef docRef, FileDocumentDomainObject.FileDocumentFile fileDocumentFile,
                                        String fileId) {
        try {
            InputStreamSource inputStreamSource = fileDocumentFile.getInputStreamSource();
            InputStream in;
            try {
                in = inputStreamSource.getInputStream();
            } catch (FileNotFoundException e) {
                throw new UnhandledException("The file for filedocument " + docRef
                        + " has disappeared.", e);
            }
            if (null == in) {
                return;
            }

            File file = getFileForFileDocumentFile(docRef, fileId);

            FileInputStreamSource fileInputStreamSource = new FileInputStreamSource(file);
            boolean sameFileOnDisk = file.exists() && inputStreamSource.equals(fileInputStreamSource);
            if (sameFileOnDisk) {
                in.close();
                return;
            }

            byte[] buffer = new byte[FILE_BUFFER_LENGTH];
            final OutputStream out = new FileOutputStream(file);
            try {
                for (int bytesRead; -1 != (bytesRead = in.read(buffer)); ) {
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


    /**
     * Returns file for FileDocumentFile.
     */
    public static File getFileForFileDocumentFile(DocRef docRef, String fileId) {
        File filePath = Imcms.getServices().getConfig().getFilePath();
        String filename = getFilenameForFileDocumentFile(docRef, fileId);

        return new File(filePath, filename);
    }


    /**
     * Returns FileDocumentFile filename.
     * <p/>
     * File name is a unique combination of doc id, doc version no and fileId (when not a blank).
     * For backward compatibility a doc version no is omitted if it equals to 0 (working version).
     * <p/>
     * If fieldId is not blank its added to filename as an extension.
     * <p/>
     * Examples:
     * 1002.xxx - 1002 is a doc id, doc version no is 0 and xxx is fileId.
     * 1002_3.xxx - 1002 is a doc id, 3 is a version no and xxx is fileId.
     * 1002_2 - 1002 is a doc id, 2 is a version no and fileId is blank.
     *
     * @param fileId
     * @return FileDocumentFile filename
     */
    public static String getFilenameForFileDocumentFile(DocRef docRef, String fileId) {
        int docId = docRef.docId();
        int docVersionNo = docRef.docVersionNo();

        String filename = "" + docId;

        if (docVersionNo != DocumentVersion.WORKING_VERSION_NO) {
            filename += ("_" + docVersionNo);
        }

        if (StringUtils.isNotBlank(fileId)) {
            filename += "." + FileUtility.escapeFilename(fileId);
        }

        return filename;
    }


    /**
     * Saves text document text fields.
     * <p/>
     * Deletes all existing text fields and then inserts new.
     *
     * @param textDocument
     * @param user
     */
    @Transactional
    void updateTextDocumentTexts(TextDocumentDomainObject textDocument, UserDomainObject user) {
        TextDao textDao = services.getSpringBean(TextDao.class);
        I18nLanguage language = textDocument.getLanguage();

        textDao.deleteTexts(textDocument.getRef(), language);
        textDao.flush();

        for (TextDomainObject text : textDocument.getTexts().values()) {
            text.setId(null);
            text.setDocRef(textDocument.getRef());
            text.setLanguage(language);

            saveTextDocumentText(text, user);
        }

        for (TextDomainObject text : textDocument.getLoopTexts().values()) {
            text.setId(null);
            text.setDocRef(textDocument.getRef());
            text.setLanguage(language);

            saveTextDocumentText(text, user);
        }
    }

    /**
     * @param textDocument
     * @param user
     */
    public void updateTextDocumentContentLoops(TextDocumentDomainObject textDocument, UserDomainObject user) {
        ContentLoopDao dao = services.getSpringBean(ContentLoopDao.class);

        dao.deleteLoops(textDocument.getRef());
        dao.flush();

        for (ContentLoop loop : textDocument.getContentLoops().values()) {
            dao.saveLoop(ContentLoop.builder(loop).id(null).docRef(textDocument.getRef()).build());
        }
    }


    @Transactional
    public void updateDocumentI18nMeta(DocumentDomainObject doc, UserDomainObject user) {
        metaDao.deleteI18nMeta(doc.getId(), doc.getLanguage().getId());

        I18nMeta i18nMeta = I18nMeta.builder(doc.getI18nMeta()).id(null).docId(doc.getIdValue()).language(doc.getLanguage()).build();

        metaDao.saveI18nMeta(i18nMeta);
    }


    /**
     * Saves text document's text.
     *
     * @param text
     * @param user
     */
    @Transactional
    public void saveTextDocumentText(TextDomainObject text, UserDomainObject user) {
        TextDao textDao = services.getSpringBean(TextDao.class);

        textDao.saveText(text);

        TextHistory textHistory = new TextHistory(text, user);
        textDao.saveTextHistory(textHistory);
    }

    /**
     * Saves text document's image.
     */
    @Transactional
    public void saveTextDocumentImage(ImageDomainObject image, UserDomainObject user) {
        ImageDao imageDao = services.getSpringBean(ImageDao.class);

        image.setImageUrl(image.getSource().toStorageString());
        image.setType(image.getSource().getTypeId());

        imageDao.saveImage(image);

        ImageHistory imageHistory = new ImageHistory(image, user);
        imageDao.saveImageHistory(imageHistory);
    }


    @Transactional
    void updateTextDocumentImages(TextDocumentDomainObject doc, UserDomainObject user) {
        ImageDao imageDao = services.getSpringBean(ImageDao.class);
        I18nLanguage language = doc.getLanguage();

        imageDao.deleteImages(doc.getRef(), language);
        imageDao.flush();

        for (ImageDomainObject image : doc.getImages().values()) {
            image.setId(null);
            image.setDocRef(doc.getRef());
            image.setLanguage(language);

            saveTextDocumentImage(image, user);
        }

        for (ImageDomainObject image : doc.getLoopImages().values()) {
            image.setId(null);
            image.setDocRef(doc.getRef());
            image.setLanguage(language);

            saveTextDocumentImage(image, user);
        }
    }


    @Transactional
    public void updateTextDocumentIncludes(TextDocumentDomainObject doc) {
        Integer docId = doc.getIdValue();

        metaDao.deleteIncludes(docId);

        for (Map.Entry<Integer, Integer> entry : doc.getIncludesMap().entrySet()) {
            Include include = new Include();
            include.setId(null);
            include.setMetaId(docId);
            include.setIndex(entry.getKey());
            include.setIncludedDocumentId(entry.getValue());

            metaDao.saveInclude(include);
        }
    }


    @Transactional
    public void updateTextDocumentTemplateNames(TextDocumentDomainObject textDocument, UserDomainObject user) {
        Integer docId = textDocument.getIdValue();

        TemplateNames templateNames = textDocument.getTemplateNames();

        templateNames.setDocId(docId);

        metaDao.saveTemplateNames(templateNames);
    }


    public void visitFileDocument(FileDocumentDomainObject fileDocument) {
        metaDao.deleteFileReferences(fileDocument.getRef());

        for (Map.Entry<String, FileDocumentDomainObject.FileDocumentFile> entry : fileDocument.getFiles().entrySet()) {
            String fileId = entry.getKey();
            FileDocumentDomainObject.FileDocumentFile fileDocumentFile = entry.getValue();

            String filename = fileDocumentFile.getFilename();
            if (filename.length() > DB_FIELD_MAX_LENGTH__FILENAME) {
                filename = truncateFilename(filename, DB_FIELD_MAX_LENGTH__FILENAME);
            }

            boolean isDefaultFile = fileId.equals(fileDocument.getDefaultFileId());
            FileReference fileRef = new FileReference();
            fileRef.setDocRef(fileDocument.getRef());
            fileRef.setFileId(fileId);
            fileRef.setFilename(filename);
            fileRef.setDefaultFileId(isDefaultFile);
            fileRef.setMimeType(fileDocumentFile.getMimeType());
            fileRef.setCreatedAsImage(fileDocumentFile.isCreatedAsImage());

            metaDao.saveFileReference(fileRef);

            saveFileDocumentFile(fileDocument.getRef(), fileDocumentFile, fileId);
        }

        DocumentMapper.deleteOtherFileDocumentFiles(fileDocument);
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

    public void updateTextDocumentMenus(final TextDocumentDomainObject doc, final UserDomainObject user) {
        MenuDao dao = services.getSpringBean(MenuDao.class);

        Integer docId = doc.getId();
        Integer docVersionNo = doc.getVersionNo();
        DocRef docRef = docId == null || docVersionNo == null ? null : DocRef.of(docId, docVersionNo);

        dao.deleteMenus(doc.getRef());

        for (Map.Entry<Integer, MenuDomainObject> entry : doc.getMenus().entrySet()) {
            MenuDomainObject menu = entry.getValue();

            menu.setId(null);
            menu.setDocRef(docRef);
            menu.setNo(entry.getKey());

            updateTextDocumentMenu(menu, user);
        }
    }


    public void updateTextDocumentMenu(final MenuDomainObject menu, final UserDomainObject user) {
        MenuDao dao = services.getSpringBean(MenuDao.class);

        dao.saveMenu(menu);

        MenuHistory menuHistory = new MenuHistory(menu, user);
        dao.saveMenuHistory(menuHistory);
    }
}