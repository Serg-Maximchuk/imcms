package com.imcode.imcms.servlet;

import imcode.server.user.UserDomainObject;
import imcode.util.Utility;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import com.imcode.imcms.servlet.superadmin.AdminManager;

public class AdminManagerSearchPage extends SearchDocumentsPage {
    private AdminManager.AdminManagerPage adminManagerPage;

    public AdminManagerSearchPage(AdminManager.AdminManagerPage adminManagerPage) {
        super(null, null);
        this.adminManagerPage = adminManagerPage;
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        UserDomainObject user = Utility.getLoggedOnUser( request );
        putInSessionAndForwardToPath("/imcms/" + user.getLanguageIso639_2() + "/jsp/admin/admin_manager_search.jsp",request, response);
    }

    public AdminManager.AdminManagerPage getAdminManagerPage() {
        return adminManagerPage;
    }
}
