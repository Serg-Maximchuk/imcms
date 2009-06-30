package com.imcode.imcms.api;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents document's version.
 */
@Entity(name="DocumentVersion")
@Table(name="meta_version")
public class DocumentVersion implements Cloneable {

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;

	@Column(name="meta_id", updatable=false)
	private Integer documentId;

	/**
	 * Version number
	 */
	@Column(name="version")	
	private Integer number;
	
	@Column(name="user_id", updatable=false)	
	private Integer userId;
	
	@Column(name="created_dt")	
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDt;	

	@Enumerated(EnumType.STRING)
	@Column(name="version_tag")
	private DocumentVersionTag tag;
	
	public DocumentVersion() {}
	
	public DocumentVersion(Integer documentId, Integer versionNumber, DocumentVersionTag versionTag) {
		this.documentId = documentId;
		this.number = versionNumber;
		this.tag = versionTag;
	}	
	
	@Override
	public DocumentVersion clone() {
		try {
			return (DocumentVersion)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	
	/**
	 * Use getNumber instead
	 */
	@Deprecated
	public Integer getVersion() {
		return number;
	}

	
	/**
	 * Use setNumber instead
	 */	
	@Deprecated
	public void setVersion(Integer version) {
		this.number = version;
	}
	
	/**
	 * @return version number.
	 */
	public Integer getNumber() {
		return getVersion();
	}

	
	/**
	 * Sets version number.
	 * 
	 * @param number version number
	 */	
	public void setNumber(Integer number) {
		setVersion(number);
	}	

	/**
	 * Use getTag instead
	 */	
	@Deprecated	
	public DocumentVersionTag getVersionTag() {
		return tag;
	}

	/**
	 * Use setTag instead
	 */	
	@Deprecated	
	public void setVersionTag(DocumentVersionTag versionTag) {
		this.tag = versionTag;
	}
	
	/** 
	 * @return document version tag.
	 */
	public DocumentVersionTag getTag() {
		return getVersionTag();
	}

	/** 
	 * Sets document version tag.
	 * 
	 * @param tag document version tag.
	 */
	public void setTag(DocumentVersionTag tag) {
		setVersionTag(tag);
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}
}