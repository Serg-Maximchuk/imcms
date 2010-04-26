package com.imcode.imcms.api;

import imcode.server.document.textdocument.ImageDomainObject;
import imcode.server.document.textdocument.ImageSource;
import imcode.server.document.textdocument.NullImageSource;
import imcode.server.user.UserDomainObject;

import javax.persistence.*;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name="imcms_text_doc_images_history")
public class ImageHistory {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Transient
    private ImageSource source = new NullImageSource();

    @Column(name="doc_id")
    private Integer docId;

    @Column(name="doc_version_no")
    private Integer docVersionNo;

    private Integer no;

    private int width;
    private int height;
    private int border;
    private String align = "";

    @Column(name="alt_text")
    private String alternateText = "";

    @Column(name="low_scr")
    private String lowResolutionUrl = "";

    @Column(name="v_space")
    private int verticalSpace;

    @Column(name="h_space")
    private int horizontalSpace;
    private String target = "";

    @Column(name="linkurl")
    private String linkUrl = "";

    @Column(name="imgurl")
    private String imageUrl = "";

    private Integer type;

    @Column(name="content_loop_no")
    private Integer contentLoopNo;

    @Column(name="content_no")
    private Integer contentNo;

    /**
     * i18n support
     */
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="language_id", referencedColumnName="id")
    private I18nLanguage language;



    @Column(name="user_id")
    private Integer userId;


    @Column(name="modified_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDt;

    public ImageHistory() {}

    public ImageHistory(ImageDomainObject imageDO, UserDomainObject user) {
    	setDocId(imageDO.getDocId());
        setDocVersionNo(imageDO.getDocVersionNo());
    	setNo(imageDO.getNo());

    	setWidth(imageDO.getWidth());
    	setHeight(imageDO.getHeight());
        setBorder(imageDO.getBorder());
        setAlign(imageDO.getAlign());
        setAlternateText(imageDO.getAlternateText());
        setLowResolutionUrl(imageDO.getLowResolutionUrl());
        setVerticalSpace(imageDO.getVerticalSpace());
        setHorizontalSpace(imageDO.getHorizontalSpace());
        setTarget(imageDO.getTarget());
        setLinkUrl(imageDO.getLinkUrl());
        setImageUrl(imageDO.getImageUrl());
        setType(imageDO.getType());
        
    	setLanguage(imageDO.getLanguage());
        setContentLoopNo(imageDO.getContentLoopNo());
        setContentNo(imageDO.getContentNo());
        setUserId(user.getId());
        setModifiedDt(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ImageSource getSource() {
        return source;
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Integer getDocVersionNo() {
        return docVersionNo;
    }

    public void setDocVersionNo(Integer docVersionNo) {
        this.docVersionNo = docVersionNo;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getAlternateText() {
        return alternateText;
    }

    public void setAlternateText(String alternateText) {
        this.alternateText = alternateText;
    }

    public String getLowResolutionUrl() {
        return lowResolutionUrl;
    }

    public void setLowResolutionUrl(String lowResolutionUrl) {
        this.lowResolutionUrl = lowResolutionUrl;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    public int getHorizontalSpace() {
        return horizontalSpace;
    }

    public void setHorizontalSpace(int horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getContentLoopNo() {
        return contentLoopNo;
    }

    public void setContentLoopNo(Integer contentLoopNo) {
        this.contentLoopNo = contentLoopNo;
    }

    public Integer getContentNo() {
        return contentNo;
    }

    public void setContentNo(Integer contentNo) {
        this.contentNo = contentNo;
    }

    public I18nLanguage getLanguage() {
        return language;
    }

    public void setLanguage(I18nLanguage language) {
        this.language = language;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getModifiedDt() {
        return modifiedDt;
    }

    public void setModifiedDt(Date modifiedDt) {
        this.modifiedDt = modifiedDt;
    }
}