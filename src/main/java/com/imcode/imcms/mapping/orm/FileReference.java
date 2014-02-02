/**
 *
 */
package com.imcode.imcms.mapping.orm;

import javax.persistence.*;

//ORDER BY default_variant DESC, variant_name

/**
 * FileDocumentDomainObject Hibernate ORM.
 */
@Entity
@Table(name = "fileupload_docs")
public class FileReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "created_as_image")
    private Boolean createdAsImage;

    @Column(name = "mime")
    private String mimeType;

    @Column(name = "default_variant")
    private Boolean defaultFileId;

    @Column(name = "variant_name")
    private String fileId;

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(name= "docId", column = @Column(name="meta_id"))
    )
    private DocRef docRef;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Boolean getCreatedAsImage() {
        return createdAsImage;
    }

    public void setCreatedAsImage(Boolean createdAsImage) {
        this.createdAsImage = createdAsImage;
    }

    public Boolean isDefaultFileId() {
        return defaultFileId;
    }

    public void setDefaultFileId(Boolean defaultFileId) {
        this.defaultFileId = defaultFileId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public DocRef getDocRef() {
        return docRef;
    }

    public void setDocRef(DocRef docRef) {
        this.docRef = docRef;
    }
}