package com.imcode.imcms.addon.imagearchive.service;

import imcode.server.user.RoleDomainObject;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.imcode.imcms.addon.imagearchive.command.SearchImageCommand;
import com.imcode.imcms.addon.imagearchive.entity.Categories;
import com.imcode.imcms.addon.imagearchive.entity.Exif;
import com.imcode.imcms.addon.imagearchive.entity.ExifPK;
import com.imcode.imcms.addon.imagearchive.entity.ImageCategories;
import com.imcode.imcms.addon.imagearchive.entity.ImageKeywords;
import com.imcode.imcms.addon.imagearchive.entity.Images;
import com.imcode.imcms.addon.imagearchive.entity.Keywords;
import com.imcode.imcms.addon.imagearchive.util.Pagination;
import com.imcode.imcms.addon.imagearchive.util.Utils;
import com.imcode.imcms.addon.imagearchive.util.exif.ExifData;
import com.imcode.imcms.addon.imagearchive.util.exif.ExifUtils;
import com.imcode.imcms.api.User;
import imcode.util.image.ImageInfo;
import org.hibernate.SessionFactory;


@Transactional
public class ImageService {
    private static final Pattern LIKE_SPECIAL_PATTERN = Pattern.compile("([%_|])");
    
    @Autowired
    private Facade facade;

    @Autowired
    private SessionFactory factory;
    
    @Autowired
    private PlatformTransactionManager txManager;
    
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public Images findById(long imageId, User user) {
        Session session = factory.getCurrentSession();

        Query query = session.createQuery("FROM Images im WHERE im.id = :id AND im.status <> :statusUploaded")
                .setLong("id", imageId)
                .setShort("statusUploaded", Images.STATUS_UPLOADED);

        Images image = (Images) query.uniqueResult();
        if (image == null) {
            return null;
        }

        Exif exif = (Exif) session.get(Exif.class, new ExifPK(imageId, Exif.TYPE_CHANGED));
        image.setChangedExif(exif);

        if (user.isDefaultUser()) {
            return image;
        } else if (user.isSuperAdmin() || image.getUsersId() == user.getId()) {
            image.setCanChange(true);
        } else {
            List<Integer> roleIds = UserService.getRoleIdsWithPermission(user, RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);

            if (!roleIds.isEmpty()) {
                long count = (Long) session.createQuery(
                        "SELECT count(cr.roleId) FROM CategoryRoles cr, ImageCategories ic " +
                        "WHERE ic.categoryId = cr.categoryId AND ic.imageId = :imageId AND cr.roleId IN (:roleIds) ")
                        .setLong("imageId", imageId)
                        .setParameterList("roleIds", roleIds)
                        .uniqueResult();
                image.setCanChange(count != 0L);
            }
        }

        return image;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public Exif findExifByPK(long imageId, short type) {
        return (Exif) factory.getCurrentSession()
                .get(Exif.class, new ExifPK(imageId, type));
    }

    public Images createImage(File tempFile, ImageInfo imageInfo, String imageName, User user) {
        Images image = new Images();
        
        String copyright = "";
        String description = "";
        String artist = "";
        int resolution = 0;
        
        ExifData data = ExifUtils.getExifData(tempFile);
        if (data != null) {
            copyright = StringUtils.substring(data.getCopyright(), 0, 255);
            description = StringUtils.substring(data.getDescription(), 0, 255);
            artist = StringUtils.substring(data.getArtist(), 0, 255);
            resolution = data.getResolution();
        }
        
        Exif originalExif = new Exif(resolution, description, artist, copyright, Exif.TYPE_ORIGINAL);
        Exif changedExif = new Exif(resolution, description, artist, copyright, Exif.TYPE_CHANGED);
        
        String uploadedBy = String.format("%s %s", user.getFirstName(), user.getLastName()).trim();
        image.setUploadedBy(uploadedBy);
        image.setUsersId(user.getId());
        
        image.setImageNm(StringUtils.substring(imageName, 0, 255));
        image.setFormat(imageInfo.getFormat().getOrdinal());
        image.setFileSize((int) tempFile.length());
        image.setWidth(imageInfo.getWidth());
        image.setHeight(imageInfo.getHeight());

        Session session = factory.getCurrentSession();
        session.persist(image);
        
        originalExif.setImageId(image.getId());
        changedExif.setImageId(image.getId());
        session.persist(originalExif);
        session.persist(changedExif);
        
        session.flush();
        image.setChangedExif(changedExif);
        
        if (!facade.getFileService().storeImage(tempFile, image.getId(), false)) {
            txManager.rollback(txManager.getTransaction(null));
            
            return null;
        }
        
        return image;
    }
    
    public void createImages(List<Object[]> tuples, User user) {
        Session session = factory.getCurrentSession();

        for (Object[] tuple : tuples) {
            File tempFile = (File) tuple[0];
            ImageInfo imageInfo = (ImageInfo) tuple[1];
            String imageName = (String) tuple[2];
            
            String copyright = "";
            String description = "";
            String artist = "";
            int resolution = 0;

            ExifData data = ExifUtils.getExifData(tempFile);
            if (data != null) {
                copyright = StringUtils.substring(data.getCopyright(), 0, 255);
                description = StringUtils.substring(data.getDescription(), 0, 255);
                artist = StringUtils.substring(data.getArtist(), 0, 255);
                resolution = data.getResolution();
            }
            
            Exif originalExif = new Exif(resolution, description, artist, copyright, Exif.TYPE_ORIGINAL);
            Exif changedExif = new Exif(resolution, description, artist, copyright, Exif.TYPE_CHANGED);
            
            Images image = new Images();
            String uploadedBy = String.format("%s %s", user.getFirstName(), user.getLastName()).trim();
            image.setUploadedBy(uploadedBy);
            image.setUsersId(user.getId());

            image.setImageNm(StringUtils.substring(imageName, 0, 255));
            image.setFormat(imageInfo.getFormat().getOrdinal());
            image.setFileSize((int) tempFile.length());
            image.setWidth(imageInfo.getWidth());
            image.setHeight(imageInfo.getHeight());
            image.setStatus(Images.STATUS_ACTIVE);

            session.persist(image);

            originalExif.setImageId(image.getId());
            changedExif.setImageId(image.getId());
            session.persist(originalExif);
            session.persist(changedExif);
            
            facade.getFileService().storeImage(tempFile, image.getId(), false);
        }
        session.flush();
    }
    
    public void deleteImage(long imageId) {
        Session session = factory.getCurrentSession();

        session.createQuery("DELETE ImageCategories ic WHERE ic.imageId = :imageId")
                .setLong("imageId", imageId)
                .executeUpdate();

        session.createQuery("DELETE ImageKeywords ik WHERE ik.imageId = :imageId")
                .setLong("imageId", imageId)
                .executeUpdate();

        session.createQuery("DELETE Exif e WHERE e.imageId = :imageId")
                .setLong("imageId", imageId)
                .executeUpdate();

        session.createQuery("DELETE Images im WHERE im.id = :imageId")
                .setLong("imageId", imageId)
                .executeUpdate();
        
        facade.getFileService().deleteImage(imageId);
    }
    
    public void updateFullData(Images image, List<Integer> categoryIds, List<String> imageKeywords) {

        Session session = factory.getCurrentSession();

        session.getNamedQuery("updateFullImageData")
                .setString("imageNm", image.getImageNm())
                .setInteger("width", image.getWidth())
                .setInteger("height", image.getHeight())
                .setInteger("fileSize", image.getFileSize())
                .setShort("format", image.getFormat())
                .setString("uploadedBy", image.getUploadedBy())
                .setDate("licenseDt", image.getLicenseDt())
                .setDate("licenseEndDt", image.getLicenseEndDt())
                .setDate("publishDt", image.getPublishDt())
                .setDate("archiveDt", image.getArchiveDt())
                .setDate("publishEndDt", image.getPublishEndDt())
                .setShort("statusActive", Images.STATUS_ACTIVE)
                .setLong("id", image.getId())
                .executeUpdate();

        image.setStatus(Images.STATUS_ACTIVE);

        Exif changedExif = image.getChangedExif();
        session.getNamedQuery("updateImageExifFull")
                .setString("artist", changedExif.getArtist())
                .setString("description", changedExif.getDescription())
                .setString("copyright", changedExif.getCopyright())
                .setInteger("resolution", changedExif.getResolution())
                .setLong("imageId", image.getId())
                .setShort("exifType", Exif.TYPE_CHANGED)
                .executeUpdate();

        Exif originalExif = image.getOriginalExif();
        session.getNamedQuery("updateImageExifFull")
                .setString("artist", originalExif.getArtist())
                .setString("description", originalExif.getDescription())
                .setString("copyright", originalExif.getCopyright())
                .setInteger("resolution", originalExif.getResolution())
                .setLong("imageId", image.getId())
                .setShort("exifType", Exif.TYPE_ORIGINAL)
                .executeUpdate();

        updateImageCategories(session, image, categoryIds);
        updateImageKeywords(session, image, imageKeywords);

    }
    
    public void updateData(Images image, List<Integer> categoryIds, List<String> imageKeywords) {

        Session session = factory.getCurrentSession();

        session.getNamedQuery("updateImageData")
                .setString("imageNm", image.getImageNm())
                .setString("uploadedBy", image.getUploadedBy())
                .setDate("licenseDt", image.getLicenseDt())
                .setDate("licenseEndDt", image.getLicenseEndDt())
                .setDate("publishDt", image.getPublishDt())
                .setDate("archiveDt", image.getArchiveDt())
                .setDate("publishEndDt", image.getPublishEndDt())
                .setShort("statusActive", Images.STATUS_ACTIVE)
                .setLong("id", image.getId())
                .executeUpdate();

        image.setStatus(Images.STATUS_ACTIVE);

        Exif exif = image.getChangedExif();
        session.getNamedQuery("updateImageExif")
                .setString("artist", exif.getArtist())
                .setString("description", exif.getDescription())
                .setString("copyright", exif.getCopyright())
                .setLong("imageId", image.getId())
                .setShort("changedType", Exif.TYPE_CHANGED)
                .executeUpdate();

        updateImageCategories(session, image, categoryIds);
        updateImageKeywords(session, image, imageKeywords);

    }
    
    private static void updateImageCategories(Session session, Images image, List<Integer> categoryIds) {
        if (categoryIds.isEmpty()) {
            session.createQuery("DELETE FROM ImageCategories ic WHERE ic.imageId = :imageId")
                    .setLong("imageId", image.getId())
                    .executeUpdate();
            return;
        }
        
        session.createQuery(
                "DELETE FROM ImageCategories ic WHERE ic.imageId = :imageId AND ic.categoryId NOT IN (:categoryIds)")
                .setLong("imageId", image.getId())
                .setParameterList("categoryIds", categoryIds)
                .executeUpdate();

        List<Integer> existingCategoryIds = session.createQuery(
                "SELECT ic.categoryId FROM ImageCategories ic WHERE ic.imageId = :imageId AND ic.categoryId IN (:categoryIds) ")
                .setLong("imageId", image.getId())
                .setParameterList("categoryIds", categoryIds)
                .list();
        Set<Integer> existingCategoryIdSet = new HashSet<Integer>(existingCategoryIds);

        long imageId = image.getId();
        for (int categoryId : categoryIds) {
            if (!existingCategoryIdSet.contains(categoryId)) {
                ImageCategories imageCategory = new ImageCategories(imageId, categoryId);
                session.persist(imageCategory);
            }
        }
        session.flush();
    }
    
    private static void updateImageKeywords(Session session, Images image, List<String> imageKeywords) {
        long imageId = image.getId();
        if (imageKeywords.isEmpty()) {
            session.createQuery("DELETE FROM ImageKeywords ik WHERE ik.imageId = :imageId")
                    .setLong("imageId", imageId)
                    .executeUpdate();
        } else {
            List<Keywords> existingKeywords = session.createQuery(
                    "SELECT k.id AS id, k.keywordNm AS keywordNm FROM Keywords k " +
                    "WHERE k.keywordNm IN (:keywords) ")
                    .setParameterList("keywords", imageKeywords)
                    .setResultTransformer(Transformers.aliasToBean(Keywords.class))
                    .list();
            Set<String> existingSet = new HashSet<String>(existingKeywords.size());
            for (Keywords k : existingKeywords) {
                existingSet.add(k.getKeywordNm());
            }

            List<String> newKeywordNames = new ArrayList<String>();
            for (String keyword : imageKeywords) {
                if (!existingSet.contains(keyword)) {
                    newKeywordNames.add(keyword);
                }
            }

            List<Long> keywordIds = new ArrayList<Long>(newKeywordNames.size() + existingKeywords.size());
            for (String k : newKeywordNames) {
                Keywords keyword = new Keywords();
                keyword.setKeywordNm(k);
                session.persist(keyword);

                keywordIds.add(keyword.getId());
            }
            for (Keywords k : existingKeywords) {
                keywordIds.add(k.getId());
            }

            List<Long> existingImageKeywordIds = session.createQuery(
                    "SELECT ik.keywordId FROM ImageKeywords ik WHERE ik.imageId = :imageId")
                    .setLong("imageId", imageId)
                    .list();
            List<Long> toDelete = new ArrayList<Long>();
            for (Long id : existingImageKeywordIds) {
                if (keywordIds.contains(id)) {
                    keywordIds.remove(id);
                } else {
                    toDelete.add(id);
                }
            }

            if (!toDelete.isEmpty()) {
                session.createQuery("DELETE FROM ImageKeywords ik WHERE ik.imageId = :imageId AND ik.keywordId IN (:keywordIds)")
                        .setLong("imageId", imageId)
                        .setParameterList("keywordIds", toDelete)
                        .executeUpdate();
            }

            for (Long id : keywordIds) {
                ImageKeywords ik = new ImageKeywords();
                ik.setKeywordId(id);
                ik.setImageId(imageId);
                session.persist(ik);
            }
            session.flush();
        }
    }
    
    public void archiveImage(long imageId) {

        factory.getCurrentSession()
                .createQuery("UPDATE Images im SET im.status = :statusArchived WHERE im.id = :id")
                .setShort("statusArchived", Images.STATUS_ARCHIVED)
                .setLong("id", imageId)
                .executeUpdate();

    }
    
    private Query buildSearchImagesQuery(SearchImageCommand command, boolean count, 
            List<Integer> categoryIds, User user) {

        StringBuilder builder = new StringBuilder();
        
        builder.append("SELECT ");
        
        if (count) {
            builder.append("count(DISTINCT im.id) ");
        } else {
            builder.append(
            		"DISTINCT im.id AS id, im.imageNm AS imageNm, im.width AS width, im.height AS height, " +
            		"         e.artist AS artist, im.createdDt as createdDt, e.description ");
        }
        
        builder.append("FROM Images im ");
        
        int categoryId = command.getCategoryId();
        if (user.isSuperAdmin()) {
            if (categoryId == SearchImageCommand.CATEGORY_NO_CATEGORY) {
                builder.append("LEFT OUTER JOIN im.categories c ");
            } else if (categoryId != SearchImageCommand.CATEGORY_ALL) {
                builder.append("INNER JOIN im.categories c ");
            }
        } else if (categoryId == SearchImageCommand.CATEGORY_NO_CATEGORY || categoryId == SearchImageCommand.CATEGORY_ALL) {
            if (user.isDefaultUser()) {
                builder.append("INNER JOIN im.categories c ");
            } else {
                builder.append("LEFT OUTER JOIN im.categories c ");
            }
        } else {
            builder.append("INNER JOIN im.categories c ");
        }
        
        long keywordId = command.getKeywordId();
        if (keywordId != SearchImageCommand.KEYWORD_ALL) {
            builder.append("INNER JOIN im.keywords k ");
        }
        
        builder.append(", Exif e WHERE e.imageId = im.id AND e.type = :changedType ");
        
        String artist = command.getArtist();
        if (artist != null) {
            builder.append("AND lower(e.artist) = :artist ");
        }
        
        Short status = null;
        switch (command.getShow()) {
            case SearchImageCommand.SHOW_ERASED:
                builder.append("AND im.status = :status ");
                status = Images.STATUS_ARCHIVED;
                break;
            case SearchImageCommand.SHOW_NEW:
                builder.append("AND im.createdDt >= current_date() ");
            default:
                builder.append("AND im.status = :status ");
                status = Images.STATUS_ACTIVE;
        }
        
        
        
        if (user.isSuperAdmin()) {
            if (categoryId == SearchImageCommand.CATEGORY_NO_CATEGORY) {
                builder.append("AND im.categories IS EMPTY ");
            } else if (categoryId != SearchImageCommand.CATEGORY_ALL) {
                builder.append("AND c.id = :categoryId ");
            }
        } else if (categoryId == SearchImageCommand.CATEGORY_ALL) {
            builder.append("AND (");
            
            if (!categoryIds.isEmpty()) {
                builder.append("c.id IN (:categoryIds) ");
            }
            
            if (!categoryIds.isEmpty() && !user.isDefaultUser()) {
                builder.append("OR ");
            }
            
            if (!user.isDefaultUser()) {
                builder.append("im.usersId = :usersId ");
            }
            
            builder.append(") ");
        } else if (categoryId == SearchImageCommand.CATEGORY_NO_CATEGORY) {
            builder.append("AND im.categories IS EMPTY AND im.usersId = :usersId ");
        } else {
            builder.append("AND c.id = :categoryId ");
        }
        
        if (keywordId != SearchImageCommand.KEYWORD_ALL) {
            builder.append("AND k.id = :keywordId ");
        }
        
        String freetext = command.getFreetext();
        if (freetext != null) {
            freetext = LIKE_SPECIAL_PATTERN.matcher(freetext).replaceAll("|$1");
            builder.append("AND (lower(im.imageNm) LIKE :freetext ESCAPE '|' OR lower(e.description) LIKE :freetext ESCAPE '|') ");
        }
        
        Date licenseDt = command.getLicenseDate();
        Date licenseEndDt = command.getLicenseEndDate();
        if (licenseDt != null && licenseEndDt != null) {
            Date min = Utils.min(licenseDt, licenseEndDt);
            Date max = Utils.max(licenseDt, licenseEndDt);
            
            licenseDt = min;
            licenseEndDt = max;
            
            builder.append("AND im.licenseDt <= :licenseDt AND im.licenseEndDt >= :licenseEndDt ");
        } else if (licenseDt != null) {
            builder.append("AND im.licenseDt >= :licenseDt ");
        } else if (licenseEndDt != null) {
            builder.append("AND im.licenseEndDt <= :licenseEndDt ");
        }
        
        Date activeDt = command.getActiveDate();
        Date activeEndDt = command.getActiveEndDate();
        if (activeDt != null && activeEndDt != null) { 
            Date min = Utils.min(activeDt, activeEndDt);
            Date max = Utils.max(activeDt, activeEndDt);
            
            activeDt = min;
            activeEndDt = max;
            
            builder.append("AND im.publishDt <= :activeDt AND im.publishEndDt >= :activeEndDt ");
        } else if (activeDt != null) {
            builder.append("AND im.publishDt >= :activeDt ");
        } else if (activeEndDt != null) {
            builder.append("AND im.publishEndDt <= :activeEndDt ");
        }
        
        if (!count) {
            builder.append("ORDER BY ");
            switch (command.getSortBy()) {
                case SearchImageCommand.SORT_BY_FREETEXT:
                    builder.append("im.imageNm, e.description ");
                    break;
                case SearchImageCommand.SORT_BY_ENTRY_DATE:
                    builder.append("im.createdDt DESC ");
                    break;
                default:
                    builder.append("e.artist ");
                    break;
            }
        }
        
        Query query = factory.getCurrentSession()
                .createQuery(builder.toString())
                .setShort("changedType", Exif.TYPE_CHANGED);
        
        if (artist != null) {
            query.setString("artist", artist.toLowerCase());
        }
        if (status != null) {
            query.setShort("status", status);
        }
        
        if (user.isSuperAdmin()) {
            if (categoryId != SearchImageCommand.CATEGORY_NO_CATEGORY && categoryId != SearchImageCommand.CATEGORY_ALL) {
                query.setInteger("categoryId", categoryId);
            }
        } else if (categoryId == SearchImageCommand.CATEGORY_ALL) {
            if (!categoryIds.isEmpty()) {
                query.setParameterList("categoryIds", categoryIds);
            }
            
            if (!user.isDefaultUser()) {
                query.setInteger("usersId", user.getId());
            }
        } else if (categoryId == SearchImageCommand.CATEGORY_NO_CATEGORY) {
            query.setInteger("usersId", user.getId());
        } else {
            query.setInteger("categoryId", categoryId);
        }
        
        if (keywordId != SearchImageCommand.KEYWORD_ALL) {
            query.setLong("keywordId", keywordId);
        }
        if (freetext != null) {
            query.setString("freetext", "%" + freetext.toLowerCase() + "%");
        }
        if (licenseDt != null) {
            query.setDate("licenseDt", licenseDt);
        }
        if (licenseEndDt != null) {
            query.setDate("licenseEndDt", licenseEndDt);
        }
        if (activeDt != null) {
            query.setDate("activeDt", activeDt);
        }
        if (activeEndDt != null) {
            query.setDate("activeEndDt", activeEndDt);
        }
        
        return query;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public int searchImagesCount(SearchImageCommand command, List<Integer> categoryIds, User user) {
        if (user.isDefaultUser() && categoryIds.isEmpty()) {
            return 0;
        }
        
        long count = (Long) buildSearchImagesQuery(command, true, categoryIds, user)
                .uniqueResult();
        
        return (int) count;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Images> searchImages(SearchImageCommand command, Pagination pag, List<Integer> categoryIds, User user) {
        
        if (user.isDefaultUser() && categoryIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        
        List<Map<String, Object>> result = buildSearchImagesQuery(command, false, categoryIds, user)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(pag.getStartPosition())
                .setMaxResults(pag.getPageSize())
                .list();

        List<Images> images = new ArrayList<Images>(result.size());
        for (Map<String, Object> row : result) {
            Images image = new Images();
            image.setId((Long) row.get("id"));
            image.setImageNm((String) row.get("imageNm"));
            image.setWidth((Integer) row.get("width"));
            image.setHeight((Integer) row.get("height"));
            image.setArtist((String) row.get("artist"));

            images.add(image);
        }

        if (!images.isEmpty()) {
            setUsedInImcms(images);
        }

        return images;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findImageCategories(long imageId) {

        return factory.getCurrentSession()
                .createQuery(
                "SELECT c.id AS id, c.name AS name FROM ImageCategories ic INNER JOIN ic.category c " +
                "WHERE ic.imageId = :imageId AND c.type.imageArchive IS TRUE ORDER BY c.name ")
                .setLong("imageId", imageId)
                .setResultTransformer(Transformers.aliasToBean(Categories.class))
                .list();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findAvailableImageCategories(long imageId, User user) {

        Session session = factory.getCurrentSession();

        if (user.isSuperAdmin()) {
            return session.getNamedQuery("availableImageCategoriesAdmin")
                    .setLong("imageId", imageId)
                    .setResultTransformer(Transformers.aliasToBean(Categories.class))
                    .list();
        }

        List<Integer> roleIds = UserService.getRoleIdsWithPermission(user, RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);
        if (roleIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return session.getNamedQuery("availableImageCategories")
                .setLong("imageId", imageId)
                .setParameterList("roleIds", roleIds)
                .setResultTransformer(Transformers.aliasToBean(Categories.class))
                .list();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public boolean canUseCategories(User user, List<Integer> categoryIds) {

        if (user.isSuperAdmin()) {
            return true;
        }

        List<Integer> roleIds = UserService.getRoleIdsWithPermission(user, RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION);
        if (roleIds.isEmpty()) {
            return false;
        }

        long count = (Long) factory.getCurrentSession().createQuery(
                "SELECT count(DISTINCT cr.categoryId) FROM CategoryRoles cr " +
                "WHERE cr.roleId IN (:roleIds) AND cr.categoryId IN (:categoryIds) ")
                .setParameterList("roleIds", roleIds)
                .setParameterList("categoryIds", categoryIds)
                .uniqueResult();

        return count == categoryIds.size();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<String> findAvailableKeywords(long imageId) {

        return factory.getCurrentSession()
                .getNamedQuery("availableKeywords")
                .setLong("imageId", imageId)
                .list();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<String> findImageKeywords(long imageId) {

        return factory.getCurrentSession()
                .createQuery(
                "SELECT k.keywordNm FROM ImageKeywords ik INNER JOIN ik.keyword k " +
                "WHERE ik.imageId = :imageId ORDER BY k.keywordNm")
                .setLong("imageId", imageId)
                .list();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Keywords> findKeywords() {

        return factory.getCurrentSession()
                .getNamedQuery("keywordsUsedByImages")
                .setResultTransformer(Transformers.aliasToBean(Keywords.class))
                .list();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public boolean canUseImage(User user, long imageId) {
        if (user.isSuperAdmin()) {
            return true;
        }

        Session session = factory.getCurrentSession();
        
        Integer usersId = (Integer) session.createQuery("SELECT im.usersId FROM Images im WHERE im.id = :imageId")
                .setLong("imageId", imageId)
                .uniqueResult();
        if (usersId == null || usersId == user.getId()) {
            return true;
        }

        List<Integer> roleIds = UserService.getRoleIdsWithPermission(user, RoleDomainObject.USE_IMAGES_IN_ARCHIVE_PERMISSION);
        if (roleIds.isEmpty()) {
            return false;
        }

        long count = (Long) session.createQuery(
                "SELECT count(ic.imageId) FROM CategoryRoles cr, ImageCategories ic " +
                "WHERE cr.roleId IN (:roleIds) AND cr.categoryId = ic.categoryId AND ic.imageId = :imageId")
                .setParameterList("roleIds", roleIds)
                .setLong("imageId", imageId)
                .uniqueResult();

        return count > 0L;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public String findImageName(long imageId) {

        return (String) factory.getCurrentSession()
                .createQuery("SELECT im.imageNm FROM Images im WHERE im.id = :imageId")
                .setLong("imageId", imageId)
                .uniqueResult();
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public void setImageMetaIds(Images image) {

        List<Integer> metaIds = factory.getCurrentSession()
                .createSQLQuery(
                "SELECT DISTINCT i.meta_id FROM images i WHERE i.archive_image_id = :imageId ORDER BY i.meta_id")
                .setLong("imageId", image.getId())
                .list();

        image.setMetaIds(metaIds);
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public void setUsedInImcms(List<Images> images) {
        
        Map<Long, Images> imageMap = new HashMap<Long, Images>(images.size());
        for (Images image : images) {
            imageMap.put(image.getId(), image);
        }

        List<BigInteger> result = factory.getCurrentSession()
                .createSQLQuery(
                "SELECT DISTINCT i.archive_image_id FROM imcms_text_doc_images i WHERE i.archive_image_id IN (:imageIds)")
                .setParameterList("imageIds", imageMap.keySet())
                .list();

        for (BigInteger id : result) {
            imageMap.get(id.longValue()).setUsedInImcms(true);
        }
    }
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public void setImagesMetaIds(List<Images> images) {
        
        Map<Long, Images> imageMap = new HashMap<Long, Images>(images.size());
        for (Images image : images) {
            imageMap.put(image.getId(), image);
        }

        List<Object[]> result = factory.getCurrentSession()
                .createSQLQuery(
                "SELECT DISTINCT i.archive_image_id, i.doc_id FROM imcms_text_doc_images i WHERE i.archive_image_id IN (:imageIds) " +
                "ORDER BY i.archive_image_id, i.doc_id")
                .setParameterList("imageIds", imageMap.keySet())
                .list();

        List<Integer> currentMetaIds = null;
        Long currentImageId = null;
        for (Object[] tuple : result) {
            Long imageId = ((BigInteger) tuple[0]).longValue();
            Integer metaId = (Integer) tuple[1];

            if (!imageId.equals(currentImageId)) {
                currentMetaIds = new ArrayList<Integer>();
                imageMap.get(imageId).setMetaIds(currentMetaIds);
            }

            currentMetaIds.add(metaId);
        }
    }
    
    public void createKeyword(final String keyword) {
        Session session = factory.getCurrentSession();

        long count = (Long) session.createQuery(
                "SELECT COUNT(k.id) FROM Keywords k WHERE k.keywordNm = :keyword")
                .setString("keyword", keyword)
                .uniqueResult();

        if (count == 0L) {
            Keywords k = new Keywords();
            k.setKeywordNm(keyword);
            session.persist(k);
        }
        session.flush();
    }
}