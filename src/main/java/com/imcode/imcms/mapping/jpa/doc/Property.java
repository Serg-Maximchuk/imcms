package com.imcode.imcms.mapping.jpa.doc;

import javax.persistence.*;

/**
 * Document (meta) property.
 */
@Entity
@Table(name = "document_properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "meta_id", nullable = false)
    private Integer docId;

    @Column(name = "key_name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}