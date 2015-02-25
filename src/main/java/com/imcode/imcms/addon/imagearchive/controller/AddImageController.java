package com.imcode.imcms.addon.imagearchive.controller;

import com.imcode.imcms.addon.imagearchive.command.AddImageUploadCommand;
import com.imcode.imcms.addon.imagearchive.command.ChangeImageDataCommand;
import com.imcode.imcms.addon.imagearchive.entity.Images;
import com.imcode.imcms.addon.imagearchive.json.UploadResponse;
import com.imcode.imcms.addon.imagearchive.service.Facade;
import com.imcode.imcms.addon.imagearchive.util.ArchiveSession;
import com.imcode.imcms.addon.imagearchive.util.Utils;
import com.imcode.imcms.addon.imagearchive.validator.ChangeImageDataValidator;
import com.imcode.imcms.addon.imagearchive.validator.ImageUploadValidator;
import com.imcode.imcms.api.ContentManagementSystem;
import com.imcode.imcms.api.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AddImageController {
    private static final Log log = LogFactory.getLog(AddImageController.class);
    
    @Autowired
    private Facade facade;
    
    
    @SuppressWarnings("unchecked")
    @RequestMapping("/archive/add-image")
    public ModelAndView indexHandler(HttpServletRequest request, HttpServletResponse response) {
        ArchiveSession session = ArchiveSession.getSession(request);
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        
        if (user.isDefaultUser()) {
            return new ModelAndView("redirect:/web/archive/");
        }
        
        ModelAndView mav = new ModelAndView("image_archive/pages/add_image");

        mav.addObject("upload", new AddImageUploadCommand());
        mav.addObject("keywords", facade.getImageService().findKeywordNames(null));
        mav.addObject("categories", facade.getImageService().findAvailableCategories(user, cms));

        return mav;
    }

    @RequestMapping(value = "/archive/add-image/multi-upload-form", method = RequestMethod.GET)
    public String multiFileDataFromHandler(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Map<String, Object> model) throws IOException {
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();

        if (user.isDefaultUser()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        model.put("keywords", facade.getImageService().findKeywordNames(null));
        model.put("categories", facade.getImageService().findAvailableCategories(user, cms));

        return "image_archive/pages/fragments/multi_file_data";
    }

    /* Called by uploadify for multi-file upload(images, zips or both).
     * Stores images and images from zip archives
     * One request, one file */
    @RequestMapping("/archive/add-image/upload")
    public void uploadHandler(
            @ModelAttribute("upload") AddImageUploadCommand command,
            BindingResult result,
            @ModelAttribute("changeData") ChangeImageDataCommand changeData,
            BindingResult dataResult,
            HttpServletRequest request, 
            HttpServletResponse response) {
        ArchiveSession session = ArchiveSession.getSession(request);
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        UploadResponse uploadResponse = new UploadResponse();
        String contextPath = request.getContextPath();
        
        if (user.isDefaultUser()) {
            uploadResponse.setRedirect(contextPath + "/web/archive");
            Utils.writeJSON(uploadResponse, response);
            return;
        }
        
        ImageUploadValidator validator = new ImageUploadValidator(facade);
        ValidationUtils.invokeValidator(validator, command.getFile(), result);

        if((command.getFileCount() > 0 || validator.isZipFile()) && changeData.isSubmitted()) {
            ChangeImageDataValidator dataValidator = new ChangeImageDataValidator(facade, user, cms);
            ValidationUtils.invokeValidator(dataValidator, changeData, dataResult);
        }

        if (!result.hasErrors() && !dataResult.hasErrors()) {
            try {
                if(validator.isZipFile()) {
                    List<Images> images = facade.getImageService().createImagesFromZip(validator.getTempFile(), user);
                    if(changeData.isSubmitted()) {
                        for(Images image: images) {
                            if(image != null) {
                                changeData.setImageNm(image.getImageNm());
                                changeData.toImage(image);
                                facade.getImageService().updateData(image, changeData.getCategoryIds(), changeData.getImageKeywordNames());
                            }
                        }
                    }
                    
                } else {
                    Images image;
                    if(command.getFileCount() > 1) {
                        image = facade.getImageService().createImageActivated(validator.getTempFile(),
                            validator.getImageInfo(), validator.getImageName(), user);
                    } else {
                        image = facade.getImageService().createImage(validator.getTempFile(),
                            validator.getImageInfo(), validator.getImageName(), user);

                        if(changeData.isSubmitted()) {
                            if(image != null) {
                                changeData.toImage(image);
                                facade.getImageService().updateData(image, changeData.getCategoryIds(), changeData.getImageKeywordNames());
                            }
                        }
                    }

                    if (image == null) {
                        result.rejectValue("file", "archive.addImage.invalidImageError");
                    } else {
                        if (changeData.isSubmitted()) {
                            changeData.setImageNm(image.getImageNm());
                            changeData.toImage(image);
                            facade.getImageService().updateData(image, changeData.getCategoryIds(), changeData.getImageKeywordNames());
                        }
                    }
                }

                if(command.isRedirToSearch()) {
                    uploadResponse.setRedirectOnAllComplete(contextPath+ "/web/archive");
                }

            } catch (Exception ex) {
                log.fatal(ex.getMessage(), ex);
                result.rejectValue("file", "archive.addImage.invalidImageError");
            } finally {
                validator.getTempFile().delete();
            }
        }

        if(result.hasErrors()) {
            List<String> errors = new ArrayList<String>();
            for(Object errorObj: result.getFieldErrors()) {
                FieldError error = (FieldError) errorObj;
                errors.add(facade.getCommonService().getMessage(error.getCode(), request.getLocale(), error.getArguments()));
            }
            uploadResponse.setImageErrors(errors);
        }

        if(dataResult.hasErrors()) {
            Map<String, String> errors = new HashMap<String, String>();
            for(Object errorObj: dataResult.getFieldErrors()) {
                FieldError error = (FieldError) errorObj;
                errors.put(error.getField(), facade.getCommonService().getMessage(error.getCode(), request.getLocale(), error.getArguments()));
            }
            uploadResponse.setDataErrors(errors);
        }

        Utils.writeJSON(uploadResponse, response);
    }

    @RequestMapping("/archive/add-image/verify-data")
    public void verifyDataHandler(
            @ModelAttribute("changeData") ChangeImageDataCommand changeData,
            BindingResult dataResult,
            HttpServletRequest request,
            HttpServletResponse response) {

        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();

        if (user.isDefaultUser()) {
            return;
        }

        UploadResponse uploadResponse = new UploadResponse();
        if(changeData.isSubmitted()) {
            ChangeImageDataValidator dataValidator = new ChangeImageDataValidator(facade, user, cms);
            ValidationUtils.invokeValidator(dataValidator, changeData, dataResult);
        }

        if(dataResult.hasErrors()) {
            Map<String, String> errors = new HashMap<String, String>();
            for(Object errorObj: dataResult.getFieldErrors()) {
                FieldError error = (FieldError) errorObj;
                errors.put(error.getField(), facade.getCommonService().getMessage(error.getCode(), request.getLocale(), error.getArguments()));
            }
            uploadResponse.setDataErrors(errors);
        }

        Utils.writeJSON(uploadResponse, response);
    }
}
