package com.imcode
package imcms
package admin.doc.projection

import _root_.imcode.server.document.{UrlDocumentDomainObject, FileDocumentDomainObject, DocumentTypeDomainObject, DocumentDomainObject}
import _root_.imcode.server.document.textdocument.TextDocumentDomainObject
import com.imcode.imcms.admin.doc.{DocEditorDialog, DocViewer, DocEditor}
import com.imcode.imcms.vaadin.ui.dialog.ConfirmationDialog
import com.imcode.imcms.vaadin._
import com.imcode.imcms.vaadin.ui._
import scala.collection.JavaConverters._

// todo: add callbacks???
class DocsProjectionOps(projection: DocsProjection) extends ImcmsServicesSupport with Log4jLoggerSupport {

  def mkDocOfType[T <: DocumentDomainObject : ClassManifest] {
    whenSingle(projection.selection) { selectedDocId =>
      imcmsServices.getDocumentMapper.getDocument(selectedDocId) match {
        case selectedDoc: TextDocumentDomainObject =>
          val (newDocType, dlgCaption) = classManifest[T].erasure match {
            case c if c == classOf[TextDocumentDomainObject] => DocumentTypeDomainObject.TEXT_ID -> "New text document"
            case c if c == classOf[FileDocumentDomainObject] => DocumentTypeDomainObject.FILE_ID -> "New file document"
            case c if c == classOf[UrlDocumentDomainObject] => DocumentTypeDomainObject.URL_ID -> "New url document"
          }
          val newDoc = imcmsServices.getDocumentMapper.createDocumentOfTypeFromParent(newDocType, selectedDoc, projection.ui.getApplication.imcmsUser)

          import projection.ui

          new DocEditorDialog(dlgCaption, newDoc) |>> { dlg =>
            dlg.setOkButtonHandler {
              dlg.docEditor.collectValues() match {
                case Left(errors) => ui.rootWindow.showErrorNotification(errors.mkString(","))
                case Right((editedDoc, i18nMetas)) =>
                  try {
                    imcmsServices.getDocumentMapper.saveNewDocument(editedDoc, i18nMetas.asJava, ui.getApplication.imcmsUser)
                    ui.rootWindow.showInfoNotification("New document has been created")
                    projection.filter()
                  } catch {
                    case e => ui.rootWindow.showErrorNotification("Failed to create new document", e.getStackTraceString)
                  }
              }
            }
          } |> ui.rootWindow.addWindow

        case _ =>
      }
    }
  }


  def deleteSelectedDocs() {
    whenNotEmpty(projection.selection) { docIds =>
      projection.ui.rootWindow.initAndShow(new ConfirmationDialog("Delete selected document(s)?")) { dlg =>
        dlg.setOkButtonHandler {
          try {
            for {
              docId <- docIds
              doc <- imcmsServices.getDocumentMapper.getDocument(docId) |> opt
            } {
              imcmsServices.getDocumentMapper.deleteDocument(doc, projection.ui.getApplication.imcmsUser)
            }
            projection.ui.rootWindow.showInfoNotification("Document(s) deleted")
          } catch {
            case e =>
              logger.error("Document delete error", e)
              projection.ui.rootWindow.showErrorNotification("Error deleging document(s)", e.getStackTraceString)
          } finally {
            // todo: update ranges ???
            projection.filter()
          }
        }
      }
    }
  }

  def showSelectedDoc() {
    whenSingle(projection.selection) { docId =>
      DocViewer.showDocViewDialog(projection.ui, docId)
    }
  }

  def copySelectedDoc() {
    whenSingle(projection.selection) { docId =>
      // todo copy selected document VERSION, not working???
      // dialog with drop down???? -> version select
      imcmsServices.getDocumentMapper.copyDocument(imcmsServices.getDocumentMapper.getWorkingDocument(docId), projection.ui.getApplication.imcmsUser)
      projection.filter()
      projection.ui.rootWindow.showInfoNotification("Document has been copied")
    }
  }

  // todo: allow change several at once???
  // todo: permissions
  def editSelectedDoc() {
    whenSingle(projection.selection) { docId =>
      imcmsServices.getDocumentMapper.getDocument(docId) match {
        case null =>
        case doc =>
          val rootWindow = projection.ui.rootWindow
          new DocEditorDialog("Edit document", doc) { dlg =>
            dlg.setOkButtonHandler {
              dlg.docEditor.collectValues() match {
                case Left(errors) => rootWindow.showErrorNotification(errors.mkString(","))
                case Right((editedDoc, i18nMetas)) =>
                  try {
                    imcmsServices.getDocumentMapper.saveDocument(editedDoc, i18nMetas.asJava, rootWindow.getApplication.imcmsUser)
                    rootWindow.showInfoNotification("Document has been saved")
                    projection.filter()
                  } catch {
                    case e => rootWindow.showErrorNotification("Failed to save document", e.getStackTraceString)
                  }
              }
            }
          } |> rootWindow.addWindow
      }
    }
  }
}
