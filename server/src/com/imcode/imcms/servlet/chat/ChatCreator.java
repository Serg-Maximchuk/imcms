package com.imcode.imcms.servlet.chat;

import imcode.external.chat.Chat;
import imcode.external.chat.ChatBase;
import imcode.external.chat.ChatError;
import imcode.external.diverse.FileManager;
import imcode.external.diverse.RmiConf;
import imcode.server.Imcms;
import imcode.server.ImcmsServices;
import imcode.server.user.UserDomainObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

//obs h�r ska det byggas om rej�lt
//har dock haft problem med att jag f�r olika servletContext
//vilket inneb�r att jag tappar chatten och anv�ndar sessionen d�rf�r
//anv�nder jag jsdk2.0 s� l�ngt det g�r, i v�ntan p� att jag eller n�gon l�st
//servletContext problemet

public class ChatCreator extends ChatBase {

    private final static String HTML_TEMPLATE = "admin_chat.html";
    private final static String HTML_TEMPLATES_BUTTON = "chat_template_admin.html";

    private final static String ADMIN_TEMPLATES_TEMPLATE = "chat_admin_template1.html";
    private final static String ADMIN_TEMPLATES_TEMPLATE_2 = "chat_admin_template2.html";
    private static final String MSG_TYPE_SAYS_TO = "100";
    private static final String MSG_TYPE_ASKS = "102";
    private static final int MAX_REFRESH_TIME = 180;

    /**
     * The POST method creates the html page when this side has been
     * redirected from somewhere else.
     */

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // Lets get an user object
        imcode.server.user.UserDomainObject user = super.getUserObj(req );
        if (user == null) return;
        if (!isUserAuthorized(req, res, user)) {
            log("isUserAuthorized==false");
            return;
        }

        String action = req.getParameter("action");

        if (action == null) {
            String header = "ChatCreator servlet. ";
            ChatError err = new ChatError(req, res, header, 3);
            log(header + err.getErrorMsg());
            return;
        }

        Chat myChat = (Chat) session.getAttribute("myChat");
        if (myChat == null) {
            myChat = createChat( getMetaId(req));
        }
        int metaId = myChat.getChatId();

        if (action.equalsIgnoreCase("admin_chat")) {
            log("admin_chat");
            //ok nu h�mtar vi in all data fr�n formul�ret och updaterar chat-objektet
            if (req.getParameter("addMsgType") != null && (!req.getParameter("msgType").trim().equals(""))) {
                myChat.addMsgType(req.getParameter("msgType"));
            }
            if (req.getParameter("removeMsgType") != null && req.getParameter("msgTypes") != null) {
                int type = Integer.parseInt(req.getParameter("msgTypes"));
                if (type < 100 || type >= 104) {
                    myChat.removeMsgType(type);
                }
            }
            if (req.getParameterValues("authorized") != null) {
                myChat.setSelectedAuto(req.getParameterValues("authorized"));
            }
            if (req.getParameter("update") != null) {
                myChat.setRefreshTime(Integer.parseInt(req.getParameter("update")));
            }
            if (req.getParameter("reload") != null) {
                myChat.setAutoRefreshEnabled(Integer.parseInt(req.getParameter("reload")));
            }
            if (req.getParameter("inOut") != null) {
                myChat.setShowEnterAndLeaveMessagesEnabled(Integer.parseInt(req.getParameter("inOut")));
            }
            if (req.getParameter("private") != null) {
                myChat.setShowPrivateMessagesEnabled(Integer.parseInt(req.getParameter("private")));
            }
            if (req.getParameter("dateTime") != null) {
                myChat.setShowDateTimesEnabled(Integer.parseInt(req.getParameter("dateTime")));
            }
            if (req.getParameter("font") != null) {
                myChat.setFontSize(Integer.parseInt(req.getParameter("font")));
            }

            ImcmsServices imcref = Imcms.getServices();

            //lets save to db
            if (req.getParameter("okChat") != null) {
                createChat(metaId, myChat, imcref, user, res);
                return;

            }//end if(action.equalsIgnoreCase("okChat"))

            //check if template adminpage is wanted
            if (req.getParameter("admin_templates_meta") != null) {
                log("admin_templates_meta");
                if (req.getParameter("add_templates") != null) {
                    String newLibName = req.getParameter("template_lib_name");
                    if (newLibName == null) {
                        String header = "ChatCreator servlet. ";
                        new ChatError(req, res, header, 80); //obs kolla om r�tt nr
                        return;
                    }
                    // Lets check if we already have a templateset with that name
                    String libNameExists = imcref.getExceptionUnhandlingDatabase().executeStringProcedure( "C_FindTemplateLib", new String[] {newLibName} );
                    if (!libNameExists.equalsIgnoreCase("-1")) {
                        String header = "ChatCreator servlet. ";
                        new ChatError(req, res, header, 84);//obs kolla om r�tt nr
                        return;
                    }
                    log("lang_prefix for External Template Folder = " + user.getLanguageIso639_2());
                    imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_AddTemplateLib", new String[] {newLibName} );
                    // Lets copy the original folders to the new foldernames
                    FileManager fileObj = new FileManager();
                    File templateSrc = new File(imcref.getExternalTemplateFolder(metaId, user), "original");
                    File imageSrc = new File(RmiConf.getImagePathForExternalDocument(imcref, metaId, user), "original");
                    File templateTarget = new File(imcref.getExternalTemplateFolder(metaId, user), newLibName);
                    File imageTarget = new File(RmiConf.getImagePathForExternalDocument(imcref, metaId, user), newLibName);

                    fileObj.copyDirectory(templateSrc, templateTarget);
                    fileObj.copyDirectory(imageSrc, imageTarget);
                }//done add new template lib

                if (req.getParameter("change_templatelib") != null) {//ok lets handle the change set case
                    log("change_templatelib");
                    // Lets get the new library name and validate it
                    String newLibName = req.getParameter("new_templateset_name");
                    //log("newLibName: "+newLibName);
                    if (newLibName == null) {
                        String header = "ChatCreator servlet. ";
                        new ChatError(req, res, header, 80);//obs kolla om r�tt nr
                        return;
                    }
                    // Lets find the selected template in the database and get its id
                    // if not found, -1 will be returned
                    String templateId = imcref.getExceptionUnhandlingDatabase().executeStringProcedure( "C_GetTemplateIdFromName", new String[] {newLibName} );
                    if (templateId.equalsIgnoreCase("-1")) {
                        String header = "ChatCreator servlet. ";
                        new ChatError(req, res, header, 81);
                        return;
                    }
                    // Ok, lets update the chat with this new templateset.
                    //but first lets delete the old one.
                    imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_deleteChatTemplateset", new String[] {""
                                                                                                                             + metaId} );

                    imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_SetNewTemplateLib", new String[] {""
                                                                                                                         + metaId,
                                                                                                    newLibName} );
                }
                if (req.getParameter("UPLOAD_CHAT") != null) {
                    log("UPLOAD_CHAT");
                    //ok lets handle the upload of templates and images
                    String folderName = req.getParameter("TEMPLATE_NAME");
                    String uploadType = req.getParameter("UPLOAD_TYPE");
                    //log(folderName +" "+uploadType+" "+metaId);
                    if (folderName == null || uploadType == null) {
                        return;
                    }
                    Vector tags = new Vector();
                    tags.add("#META_ID#");
                    tags.add(metaId + "");
                    tags.add("#UPLOAD_TYPE#");
                    tags.add(uploadType);
                    tags.add("#FOLDER_NAME#");
                    tags.add(folderName);
                    //sendHtml(req,res,vm, ADMIN_TEMPLATES_TEMPLATE_2) ;
                    sendHtml(req, res, tags, ADMIN_TEMPLATES_TEMPLATE_2, null);
                    return;
                }
            }
            //check if template adminpage is wanted
            if (req.getParameter("adminTemplates") != null) {
                log("adminTemplates");
                //ok now lets get the template set name
                String templateSetName = imcref.getExceptionUnhandlingDatabase().executeStringProcedure( "C_GetTemplateLib", new String[] {""
                                                                                                                                           + metaId} );
                if (templateSetName == null) {
                    templateSetName = "";
                }
                //ok lets get all the template set there is
                String[] templateLibs = imcref.getExceptionUnhandlingDatabase().executeArrayProcedure( "C_GetAllTemplateLibs", new String[] {} );
                Vector vect = new Vector();
                if (templateLibs != null) {
                    vect = super.convert2Vector(templateLibs);
                }
                Vector tags = new Vector();
                tags.add("#TEMPLATE_LIST#");
                tags.add(createOptionCode(templateSetName, vect));
                tags.add("#CURRENT_TEMPLATE_SET#");
                tags.add(templateSetName);
                sendHtml(req, res, tags, ADMIN_TEMPLATES_TEMPLATE, null);
                return;

            }

        }//end if(action.equalsIgnoreCase("ADD_CHAT"))
        log("default k�ret");
        sendHtml(req, res, createTaggs(myChat, user), HTML_TEMPLATE, myChat);

    } // End POST

    private void createChat( int metaId, Chat myChat, ImcmsServices imcref,
                             UserDomainObject user, HttpServletResponse res ) throws IOException {
        log("okChat");

        String result = imcref.getExceptionUnhandlingDatabase().executeStringProcedure( "C_FindMetaId", new String[] {""
                                                                                                                      + metaId} );
        boolean chatDoesNotExist = result.equals("1");
        if( chatDoesNotExist ) {
            imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_AddNewChat", new String[] {"" + metaId,
                                                                                            myChat.getChatName(),
                                                                                            "3"} );
        }

        imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_Delete_MsgTypes", new String[] {""
                                                                                                           + metaId} );

        //lets connect the standard msgTypes with the chat
        String[] tempTypes = imcref.getExceptionUnhandlingDatabase().executeArrayProcedure( "C_GetBaseMsgTypes", new String[] {} );
        for (int i = 0; i < tempTypes.length; i++) {
            String tempTypeId = imcref.getExceptionUnhandlingDatabase().executeStringProcedure( "C_GetMsgTypeId", new String[] {tempTypes[i]} );
            /*TODO
            This is only a temporary blocking of standard message types
            we only select message type " s�ger till" and "fr�gar"
            */
            if (MSG_TYPE_SAYS_TO.equals(tempTypeId) || MSG_TYPE_ASKS.equals(tempTypeId)) {
                imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_AddNewChatMsg", new String[] {tempTypeId,
                                                                                        "" + metaId} );
            }
        }

        // Lets add the new msgTypes to the db /ugly but it works
        Vector msgV = myChat.getMsgTypes();
        for (int i = 0; i < msgV.size(); i += 2) {
            imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_AddMessageType", new String[] {""
                                                                                                              + metaId,
                                                                                    "" + msgV.get( i ),
                                                                                    "" + msgV.get( i + 1 )} );
        }

        //	Vector valuesV = new Vector();

        imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_AddChatParams", new String[] {
                                                                                "" + metaId,
                                                                                "" + myChat.getRefreshTime(),
                                                                                "" + myChat.isAutoRefreshEnabled(),
                                                                                ""
                                                                                + myChat.isShowEnterAndLeaveMessagesEnabled(),
                                                                                ""
                                                                                + myChat.isShowPrivateMessagesEnabled(),
                                                                                ""
                                                                                + myChat.isShowPublicMessagesEnabled(),
                                                                                "" + myChat.isShowDateTimesEnabled(),
                                                                                "" + myChat.getfont()} );


        // lets delete all authorization for one meta
        imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_DeleteAuthorizations", new String[] {""
                                                                                                                + metaId} );

        //ok lets add the authorization types
        Vector autoV = myChat.getSelectedAuto();
        for (int i = 0; i < autoV.size(); i++) {
            imcref.getExceptionUnhandlingDatabase().executeUpdateProcedure( "C_ChatAutoTypes", new String[] {""
                                                                                                             + autoV.elementAt( i ),
                                                                                    "" + metaId} );
        }
        //ok now we have saved the stuff to the db so lets set up the chat and put it in the context
        String[][] messages = imcref.getExceptionUnhandlingDatabase().execute2dArrayProcedure( "C_GetMsgTypes", new String[] {""
                                                                                                                              + metaId} );
        if (messages != null) {
            myChat.setMsgTypes(convert2Vector(messages));
        }
        ServletContext myContext = getServletContext();
        myContext.setAttribute("theChat" + metaId, myChat);

        // Ok, we're done adding the chat, Lets log in to it!
        res.sendRedirect("ChatLogin?login_type=login&meta_id=" + metaId);
    }

    /**
     * The GET method creates the html page when this side has been
     * redirected from somewhere else.
     * <p/>
     * laddar admin sida osv
     */
    //laddar admin sida osv
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        log("startar do get");
        // Lets validate the session, e.g has the user logged in to Janus?
        HttpSession session = req.getSession(false);

        // Lets get an user object
        imcode.server.user.UserDomainObject user = super.getUserObj(req );
        if (user == null) return;

        if (!isUserAuthorized(req, res, user)) {
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            action = (String) req.getAttribute("action");
            if (action == null) {
                String header = "ChatCreator servlet. ";
                ChatError err = new ChatError(req, res, header, 3);
                log(header + err.getErrorMsg());
                return;
            }
        }

        // ********* Create NEW Chat *********************************************************
        if (action.equalsIgnoreCase("NEW")) {
            log("NEW");
            //vi m�ste h�mta allt som beh�vs fr�n databasen och sedan fixa till mallen

            //skapa en temp chat
            int meta_id = Integer.parseInt((String) session.getAttribute("Chat.meta_id"));
            Chat myChat = createChat( meta_id);
            session.setAttribute("myChat", myChat);
            // Lets build the Responsepage to the loginpage
            Vector vect = createTaggs(myChat, user);
            sendHtml(req, res, vect, HTML_TEMPLATE, myChat);
            return;
        }

        String templateAdmin = req.getParameter("ADMIN_TEMPLATES");
        if (templateAdmin != null) {//ok we have done upload template or image lets get back to the adminpage
            this.doPost(req, res);
            return;
        }

        if (action.equalsIgnoreCase("admin_chat")) {
            log("action =  admin_chat");

            //check which chat we have
            String chatName = req.getParameter("chatName");
            log("ChatName: " + chatName);
            Vector tags = new Vector();
            tags.add("#chatName#");
            tags.add(chatName);

            String metaId = (String) session.getAttribute("Chat.meta_id");
            log("MetaId: " + metaId);

            ServletContext myContext = getServletContext();
            Chat myChat = (Chat) myContext.getAttribute("theChat" + metaId);

            log("Chat: " + myChat);

            Vector vect = createTaggs(myChat, user);
            sendHtml(req, res, vect, HTML_TEMPLATE, myChat);
            return;

        }

    } // End doGet

    private String getTemplateButtonHtml(int metaId, UserDomainObject user) {
        ImcmsServices imcref = Imcms.getServices();
        return imcref.getTemplateFromSubDirectoryOfDirectory( HTML_TEMPLATES_BUTTON, user, null, "103", getTemplateSetDirectoryName( metaId));
    }

    private Vector createTaggs(Chat chat, UserDomainObject user) {

        Vector bv = new Vector();
        bv.add("1");
        bv.add("2");
        bv.add("3");
        Vector taggs = new Vector();
        taggs.add("#msgTypes#");
        taggs.add(createOptionCode("s�ger till", chat.getMsgTypes()));
        taggs.add("#authorized#");
        taggs.add(createOptionCode(chat.getSelectedAuto(), chat.getAuthorizations()));
        taggs.add("#msgType#");
        taggs.add("");
        taggs.add("#updateTime#");
        taggs.add(createOptionCode(chat.getRefreshTime() + "", createUpdateTimeV()));
        taggs.add("#reload#");
        taggs.add(createRadioButton("reload", bv, chat.isAutoRefreshEnabled() + ""));
        taggs.add("#inOut#");
        taggs.add(createRadioButton("inOut", bv, chat.isShowEnterAndLeaveMessagesEnabled() + ""));
        taggs.add("#private#");
        taggs.add(createRadioButton("private", bv, chat.isShowPrivateMessagesEnabled() + ""));
        taggs.add("#dateTime#");
        taggs.add(createRadioButton("dateTime", bv, chat.isShowDateTimesEnabled() + ""));
        taggs.add("#font#");
        taggs.add(createRadioButton("font", bv, chat.getfont() + ""));

        taggs.add("#templates#");
        taggs.add(getTemplateButtonHtml(chat.getChatId(), user));

        return taggs;
    }

    public static Vector createUpdateTimeV() {
        Vector vect = new Vector();
        for (int i = 10; i < MAX_REFRESH_TIME + 10; i += 10) {
            vect.add(i + "");
            vect.add(i + "");
        }
        return vect;
    }

} // End class

