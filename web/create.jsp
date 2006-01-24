<%@ page import="com.imcode.imcms.api.ContentManagementSystem"%><%@ page import="com.imcode.imcms.api.TextDocumentViewing"%><%@ page import="com.imcode.imcms.api.TextDocument"%><%@ page import="com.imcode.imcms.api.DocumentService"%><%
    ContentManagementSystem cms = ContentManagementSystem.fromRequest(request);
    DocumentService documentService = cms.getDocumentService();
    TextDocument textDocument = documentService.getTextDocument(1001) ;
    for (int i = 0; i < 1000; ++i) {
        TextDocument newTextDocument = documentService.createNewTextDocument(textDocument);
        newTextDocument.setHeadline("test");
        documentService.saveChanges(newTextDocument);
    }

    request.getRequestDispatcher( "/servlet/StartDoc" ).forward( request, response ); %>