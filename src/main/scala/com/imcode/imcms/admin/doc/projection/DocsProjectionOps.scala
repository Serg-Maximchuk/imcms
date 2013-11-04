package com.imcode
package imcms
package admin.doc.projection

import imcode.server.document._
import _root_.imcode.server.document.textdocument.TextDocumentDomainObject
import com.imcode.imcms.admin.doc.{DocEditorDialog, DocViewer}
import com.imcode.imcms.vaadin.ui.dialog.{InformationDialog, ConfirmationDialog}

import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.server._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import com.imcode.imcms.mapping.DocumentMapper
import com.imcode.imcms.admin.doc.content.textdoc.NewTextDocContentEditor
import com.vaadin.ui.UI
import com.vaadin.server.Page

/**
 * Common operations associated with docs projection [selection] such as edit, view, delete etc.
 *
 * @param projection
 */
// todo: allow edit several at once
// todo: check permissions
class DocsProjectionOps(projection: DocsProjection) extends ImcmsServicesSupport with Log4jLoggerSupport {

  def mkDocOfType[T <: DocumentDomainObject : ClassTag] {
    whenSingleton(projection.selection) { ref =>
      imcmsServices.getDocumentMapper.getDefaultDocument(ref.metaId(), ref.language()) match {
        case selectedDoc: TextDocumentDomainObject =>
          val (newDocType, dlgCaption) = scala.reflect.classTag[T].runtimeClass match {
            case c if c == classOf[TextDocumentDomainObject] => DocumentTypeDomainObject.TEXT_ID -> "new_text_doc.dlg.title".i
            case c if c == classOf[FileDocumentDomainObject] => DocumentTypeDomainObject.FILE_ID -> "new_text_doc.dlg.title".i
            case c if c == classOf[UrlDocumentDomainObject] => DocumentTypeDomainObject.URL_ID -> "new_url_doc.dlg.title".i
            case c if c == classOf[HtmlDocumentDomainObject] => DocumentTypeDomainObject.HTML_ID -> "new_html_doc.dlg.title".i
          }

          val newDoc = imcmsServices.getDocumentMapper.createDocumentOfTypeFromParent(newDocType, selectedDoc, UI.getCurrent.imcmsUser)

          new DocEditorDialog(dlgCaption, newDoc) |>> { dlg =>
            dlg.setOkButtonHandler {
              dlg.docEditor.collectValues() match {
                case Left(errors) =>
                  Page.getCurrent.showErrorNotification(errors.mkString(", "))

                case Right((editedDoc, i18nMetas)) =>
                  val saveOpts = dlg.docEditor.contentEditor match {
                    case contentEditor: NewTextDocContentEditor if contentEditor.ui.chkCopyI18nMetaTextsToTextFields.checked =>
                      java.util.EnumSet.of(DocumentMapper.SaveOpts.CopyI18nMetaTextsIntoTextFields)

                    case _ =>
                      java.util.EnumSet.noneOf(classOf[DocumentMapper.SaveOpts])
                  }

                  imcmsServices.getDocumentMapper.saveNewDocument(
                    editedDoc,
                    i18nMetas.values.to[Set].asJava,
                    saveOpts,
                    UI.getCurrent.imcmsUser
                  )
                  Page.getCurrent.showInfoNotification("New document has been saved")
                  projection.reload()
                  dlg.close()
              }
            }
          } |> UI.getCurrent.addWindow

        case _ =>
          new InformationDialog("Please select a text document/profile") |>> UI.getCurrent.addWindow
      }
    }
  }


  def deleteSelectedDocs() {
    whenNotEmpty(projection.selection) { refs =>
      new ConfirmationDialog("Delete selected document(s)?") |>> { dlg =>
        dlg.setOkButtonHandler {
          try {
            refs.foreach(ref => imcmsServices.getDocumentMapper.deleteDocument(ref.metaId(), UI.getCurrent.imcmsUser))
            Page.getCurrent.showInfoNotification("Documents has been deleted")
            dlg.close()
          } finally {
            projection.reload()
          }
        }
      } |> UI.getCurrent.addWindow
    }
  }


  def showSelectedDoc() {
    whenSingleton(projection.selection) { ref =>
      DocViewer.showDocViewDialog(projection.ui, ref.metaId())
    }
  }


  def copySelectedDoc() {
    whenSingleton(projection.selection) { ref =>
      imcmsServices.getDocumentMapper.copyDocument(ref.docRef(), UI.getCurrent.imcmsUser)
      projection.reload()
      Page.getCurrent.showInfoNotification("Document has been copied")
    }
  }


  def editSelectedDoc() {
    whenSingleton(projection.selection) { ref =>
      val page = Page.getCurrent
      val doc: DocumentDomainObject = imcmsServices.getDocumentMapper.getWorkingDocument(ref.metaId(), ref.language())

      new DocEditorDialog(s"Edit document ${doc.getMetaId}", doc) |>> { dlg =>
        dlg.setOkButtonHandler {
          dlg.docEditor.collectValues() match {
            case Left(errors) =>
              page.showErrorNotification("Unable to save document", errors.mkString(", "))

            case Right((editedDoc, i18nMetas)) =>
              imcmsServices.getDocumentMapper.saveDocument(editedDoc, i18nMetas.values.to[Set].asJava, UI.getCurrent.imcmsUser)
              page.showInfoNotification("Document has been saved")
              projection.reload()
              dlg.close()
          }
        }
      } |> UI.getCurrent.addWindow
    }
  }
}