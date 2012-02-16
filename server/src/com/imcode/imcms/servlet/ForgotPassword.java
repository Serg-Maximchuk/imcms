package com.imcode.imcms.servlet;

import com.sun.xml.internal.fastinfoset.util.CharArray;
import imcode.server.Imcms;
import imcode.server.SystemData;
import imcode.server.user.UserDomainObject;
import imcode.util.net.SMTP;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPassword extends HttpServlet {

    // todo: add captcha?
    // todo: add logging

    // Available commands.
    public enum Op {
        REQUEST_RESET,
        SEND_RESET_URL,
        RESET,
        SAVE_NEW_PASSWORD,
    }


    // Request parameters associated with commands.
    public static final String
            REQUEST_PARAM_OP = "op",
            REQUEST_PARAM_RESET_ID = "id",
            REQUEST_PARAM_EMAIL = "email",
            REQUEST_PARAM_PASSWORD = "password",
            REQUEST_PARAM_PASSWORD_CHECK = "password_check";

    // Attribute is bounded to List<String> of error messages.
    public static final String REQUEST_ATTR_VALIDATION_ERRORS = "form_errors";


    // Views
    private static final String
            account_email_form_view = "account_email_form.jsp",
            email_sent_confirmation_view = "email_sent_confirmation.jsp",
            new_password_form_view = "password_reset_form.jsp",
            password_changed_confirmation_view = "password_changed_confirmation.jsp";


    private ExecutorService emailSender = Executors.newFixedThreadPool(5);


    /**
     * Forwards request to password reset or password edit view.
     *
     * Password reset request does not require any additional parameters.
     * Password edit request expects valid reset-id parameter.
     *
     * Forwards to 404 if request parameters do not met requirements.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Op op = getOp(request);
        String view = op == Op.REQUEST_RESET
                ? account_email_form_view
                : op == Op.RESET && getUserByPasswordResetId(request) != null
                    ? new_password_form_view
                    : null;

        render(request, response, view);
    }


    /**
     * Handles email sending and new password saving.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Op op = getOp(request);
        String view = null;

        if (op == Op.SEND_RESET_URL) {
            String userEmail = StringUtils.trimToEmpty(request.getParameter(REQUEST_PARAM_EMAIL));

            if (userEmail.isEmpty()) {
                setValidationErrors(request, "Missing email address");
                view = account_email_form_view;
            } else {
                // todo: only by email ???
                UserDomainObject user = Imcms.getServices().getImcmsAuthenticatorAndUserAndRoleMapper().createPasswordReset(userEmail);

                if (user != null) {
                    System.out.println(user.getPasswordReset());

                    String url = String.format("%s?%s=%s&%s=%s",
                            request.getRequestURL(),
                            REQUEST_PARAM_OP, Op.RESET.toString().toLowerCase(),
                            REQUEST_PARAM_RESET_ID, user.getPasswordReset().getId());

                    asyncSendPasswordResetEMail(request, user, url);
                }

                view = email_sent_confirmation_view;
            }
        } else if (op == Op.SAVE_NEW_PASSWORD) {
            UserDomainObject user = getUserByPasswordResetId(request);

            if (user != null) {
                String password = StringUtils.trimToEmpty(request.getParameter(REQUEST_PARAM_PASSWORD));
                String passwordCheck = StringUtils.trimToEmpty(request.getParameter(REQUEST_PARAM_PASSWORD_CHECK));

                // todo: check password length and strength
                if (password.isEmpty() || !password.equals(passwordCheck)) {
                    setValidationErrors(request, "Password must not be empty and must be equal to reentered password.");
                    view = new_password_form_view;
                } else {
                    user.setPassword(password);
                    Imcms.getServices().getImcmsAuthenticatorAndUserAndRoleMapper().saveUser(user);
                    view = password_changed_confirmation_view;
                }
            }
        }

        render(request, response, view);
    }


    private static void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws IOException, ServletException {

        if (view == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            request.getRequestDispatcher("/WEB-INF/forgotpassword/" + view).forward(request, response);
        }
    }


    private static UserDomainObject getUserByPasswordResetId(HttpServletRequest request) {
        String resetId = StringUtils.trimToEmpty(request.getParameter(REQUEST_PARAM_RESET_ID));

        return Imcms.getServices().getImcmsAuthenticatorAndUserAndRoleMapper().getUserByPasswordResetId(resetId);
    }


    private Op getOp(HttpServletRequest request) {
        try {
            return Op.valueOf(StringUtils.trimToEmpty(request.getParameter(REQUEST_PARAM_OP)).toUpperCase());
        } catch (Exception e) {
            return Op.REQUEST_RESET;
        }
    }


    private void setValidationErrors(HttpServletRequest request, String first, String... rest) {
        List<String> errors = new LinkedList<String>();

        errors.add(first);

        for (String error: rest) {
            errors.add(error);
        }

        request.setAttribute(REQUEST_ATTR_VALIDATION_ERRORS, errors);
    }


    private void asyncSendPasswordResetEMail(final HttpServletRequest request, final UserDomainObject receiver, final String url) {
        emailSender.submit(new Runnable() {
            public void run() {
                try {
                    InputStream in = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/forgotpassword/email_template.en.txt");
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }

                    in.close();

                    String subject = "imcms.se Password Assistance -- DO NOT REPLAY --";
                    String body = String.format(sb.toString(), url);

            SystemData sysData = Imcms.getServices().getSystemData();
            String eMailServerMaster = sysData.getServerMasterAddress();
            SMTP smtp = Imcms.getServices().getSMTP();
            // imcode.util.Utility.isValidEmail(userEmail)
            smtp.sendMail(new SMTP.Mail( eMailServerMaster, new String[] { receiver.getEmailAddress() }, subject, body));

//                    Email email = new SimpleEmail();
//                    email.setHostName("smtp.gmail.com");
//                    email.setSmtpPort(587);
//                    email.setDebug(true);
//                    email.setAuthenticator(new DefaultAuthenticator(xxx@gmail.com", "xxx"));
//                    email.setTLS(true);
//                    email.setFrom("admin@imcms.se");
//                    email.setSubject(subject);
//                    email.setMsg(body);
//                    email.addTo("anton.josua@gmail.com");
//                    email.setDebug(true);
//                    email.send();
                    System.out.println("Sent!!");
                } catch (Exception e) {
                    System.out.println("Mail sending error: " + e);
                }
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("Shutting down!!");
        emailSender.shutdownNow();
    }
}