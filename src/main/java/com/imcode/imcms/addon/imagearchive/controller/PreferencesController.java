package com.imcode.imcms.addon.imagearchive.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.imcode.imcms.addon.imagearchive.command.CreateCategoryCommand;
import com.imcode.imcms.addon.imagearchive.command.EditCategoryCommand;
import com.imcode.imcms.addon.imagearchive.command.PreferencesActionCommand;
import com.imcode.imcms.addon.imagearchive.command.SaveLibraryRolesCommand;
import com.imcode.imcms.addon.imagearchive.command.SaveRoleCategoriesCommand;
import com.imcode.imcms.addon.imagearchive.dto.LibraryRolesDto;
import com.imcode.imcms.addon.imagearchive.entity.Categories;
import com.imcode.imcms.addon.imagearchive.entity.CategoryTypes;
import com.imcode.imcms.addon.imagearchive.entity.Libraries;
import com.imcode.imcms.addon.imagearchive.entity.Roles;
import com.imcode.imcms.addon.imagearchive.service.Facade;
import com.imcode.imcms.addon.imagearchive.service.exception.CategoryExistsException;
import com.imcode.imcms.addon.imagearchive.util.ArchiveSession;
import com.imcode.imcms.addon.imagearchive.util.Utils;
import com.imcode.imcms.addon.imagearchive.validator.CreateCategoryValidator;
import com.imcode.imcms.addon.imagearchive.validator.EditCategoryValidator;
import com.imcode.imcms.addon.imagearchive.validator.SaveLibraryRolesValidator;
import com.imcode.imcms.api.ContentManagementSystem;
import com.imcode.imcms.api.User;

@Controller
public class PreferencesController {
    private static final Log log = LogFactory.getLog(PreferencesController.class);
    
    private static final String ROLE_KEY = Utils.makeKey(PreferencesController.class, "role");
    private static final String LIBRARY_KEY = Utils.makeKey(PreferencesController.class, "library");
    
    @Autowired
    private Facade facade;
    
    
    @RequestMapping("/archive/preferences")
    public String indexHandler(
            @ModelAttribute PreferencesActionCommand actionCommand, 
            
            @ModelAttribute("createCategory") CreateCategoryCommand createCategoryCommand, 
            BindingResult createCategoryResult,
            
            @ModelAttribute("editCategory") EditCategoryCommand editCategoryCommand, 
            BindingResult editCategoryResult, 
            
            @ModelAttribute("saveLibraryRoles") SaveLibraryRolesCommand librariesCommand, 
            BindingResult librariesResult,
            
            @ModelAttribute("saveCategories") SaveRoleCategoriesCommand roleCategoriesCommand, 
            
            HttpServletRequest request, 
            HttpServletResponse response, 
            Map<String, Object> model) {
        
        ArchiveSession session = ArchiveSession.getSession(request);
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        
        if (user.isDefaultUser()) {
            Utils.redirectToLogin(request, response, facade);
            
            return null;
        } else if (!user.isSuperAdmin()) {
            return "redirect:/web/archive";
        }
        
        facade.getLibraryService().syncLibraryFolders();
        
        List<Roles> roles = facade.getRoleService().findRoles();
        Roles role = getRole(session, roles);
        model.put("currentRole", role);
        
        List<Libraries> libraries = facade.getLibraryService().findLibraries();
        Libraries library = getLibrary(session, libraries);
        
        List<Roles> availableLibraryRoles = Collections.emptyList();
        List<LibraryRolesDto> libraryRoles = Collections.emptyList();
        
        if (library != null) {
            availableLibraryRoles = facade.getLibraryService().findAvailableRoles(library.getId());
            
            libraryRoles = facade.getLibraryService().findLibraryRoles(library.getId());
        }
        
        model.put("availableLibraryRoles", availableLibraryRoles);
        model.put("libraryRoles", libraryRoles);
        model.put("currentLibrary", library);
        
        if (actionCommand.isCreateCategory()) {
            processCreateCategory(createCategoryCommand, createCategoryResult);
            
        } else if (actionCommand.isEditCategory()) {
            processEditCategory(editCategoryCommand);
            
        } else if (actionCommand.isSaveCategory()) {
            processSaveCategory(editCategoryCommand, editCategoryResult);
            
        } else if (actionCommand.isRemoveCategory()) {
            processRemoveCategory(editCategoryCommand);
            
        } else if (actionCommand.isSaveLibraryRoles() && library != null && librariesCommand.getLibraryRoles() != null) {
            processSaveLibraryRoles(librariesCommand, librariesResult, model);
            
        } else if (actionCommand.isSaveRoleCategories()) {
            processSaveRoleCategories(roleCategoriesCommand, model);
            
        }
        
        if (!actionCommand.isSaveLibraryRoles() && library != null) {
            librariesCommand.setLibraryNm(library.getLibraryNm());
        }
        
        model.put("categoryTypes", facade.getCategoryService().getCategoryTypes());
        model.put("categories", facade.getCategoryService().getCategories());
        
        model.put("roles", roles);
        
        model.put("libraries", facade.getLibraryService().findLibraries());
        model.put("freeCategories", facade.getRoleService().findFreeCategories(role.getId()));
        model.put("roleCategories", facade.getRoleService().findRoleCategories(role.getId()));
        
        return "image_archive/pages/preferences";
    }
    
    private void processCreateCategory(CreateCategoryCommand command, BindingResult result) {
        CreateCategoryValidator validator = new CreateCategoryValidator();
        ValidationUtils.invokeValidator(validator, command, result);
        
        if (!result.hasErrors()) {
            String categoryName = command.getCreateCategoryName();
            int categoryTypeId = command.getCreateCategoryType();
            
            try {
                facade.getCategoryService().createCategory(categoryName, categoryTypeId);
                
                command.setCreateCategoryName("");
                command.setCreateCategoryType(0);
            } catch (CategoryExistsException ex) {
                result.rejectValue("createCategoryName", "archive.preferences.categoryExistsError");
            }
        }
    }
    
    private void processEditCategory(EditCategoryCommand command) {
        Categories category = facade.getCategoryService().getCategory(command.getEditCategoryId());
        if (category != null) {
            command.setShowEditCategory(true);
            command.setEditCategoryName(category.getName());
            command.setEditCategoryType(category.getTypeId());
        }
    }
    
    private void processSaveCategory(EditCategoryCommand command, BindingResult result) {
        EditCategoryValidator validator = new EditCategoryValidator();
        ValidationUtils.invokeValidator(validator, command, result);
        
        if (!result.hasErrors()) {
            int categoryId = command.getEditCategoryId();
            int typeId = command.getEditCategoryType();
            String newName = command.getEditCategoryName();
            
            try {
                facade.getCategoryService().updateCategory(categoryId, newName, typeId);
                
            } catch (CategoryExistsException ex) {
                result.rejectValue("editCategoryName", "archive.preferences.categoryExistsError");
            }
        }
    }
    
    private void processRemoveCategory(EditCategoryCommand command) {
        facade.getCategoryService().deleteCategory(command.getEditCategoryId());
        
        command.setEditCategoryName("");
        command.setShowEditCategory(false);
    }
    
    @SuppressWarnings("unchecked")
    private void processSaveLibraryRoles(SaveLibraryRolesCommand command, BindingResult result, Map<String, Object> model) {
        SaveLibraryRolesValidator validator = new SaveLibraryRolesValidator();
        ValidationUtils.invokeValidator(validator, command, result);
        
        Libraries library = (Libraries) model.get("currentLibrary");
        
        if (!result.hasErrors()) {
            try {
                facade.getLibraryService().updateLibraryRoles(library.getId(), command.getLibraryNm(), command.getLibraryRoles());
                
                library.setLibraryNm(command.getLibraryNm());
            } catch (Exception ex) {
                log.warn(ex.getMessage(), ex);
            }
        }
        
        List<LibraryRolesDto> libraryRoles = command.getLibraryRoles();
        List<LibraryRolesDto> assignedLibraryRoles = facade.getLibraryService().findLibraryRoles(library.getId());
        List<Roles> availableLibraryRoles = facade.getLibraryService().findAvailableRoles(library.getId());
        
        for (LibraryRolesDto oldLibrary : assignedLibraryRoles) {
            if (!libraryRoles.contains(oldLibrary)) {
                Roles role = new Roles(oldLibrary.getRoleId());
                role.setRoleName(oldLibrary.getRoleName());
                
                availableLibraryRoles.add(role);
            }
        }
        
        for (LibraryRolesDto libraryRole : libraryRoles) {
            availableLibraryRoles.remove(new Roles(libraryRole.getRoleId()));
        }
        
        model.put("libraryRoles", libraryRoles);
        model.put("availableLibraryRoles", availableLibraryRoles);
    }
    
    private void processSaveRoleCategories(SaveRoleCategoriesCommand command, Map<String, Object> model) {
        try {
            Roles role = (Roles) model.get("currentRole");
            
            facade.getRoleService().assignCategoryRoles(role, command.getAssignedCategoryIds(), 
                    command.isCanUse(), command.isCanChange());
        } catch (Exception ex) {
            log.fatal(ex.getMessage(), ex);
        }
    }
    
    
    @RequestMapping("/archive/preferences/role")
    public String changeCurrentRoleHandler(
            @RequestParam(required=false) Integer id, 
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        ArchiveSession session = ArchiveSession.getSession(request);
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        
        if (user.isDefaultUser()) {
            Utils.redirectToLogin(request, response, facade);
            
            return null;
        } else if (!user.isSuperAdmin()) {
            return "redirect:/web/archive";
        }
        
        Roles role = null;
        if (id != null && (role = facade.getRoleService().findRoleById(id)) != null) {
            session.put(ROLE_KEY, role);
        }
        
        return "redirect:/web/archive/preferences";
    }
    
    @RequestMapping("/archive/preferences/library")
    public String changeCurrentLibraryHander(
            @RequestParam(required=false) Integer id, 
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        ArchiveSession session = ArchiveSession.getSession(request);
        ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
        User user = cms.getCurrentUser();
        
        if (user.isDefaultUser()) {
            Utils.redirectToLogin(request, response, facade);
            
            return null;
        } else if (!user.isSuperAdmin()) {
            return "redirect:/web/archive";
        }
        
        Libraries library = null;
        if (id != null && (library = facade.getLibraryService().findLibraryById(id)) != null) {
            session.put(LIBRARY_KEY, library);
        }
        
        return "redirect:/web/archive/preferences";
    }
    
    private static Roles getRole(ArchiveSession session, List<Roles> roles) {
        Roles role = (Roles) session.get(ROLE_KEY);
        if (role == null && roles != null) {
            role = roles.get(0);
            session.put(ROLE_KEY, role);
        }
        
        return role;
    }
    
    private static Libraries getLibrary(ArchiveSession session, List<Libraries> libraries) {
        Libraries library = (Libraries) session.get(LIBRARY_KEY);
        
        if (library != null && libraries != null) {
            boolean exists = false;
            for (Libraries lib : libraries) {
                if (lib.getId() == library.getId()) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                library = null;
                session.remove(LIBRARY_KEY);
            }
        }
        
        if (library == null && libraries != null && !libraries.isEmpty()) {
            library = libraries.get(0);
            session.put(LIBRARY_KEY, library);
        }
        
        return library;
    }
}
