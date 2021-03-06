package com.imcode.imcms.mapping.container;

import com.imcode.imcms.mapping.DocumentCommonContent;

import java.util.Objects;

public class DocCommonContentContainer {

    public static DocCommonContentContainer of(VersionRef versionRef, DocumentCommonContent documentCommonContent) {
        return new DocCommonContentContainer(versionRef, documentCommonContent);
    }

    private final VersionRef versionRef;

    private final DocumentCommonContent documentCommonContent;

    public DocCommonContentContainer(VersionRef versionRef, DocumentCommonContent documentCommonContent) {
        this.versionRef = Objects.requireNonNull(versionRef);
        this.documentCommonContent = Objects.requireNonNull(documentCommonContent);
    }

    public VersionRef getVersionRef() {
        return versionRef;
    }

    public DocumentCommonContent getCommonContent() {
        return documentCommonContent;
    }

    public int getDocId() {
        return versionRef.getDocId();
    }

    public int getVersionNo() {
        return versionRef.getNo();
    }
}
