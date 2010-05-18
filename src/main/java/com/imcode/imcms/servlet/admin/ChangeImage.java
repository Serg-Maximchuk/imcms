package com.imcode.imcms.servlet.admin;

import imcode.server.Imcms;
import imcode.server.ImcmsConstants;
import imcode.server.ImcmsServices;
import imcode.server.document.ConcurrentDocumentModificationException;
import imcode.server.document.NoPermissionToEditDocumentException;
import imcode.server.document.textdocument.ImageDomainObject;
import imcode.server.document.textdocument.NoPermissionToAddDocumentToMenuException;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.ShouldHaveCheckedPermissionsEarlierException;
import imcode.util.ShouldNotBeThrownException;
import imcode.util.Utility;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.imcode.imcms.dao.ImageDao;
import com.imcode.imcms.flow.DispatchCommand;
import com.imcode.imcms.mapping.DocumentMapper;
import com.imcode.imcms.mapping.DocumentSaveException;
import com.imcode.imcms.util.l10n.LocalizedMessage;
import com.imcode.imcms.util.l10n.LocalizedMessageFormat;

/**
 * Change image withe the same 'no' for all available languages. 
 */
public class ChangeImage extends HttpServlet {

    public static final String REQUEST_PARAMETER__IMAGE_INDEX = "img";
    public static final String REQUEST_PARAMETER__LABEL = "label";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ImcmsServices imcref = Imcms.getServices();
        final UserDomainObject user = Utility.getLoggedOnUser(request);
        final DocumentMapper documentMapper = imcref.getDocumentMapper();
        final Integer documentId = Integer.parseInt(request.getParameter("meta_id"));
        
        //final TextDocumentDomainObject document = (TextDocumentDomainObject)documentMapper.getDocument(
        //		documentId, user.getDocumentShowSettings().getVersionSelector());

        final TextDocumentDomainObject document = (TextDocumentDomainObject)documentMapper.getDocument(
        		documentId);        
        
        final int imageIndex = Integer.parseInt(request.getParameter(REQUEST_PARAMETER__IMAGE_INDEX));        
        
        /**
         * Image DTO. Holds generic properties such as size and border. 
         */
        final ImageDomainObject defaultImage = document.getImage(imageIndex);
        final ImageDomainObject image = defaultImage != null 
        	? defaultImage
        	: new ImageDomainObject();

        image.setNo(imageIndex);

        // Check if user has image rights
        if ( !ImageEditPage.userHasImagePermissionsOnDocument(user, document) ) {
            Utility.redirectToStartDocument(request, response);
            return;
        }

        DispatchCommand returnCommand = new DispatchCommand() {
            public void dispatch(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
                response.sendRedirect("AdminDoc?meta_id=" + document.getId() + "&flags="
                                      + ImcmsConstants.DISPATCH_FLAG__EDIT_TEXT_DOCUMENT_IMAGES);

            }
        };
        
        Handler<List<ImageDomainObject>> imageCommand = new Handler<List<ImageDomainObject>>() {
            public void handle(List<ImageDomainObject> images) {
                ImcmsServices services = Imcms.getServices();
                
                try {
                    services.getDocumentMapper().saveTextDocImages(document, images, user);
                } catch ( NoPermissionToEditDocumentException e ) {
                    throw new ShouldHaveCheckedPermissionsEarlierException(e);
                } catch ( NoPermissionToAddDocumentToMenuException e ) {
                    throw new ConcurrentDocumentModificationException(e);
                } catch ( DocumentSaveException e ) {
                    throw new ShouldNotBeThrownException(e);
                }
                services.updateMainLog("ImageRef " + imageIndex + " =" + image.getUrlPathRelativeToContextPath() +
                                       " in  " + "[" + document.getId() + "] modified by user: [" +
                                       user.getFullName() + "]");

            }
            
        };
        
        ImageDao imageDao = (ImageDao)Imcms.getServices().getSpringBean("imageDao");
        
        List<ImageDomainObject> images = imageDao.getImagesByIndex(document.getMeta().getId(), document.getVersion().getNo() ,imageIndex, true);
        
        LocalizedMessage heading = new LocalizedMessageFormat("image/edit_image_on_page", imageIndex, document.getId());
        ImageEditPage imageEditPage = new ImageEditPage(document, image, heading, StringUtils.defaultString(request.getParameter(REQUEST_PARAMETER__LABEL)), getServletContext(), imageCommand, returnCommand, true);
        
        imageEditPage.setImages(images);
        imageEditPage.forward(request, response);
    }
}
