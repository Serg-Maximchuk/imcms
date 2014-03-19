package com.imcode.imcms.addon.imagearchive.controller;

import com.imcode.imcms.addon.imagearchive.SessionConstants;
import com.imcode.imcms.addon.imagearchive.entity.Keywords;
import com.imcode.imcms.addon.imagearchive.service.Facade;
import com.imcode.imcms.addon.imagearchive.util.SessionUtils;
import com.imcode.imcms.addon.imagearchive.util.Utils;
import com.imcode.imcms.api.ContentManagementSystem;
import com.imcode.imcms.api.User;
import com.imcode.imcms.servlet.admin.ImageEditPage;
import imcode.util.Utility;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Controller
public class GeneralController {
    private static final Log log = LogFactory.getLog(GeneralController.class);
    
    @Autowired
    private Facade facade;
    
    @Autowired
    private SessionLocaleResolver localeResolver;

    @RequestMapping("/archive/language")
    public ModelAndView languageChangeHandler(
            @RequestParam(required=false) String lang, 
            @RequestParam(required=false) String redir, 
            HttpServletResponse response,
            HttpServletRequest request) {
        lang = StringUtils.trimToNull(lang);
        redir = StringUtils.trimToNull(redir);
        
        if (lang != null && facade.getConfig().getLanguages().containsValue(lang)) {
            localeResolver.setLocale(request, response, new Locale(lang));
        }

        if (redir != null) {
            try {
                response.sendRedirect(response.encodeRedirectURL(redir));
            } catch (IOException ex) {
                log.warn(ex.getMessage(), ex);
            }
        } else {
            return new ModelAndView("redirect:/archive");
        }
        
        return null;
    }
    
    @RequestMapping("/archive/use")
    public String useInImcmsHandler(
            @RequestParam(required=false) Long id, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            HttpSession session) {
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        
        String returnTo = SessionUtils.getImcmsReturnToUrl(session);
        
        if (user.isDefaultUser()) {
            return "redirect:/web/archive";
        } else if (id == null || returnTo == null || !facade.getImageService().canUseImage(user, id, cms)) {
            return "redirect:/web/archive";
        }
        
        String imageName = facade.getImageService().findImageName(id);
        String fileName = facade.getFileService().transferImageToImcms(id);
        String altText = StringUtils.defaultString(facade.getImageService().findImageAltText(id));
        
        StringBuilder builder = new StringBuilder(returnTo);
        builder.append("&archive_img_id=");
        builder.append(id);
        builder.append("&archive_img_nm=");
        builder.append(Utils.encodeUrl(imageName));
        builder.append("&archive_file_nm=");
        builder.append(Utils.encodeUrl(fileName));
        if(!"".equals(altText)) {
            builder.append("&" + ImageEditPage.REQUEST_PARAMETER__IMAGE_ARCHIVE_IMAGE_ALT_TEXT + "=");
            builder.append(Utils.encodeUrl(altText));
        }
        
        try {
            response.sendRedirect(builder.toString());
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
        }
        
        session.removeAttribute(SessionConstants.IMCMS_RETURN_URL);
        
        return null;
    }
    
    @RequestMapping("/archive/back")
    public String backToImcmsHandler(HttpServletResponse response, HttpSession session) {
        String returnTo = SessionUtils.getImcmsReturnToUrl(session);
        if (returnTo == null) {
            return "redirect:/web/archive";
        }
        
        session.removeAttribute(SessionConstants.IMCMS_RETURN_URL);
        
        try {
            response.sendRedirect(returnTo);
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
        }
        
        return null;
    }
    
    @RequestMapping("/archive/service/keyword/add")
    public void addKeywordHandler(
    		@RequestParam(required=false) String keyword, 
    		HttpServletResponse response) {
    	Utils.addNoCacheHeaders(response);
    	
    	keyword = StringUtils.trimToEmpty(keyword);
    	
    	if (StringUtils.isEmpty(keyword)) {
    		Utils.sendErrorCode(response, HttpServletResponse.SC_NOT_FOUND);
    	} else {
    		keyword = StringUtils.substring(keyword, 0, 50);
    		if(!facade.getImageService().createKeyword(keyword)) {
                Map<String, String> error = new HashMap<String, String>();
                error.put("error", "alreadyExists");
                Utils.writeJSON(error, response);
            } else {
                Map<String, String> newKeyword = new HashMap<String, String>();
                newKeyword.put("newKeyword", keyword);
                Utils.writeJSON(newKeyword, response);
            }
    	}
    }

    @RequestMapping("/archive/service/keyword/list")
    public void keywordListHandler(@RequestParam(required = false) String text,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session) {

        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();

        if(user == null) {
            return;
        }

        List<String> keywords = new ArrayList<String>();
        text = StringUtils.trimToEmpty(text);
        if (!text.isEmpty()) {
            text = "%" + text + "%";
            for (Keywords keyword : facade.getImageService().findKeywords(text)) {
                keywords.add(keyword.getKeywordNm());
            }
        }

        Map<String, List<String>> tmp = new HashMap<String, List<String>>();
        tmp.put("keywords", keywords);
        Utils.writeJSON(tmp, response);
    }

    @RequestMapping("/archive/logOut")
    public void logOut(
            @RequestParam(required = false) String redirectTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Utility.makeUserLoggedOut(request);

        if(redirectTo != null) {
            try {
                response.sendRedirect(redirectTo);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }
}
