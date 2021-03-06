//package com.imcode.imcms.addon.imagearchive.controller;
//
//import com.imcode.imcms.addon.imagearchive.command.ChangeImageDataCommand;
//import com.imcode.imcms.addon.imagearchive.command.ExternalFilesCommand;
//import com.imcode.imcms.addon.imagearchive.command.ExternalFilesSaveImageCommand;
//import com.imcode.imcms.addon.imagearchive.dto.LibrariesDto;
//import com.imcode.imcms.addon.imagearchive.dto.LibraryEntryDto;
//import com.imcode.imcms.addon.imagearchive.entity.Categories;
//import com.imcode.imcms.addon.imagearchive.entity.Images;
//import com.imcode.imcms.addon.imagearchive.json.UploadResponse;
//import com.imcode.imcms.addon.imagearchive.service.Facade;
//import com.imcode.imcms.addon.imagearchive.service.file.LibrarySort;
//import com.imcode.imcms.addon.imagearchive.tag.func.Functions;
//import com.imcode.imcms.addon.imagearchive.util.ArchiveSession;
//import com.imcode.imcms.addon.imagearchive.util.Utils;
//import com.imcode.imcms.addon.imagearchive.validator.ChangeImageDataValidator;
//import com.imcode.imcms.addon.imagearchive.validator.ExternalFilesValidator;
//import com.imcode.imcms.api.ContentManagementSystem;
//import com.imcode.imcms.api.User;
//import imcode.util.image.Format;
//import imcode.util.image.ImageInfo;
//import imcode.util.image.ImageOp;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.collections.Predicate;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
//import org.springframework.validation.ValidationUtils;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class ExternalFilesController {
//    private static final Log log = LogFactory.getLog(ExternalFilesController.class);
//
//    private static final String LIBRARY_KEY = Utils.makeKey(ExternalFilesController.class, "library");
//    private static final String SORT_KEY = Utils.makeKey(ExternalFilesController.class, "sortBy");
//
//    private static final String IMAGE_KEY = Utils.makeKey(ExternalFilesController.class, "image");
//    private static final String KEYWORDS_KEY = Utils.makeKey(ExternalFilesController.class, "keywords");
//    private static final String IMAGE_KEYWORDS_KEY = Utils.makeKey(ExternalFilesController.class, "imageKeywords");
//
//    @Autowired
//    private Facade facade;
//
//
//    @RequestMapping("/archive/external-files")
//    public ModelAndView indexHandler(
//            HttpServletRequest request) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        if (user.isDefaultUser()) {
//            return new ModelAndView("redirect:/web/archive/");
//        }
//
//        ModelAndView mav = new ModelAndView("image_archive/pages/external_files/external_files");
//
//        List<LibrariesDto> libraries = facade.getLibraryService().findLibraries(user);
//        final List<File> firstLevelLibraries = facade.getFileService().listFirstLevelLibraryFolders();
//        CollectionUtils.filter(libraries, new Predicate() {
//            public boolean evaluate(Object o) {
//                LibrariesDto lib = (LibrariesDto) o;
//                return firstLevelLibraries.contains(new File(lib.getFilepath(), lib.getFolderNm()));
//            }
//        });
//        List<LibrariesDto> allLibraries = facade.getLibraryService().findLibraries(user);
//
//        LibrariesDto library = getLibrary(session, user);
//        LibrarySort sortBy = getSortBy(session);
//        List<LibraryEntryDto> libraryEntries = facade.getFileService().listLibraryEntries(library, sortBy);
//
//        mav.addObject("currentLibrary", library);
//        mav.addObject("libraries", libraries);
//        mav.addObject("allLibraries", allLibraries);
//        mav.addObject("libraryEntries", libraryEntries);
//        mav.addObject("sortBy", sortBy);
//        mav.addObject("externalFiles", new ExternalFilesCommand());
//
//        return mav;
//    }
//
//    /* Switches current library */
//    @RequestMapping("/archive/external-files/library")
//    public String changeLibraryHandler(
//            @RequestParam(required = false) Integer id,
//            HttpServletRequest request) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        if (user.isDefaultUser()) {
//            return "redirect:/web/archive/";
//        }
//
//        if (id != null) {
//            LibrariesDto library;
//            if (id == LibrariesDto.USER_LIBRARY_ID) {
//                library = LibrariesDto.userLibrary(user);
//            } else {
//                library = facade.getLibraryService().findLibraryById(user, id);
//            }
//            session.put(LIBRARY_KEY, library);
//        }
//
//        return "redirect:/web/archive/external-files";
//    }
//
//    /* Changes sorting. Called each time sorting order is changed in library entry table(ajax) */
//    @RequestMapping("/archive/external-files/sort")
//    public String changeSortByHandler(
//            @RequestParam(required = false) String sortBy,
//            HttpServletRequest request) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//
//        LibrarySort sort;
//        if (sortBy != null && (sort = LibrarySort.findByName(sortBy)) != null) {
//            session.put(SORT_KEY, sort);
//        }
//
//        return "redirect:/web/archive/external-files";
//    }
//
//    /* Adds images from external files to archive if they are not there already(by file name, size criteria).
//     * Erazed files from archive */
//    @RequestMapping("/archive/external-files/process")
//    public ModelAndView processHandler(
//            @ModelAttribute("externalFiles") ExternalFilesCommand command,
//            HttpServletRequest request) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        if (user.isDefaultUser()) {
//            return new ModelAndView("redirect:/web/archive/");
//        }
//
//        LibrariesDto library = getLibrary(session, user);
//        if (library == null) {
//            return new ModelAndView("redirect:/web/archive/external-files");
//        }
//
//        if (command.getActivate() != null) {
//            String[] fileNames = command.getFileNames();
//            if (!library.isCanUse() || fileNames == null || fileNames.length == 0) {
//                return new ModelAndView("redirect:/web/archive/external-files");
//            }
//
//            ModelAndView mav = new ModelAndView("image_archive/pages/external_files/external_files");
//            mav.addObject("activate", true);
//
//            if (fileNames.length == 1) {
//                Images image;
//                boolean alreadyInArchive =
//                        Utils.isInArchive(facade.getFileService().getImageFileFromLibrary(library, fileNames[0]), facade).size() > 0;
//                if (alreadyInArchive || (image = activateImage(library, fileNames[0], user)) == null) {
//                    ModelAndView tmp = new ModelAndView("redirect:/web/archive/external-files");
//                    tmp.addObject("activateError", true);
//                    if (alreadyInArchive) {
//                        tmp.addObject("alreadyInArchive", true);
//                    }
//                    return tmp;
//                }
//                session.put(IMAGE_KEY, image);
//
//                ChangeImageDataCommand changeData = new ChangeImageDataCommand();
//                changeData.fromImage(image);
//                mav.addObject("changeData", changeData);
//                mav.addObject("categories", facade.getImageService().findAvailableImageCategories(image.getId(), user));
//
//                List<String> keywords = facade.getImageService().findAvailableKeywords(image.getId());
//                List<String> imageKeywords = facade.getImageService().findImageKeywords(image.getId());
//                session.put(KEYWORDS_KEY, keywords);
//                session.put(IMAGE_KEYWORDS_KEY, imageKeywords);
//
//                mav.addObject("keywords", keywords);
//                mav.addObject("imageKeywords", imageKeywords);
//                mav.addObject("image", image);
//                mav.addObject("format", Format.findFormat(image.getFormat()));
//            } else {
//                List<Object[]> tuples = new ArrayList<Object[]>(fileNames.length);
//                boolean generalActivationErrorOccured = false;
//                boolean oneOfTheImageIsAlreadyInArchive = false;
//
//                for (String fileName : fileNames) {
//                    try {
//                        File imageFile = facade.getFileService().getImageFileFromLibrary(library, fileName);
//                        ImageInfo imageInfo;
//                        boolean alreadyInArchive = Utils.isInArchive(imageFile, facade).size() > 0;
//                        oneOfTheImageIsAlreadyInArchive |= alreadyInArchive;
//                        if (imageFile == null || (imageInfo = ImageOp.getImageInfo(imageFile)) == null
//                                || alreadyInArchive) {
//                            generalActivationErrorOccured |= true;
//                            continue;
//                        }
//
//                        tuples.add(new Object[]{imageFile, imageInfo, fileName});
//                    } catch (Exception ex) {
//                        log.warn(ex.getMessage(), ex);
//                    }
//                }
//
//                if (!tuples.isEmpty()) {
//                    facade.getImageService().createImages(tuples, user);
//                }
//
//                if (generalActivationErrorOccured) {
//                    ModelAndView tmp = new ModelAndView("redirect:/web/archive/external-files");
//                    tmp.addObject("activateError", true);
//                    if (oneOfTheImageIsAlreadyInArchive) {
//                        tmp.addObject("alreadyInArchive", true);
//                    }
//
//                    return tmp;
//                }
//
//                return new ModelAndView("redirect:/web/archive/external-files");
//            }
//
//            return mav;
//        } else if (command.getErase() != null && library.isUserLibrary()) {
//            String[] fileNames = command.getFileNames();
//            if (library.isCanChange() && fileNames != null) {
//                for (String fileName : fileNames) {
//                    facade.getFileService().deleteFileFromLibrary(library, fileName);
//                }
//            }
//        }
//
//        return new ModelAndView("redirect:/web/archive/external-files");
//    }
//
//    /* Called by uploadify for multi-file upload(images, zips or both).
//     * Stores images and images from zip archives
//     * One request, one file.
//     * User is only allowed to upload images to personal folder
//     * Single image upload redirects to a form where attributes can be changed, when multiple files are uploaded
//     * no form is shown*/
//    @RequestMapping("/archive/external-files/upload")
//    public void uploadHandler(ExternalFilesCommand externalFilesUpload,
//                              BindingResult result,
//                              HttpServletRequest request,
//                              HttpServletResponse response) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//        UploadResponse status = new UploadResponse();
//        String contextPath = request.getContextPath();
//
//        if (user.isDefaultUser()) {
//            status.setRedirect(request.getContextPath() + "/web/archive");
//            Utils.writeJSON(status, response);
//            return;
//        }
//
//        LibrariesDto library = LibrariesDto.userLibrary(user);
//        session.put(LIBRARY_KEY, library);
//        if (library == null || !library.isCanChange()) {
//            status.setRedirect(contextPath + "/web/archive/external-files");
//            Utils.writeJSON(status, response);
//            return;
//        }
//
//        ExternalFilesValidator validator = new ExternalFilesValidator(facade);
//        ValidationUtils.invokeValidator(validator, externalFilesUpload, result);
//
//        if (!result.hasErrors()) {
//            boolean success;
//            if (!validator.isZipFile()) {
//                success = facade.getFileService().storeImageToLibrary(library, validator.getTempFile(), validator.getFileName());
//            } else {
//                success = facade.getFileService().storeZipToLibrary(library, validator.getTempFile());
//            }
//
//            if (!success) {
//                result.rejectValue("file", "archive.externalFiles.fileError");
//            }
//        }
//
//        if (validator.getTempFile() != null) {
//            validator.getTempFile().delete();
//        }
//
//        List<String> errors = new ArrayList<String>();
//        if (result.hasErrors()) {
//            for (FieldError error : result.getFieldErrors()) {
//                errors.add(facade.getCommonService().getMessage(error.getCode(), request.getLocale(), error.getArguments()));
//            }
//            status.setErrors(errors);
//        } else {
//            status.setRedirectOnAllComplete(contextPath + "/web/archive/external-files");
//        }
//
//        Utils.writeJSON(status, response);
//    }
//
//    private Images activateImage(LibrariesDto library, String fileName, User user) {
//        File imageFile = facade.getFileService().getImageFileFromLibrary(library, fileName);
//        ImageInfo imageInfo;
//        if (imageFile == null || (imageInfo = ImageOp.getImageInfo(imageFile)) == null) {
//            return null;
//        }
//
//        try {
//            return facade.getImageService().createImage(imageFile, imageInfo, fileName, user);
//        } catch (Exception ex) {
//            log.warn(ex.getMessage(), ex);
//        }
//
//        return null;
//    }
//
//    /* Used when adding images to archive(adding attributes, rotation etc) */
//    @RequestMapping("/archive/external-files/change")
//    public ModelAndView changeHandler(
//            @ModelAttribute("changeData") ChangeImageDataCommand changeData,
//            BindingResult result,
//            ExternalFilesSaveImageCommand saveCommand,
//            HttpServletRequest request) {
//        ArchiveSession session = ArchiveSession.getSession(request);
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        Images image = (Images) session.get(IMAGE_KEY);
//        if (user.isDefaultUser()) {
//            return new ModelAndView("redirect:/web/archive/");
//        } else if (image == null) {
//            return new ModelAndView("redirect:/web/archive/external-files");
//        }
//
//        if (saveCommand.getCancel() != null) {
//            facade.getImageService().deleteImage(image.getId());
//            session.remove(IMAGE_KEY);
//            session.remove(KEYWORDS_KEY);
//            session.remove(IMAGE_KEYWORDS_KEY);
//
//            return new ModelAndView("redirect:/web/archive/external-files");
//        }
//
//        ChangeImageDataValidator validator = new ChangeImageDataValidator(facade, user);
//        ValidationUtils.invokeValidator(validator, changeData, result);
//
//        ModelAndView mav = new ModelAndView("image_archive/pages/external_files/external_files");
//        mav.addObject("activate", true);
//        mav.addObject("image", image);
//
//        List<String> keywords = changeData.getKeywordNames();
//        List<String> imageKeywords = changeData.getImageKeywordNames();
//        session.put(KEYWORDS_KEY, keywords);
//        session.put(IMAGE_KEYWORDS_KEY, imageKeywords);
//        mav.addObject("keywords", keywords);
//        mav.addObject("imageKeywords", imageKeywords);
//
//        if (saveCommand.getRotateLeft() != null || saveCommand.getRotateRight() != null) {
//            if (saveCommand.getRotateLeft() != null) {
//                facade.getFileService().rotateImage(image.getId(), -90, false);
//            } else {
//                facade.getFileService().rotateImage(image.getId(), 90, false);
//            }
//
//            mav.addObject("categories", facade.getImageService().findAvailableImageCategories(image.getId(), user));
//            mav.addObject("imageCategories", facade.getImageService().findImageCategories(image.getId()));
//
//            return mav;
//        } else if (result.hasErrors()) {
//            mav.addObject("categories", facade.getImageService().findAvailableImageCategories(image.getId(), user));
//            mav.addObject("imageCategories", facade.getImageService().findImageCategories(image.getId()));
//
//            return mav;
//        }
//
//        changeData.toImage(image);
//
//        try {
//            facade.getImageService().updateData(image, changeData.getCategoryIds(), imageKeywords);
//
//            if (saveCommand.getSaveActivate() != null) {
//                session.remove(IMAGE_KEY);
//                session.remove(KEYWORDS_KEY);
//                session.remove(IMAGE_KEYWORDS_KEY);
//
//                return new ModelAndView("redirect:/web/archive/external-files");
//            } else if (saveCommand.getSaveUse() != null) {
//                session.remove(IMAGE_KEY);
//                session.remove(KEYWORDS_KEY);
//                session.remove(IMAGE_KEYWORDS_KEY);
//
//                return new ModelAndView("redirect:/web/archive/use?id=" + image.getId());
//            } else if (saveCommand.getSaveImageCard() != null) {
//                session.remove(IMAGE_KEY);
//                session.remove(KEYWORDS_KEY);
//                session.remove(IMAGE_KEYWORDS_KEY);
//
//                return new ModelAndView("redirect:/web/archive/image/" + image.getId());
//            }
//
//            mav.addObject("categories", facade.getImageService().findAvailableImageCategories(image.getId(), user));
//            mav.addObject("imageCategories", facade.getImageService().findImageCategories(image.getId()));
//        } catch (Exception ex) {
//            log.fatal(ex.getMessage(), ex);
//
//            return new ModelAndView("redirect:/web/archive/external-files");
//        }
//
//        return mav;
//    }
//
//    @RequestMapping("/archive/external-files/preview")
//    public ModelAndView previewHandler(
//            @RequestParam(required = false) Integer id,
//            @RequestParam(required = false) String name,
//            HttpServletRequest request) {
//
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        LibrariesDto library = null;
//        ImageInfo imageInfo = null;
//        if (!user.isDefaultUser() && id != null) {
//            if (id == LibrariesDto.USER_LIBRARY_ID) {
//                library = LibrariesDto.userLibrary(user);
//            } else {
//                library = facade.getLibraryService().findLibraryById(user, id);
//            }
//
//            name = StringUtils.trimToNull(name);
//
//            if (name != null) {
//                File imageFile = facade.getFileService().getImageFileFromLibrary(library, name);
//                if (imageFile != null) {
//                    imageInfo = ImageOp.getImageInfo(imageFile);
//                }
//            }
//        }
//
//        Map<String, Object> model = new HashMap<String, Object>();
//        model.put("user", user);
//        model.put("library", library);
//        model.put("imageInfo", imageInfo);
//        model.put("name", name);
//
//        return new ModelAndView("image_archive/pages/external_files/preview", model);
//    }
//
//    /* Used by small tooltip when moving cursor over filenames in library entry list */
//    @RequestMapping("/archive/external-files/preview-tooltip")
//    public ModelAndView previewTooltipHandler(
//            @RequestParam(required = false) Integer id,
//            @RequestParam(required = false) String name,
//            HttpServletRequest request) {
//
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        LibrariesDto library = null;
//        ImageInfo imageInfo = null;
//        Integer fileSize = null;
//        String categoryNamesNeededToUse = "";
//        boolean noCategories = false;
//        if (!user.isDefaultUser() && id != null) {
//            if (id == LibrariesDto.USER_LIBRARY_ID) {
//                library = LibrariesDto.userLibrary(user);
//            } else {
//                library = facade.getLibraryService().findLibraryById(user, id);
//            }
//
//            name = StringUtils.trimToNull(name);
//
//            if (name != null) {
//                File imageFile = facade.getFileService().getImageFileFromLibrary(library, name);
//                List<Images> images = Utils.isInArchive(imageFile, facade);
//                if (images.size() > 0) {
//                    for (Images image : images) {
//                        /* we only need one case of missing categories */
//                        if (!noCategories) {
//                            noCategories = image.getCategories() == null || image.getCategories().size() == 0;
//                        }
//
//                        List<String> categoryNames = new ArrayList<String>();
//                        for (Categories cat : Utils.getCategoriesRequiredToUse(image, facade, user)) {
//                            categoryNames.add(cat.getName());
//                        }
//
//                        categoryNamesNeededToUse += Functions.join(categoryNames, ",");
//                    }
//                }
//
//                if (imageFile != null) {
//                    fileSize = (int) imageFile.length();
//                    imageInfo = ImageOp.getImageInfo(imageFile);
//                }
//            }
//        }
//
//        Map<String, Object> model = new HashMap<String, Object>();
//        model.put("user", user);
//        model.put("library", library);
//        model.put("imageInfo", imageInfo);
//        model.put("name", name);
//        model.put("size", fileSize);
//        model.put("categoryNamesNeededToUse", categoryNamesNeededToUse);
//        model.put("noCategories", noCategories);
//
//        return new ModelAndView("image_archive/pages/external_files/preview-tooltip", model);
//    }
//
//    @RequestMapping("/archive/external-files/image")
//    public String imageHandler(
//            @RequestParam(required = false) Integer id,
//            @RequestParam(required = false) String name,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//
//        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
//        User user = cms.getCurrentUser();
//
//        if (user.isDefaultUser() || id == null) {
//            Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
//            return null;
//        }
//
//        LibrariesDto library;
//        if (id == LibrariesDto.USER_LIBRARY_ID) {
//            library = LibrariesDto.userLibrary(user);
//        } else {
//            library = facade.getLibraryService().findLibraryById(user, id);
//        }
//
//        if (library == null || !library.isCanUse()) {
//            Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
//            return null;
//        }
//
//        File imageFile = facade.getFileService().getImageFileFromLibrary(library, name);
//        if (imageFile == null) {
//            Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
//            return null;
//        }
//
//        try {
//            byte[] data = new ImageOp().input(imageFile)
//                    .outputFormat(Format.JPEG)
//                    .processToByteArray();
//
//            if (data == null) {
//                Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
//
//                return null;
//            }
//
//            Utils.addNoCacheHeaders(response);
//            response.setContentLength(data.length);
//            response.setContentType("image/jpeg");
//
//            OutputStream output = null;
//            try {
//                output = new BufferedOutputStream(response.getOutputStream());
//                IOUtils.copy(new ByteArrayInputStream(data), output);
//                output.flush();
//            } catch (Exception ex) {
//            } finally {
//                IOUtils.closeQuietly(output);
//            }
//        } catch (Exception ex) {
//            Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
//
//            return null;
//        }
//
//        return null;
//    }
//
//    private LibrariesDto getLibrary(ArchiveSession session, User user) {
//        LibrariesDto library = (LibrariesDto) session.get(LIBRARY_KEY);
//        if (library != null && !library.isUserLibrary()) {
//            library = facade.getLibraryService().findLibraryById(user, library.getId());
//        }
//        if (library == null) {
//            library = LibrariesDto.userLibrary(user);
//            session.put(LIBRARY_KEY, library);
//        }
//
//        return library;
//    }
//
//    private static LibrarySort getSortBy(ArchiveSession session) {
//        LibrarySort sortBy = (LibrarySort) session.get(SORT_KEY);
//        if (sortBy == null) {
//            sortBy = LibrarySort.FILENAME;
//            sortBy.setDirection(LibrarySort.DIRECTION.ASC);
//            session.put(SORT_KEY, sortBy);
//        }
//
//        return sortBy;
//    }
//}
