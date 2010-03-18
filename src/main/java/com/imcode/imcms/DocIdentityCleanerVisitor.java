package com.imcode.imcms;

import com.imcode.imcms.api.Content;
import com.imcode.imcms.api.ContentLoop;
import com.imcode.imcms.api.DocumentVersion;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.DocumentVisitor;
import imcode.server.document.textdocument.ImageDomainObject;
import imcode.server.document.textdocument.MenuDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.document.textdocument.TextDomainObject;

/**
 * Cleans doc's identity data.
 * When new doc is created by cloning an existing one its identity and version fields should be cleared/reseted.
 * //todo: clean version?
 */
public class DocIdentityCleanerVisitor extends DocumentVisitor {

    @Override
    public void visitTextDocument(TextDocumentDomainObject doc) {
        visitOtherDocument(doc);

        for (TextDomainObject text: doc.getTexts().values()) {
            text.setId(null);
            text.setDocId(null);
        }

        for (ImageDomainObject image: doc.getImages().values()) {
            image.setId(null);
            image.setDocId(null);
        }

    	for (MenuDomainObject menu: doc.getMenus().values()) {
    		menu.setId(null);
    		menu.setDocId(null);
    	}

    	for (ContentLoop loop: doc.getContentLoops().values()) {
    		loop.setId(null);
    		loop.setDocId(null);
    	}

        doc.getTemplateNames().setId(null);
        doc.getTemplateNames().setMetaId(null);
    }


    @Override
    protected void visitOtherDocument(DocumentDomainObject doc) {
		doc.getMeta().setId(null);
        doc.getLabels().setId(null);

        DocumentVersion version = doc.getVersion();

        version.setId(null);
        version.setDocId(null);
        version.setNo(0);

        // TODO: add created by, created dt.
    }    
}