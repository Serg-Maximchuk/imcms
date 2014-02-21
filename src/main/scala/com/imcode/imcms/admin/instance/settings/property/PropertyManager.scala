package com.imcode
package imcms.admin.instance.settings.property

import com.imcode.imcms.vaadin.Current
import scala.util.control.{Exception => Ex}
import imcms.security.{PermissionGranted, PermissionDenied}
import imcode.server.Imcms
import com.imcode.imcms.vaadin.component._
import com.imcode.imcms.vaadin.data._
import com.imcode.imcms.vaadin.component.dialog._
import com.imcode.imcms.vaadin.server._

//todo: move to system dir + monitor
// todo: updateReadOnly ->

class PropertyManager {

  val view = new PropertyManagerView |>> { w =>
    w.miReload.setCommandHandler { _ => reload() }
    w.miEdit.setCommandHandler { _ =>
      new OkCancelDialog("Edit system properties") |>> { dlg =>
        dlg.mainComponent = new PropertyEditorView |>> { eui =>
          Imcms.getServices.getSystemData |> { d =>
            eui.txtStartPageNumber.value = d.getStartDocument.toString
            eui.txaSystemMessage.value = d.getSystemMessage
            eui.webMaster.txtName.value = d.getWebMaster
            eui.webMaster.txtEmail.value = d.getWebMasterAddress
            eui.serverMaster.txtName.value = d.getServerMaster
            eui.serverMaster.txtEmail.value = d.getServerMasterAddress
          }

          dlg.setOkButtonHandler {
            Current.ui.privileged(permission) {
              val systemData = Imcms.getServices.getSystemData |>> { d =>
                d.setStartDocument(eui.txtStartPageNumber.value.toInt)
                d.setSystemMessage(eui.txaSystemMessage.value)
                d.setWebMaster(eui.webMaster.txtName.value)
                d.setWebMasterAddress(eui.webMaster.txtEmail.value)
                d.setServerMaster(eui.serverMaster.txtName.value)
                d.setServerMasterAddress(eui.serverMaster.txtEmail.value)
              }

              Ex.allCatch.either(Imcms.getServices.setSystemData(systemData)) match {
                case Right(_) =>
                  Current.page.showInfoNotification("System properties has been updated")
                  reload()
                case Left(ex) =>
                  Current.page.showErrorNotification("Internal error")
                  throw ex
              }
            }
          }
        }
      } |> Current.ui.addWindow
    }
  }

  reload()
  // END OF PRIMARY CONSTRUCTOR

  def canManage = Current.imcmsUser.isSuperAdmin
  def permission = if (canManage) PermissionGranted else PermissionDenied("No permissions to manage system properties")

  def reload() {
    import view.propertyEditorView._

    Seq(txtStartPageNumber, txaSystemMessage, webMaster.txtName, webMaster.txtEmail, serverMaster.txtName,
      serverMaster.txtEmail).foreach { txt =>
      txt.setReadOnly(false)
    }

    Imcms.getServices.getSystemData |> { d =>
      txtStartPageNumber.value = d.getStartDocument.toString
      txaSystemMessage.value = d.getSystemMessage
      webMaster.txtName.value = d.getWebMaster
      webMaster.txtEmail.value = d.getWebMasterAddress
      serverMaster.txtName.value = d.getServerMaster
      serverMaster.txtEmail.value = d.getServerMasterAddress
    }

    Seq(txtStartPageNumber, txaSystemMessage, webMaster.txtName, webMaster.txtEmail, serverMaster.txtName,
      serverMaster.txtEmail).foreach { txt =>
      txt.setReadOnly(true)
    }

    Seq(view.miEdit).foreach(_.setEnabled(canManage))
  }
}