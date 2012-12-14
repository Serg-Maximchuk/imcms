package com.imcode
package imcms.admin.instance.settings.property

import scala.util.control.{Exception => Ex}
import scala.collection.JavaConversions._
import com.vaadin.ui._
import com.imcode.imcms.vaadin.{_}
import imcms.security.{PermissionGranted, PermissionDenied}
import imcode.util.Utility.{ipLongToString, ipStringToLong}
import com.vaadin.ui.Window.Notification
import imcode.server.{SystemData, Imcms}
import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.ui.dialog._

//todo: move to system dir + monitor
// todo: updateReadOnly ->

class PropertyManagerManager(app: ImcmsApplication) {

  val ui = new PropertyManagerUI |>> { ui =>
    ui.rc.btnReload addClickHandler { reload() }
    ui.miEdit setCommandHandler {
      app.getMainWindow.initAndShow(new OkCancelDialog("Edit system properties")) { dlg =>
        dlg.mainUI = new PropertyEditorUI |>> { eui =>
          Imcms.getServices.getSystemData |> { d =>
            eui.txtStartPageNumber.value = d.getStartDocument.toString
            eui.txaSystemMessage.value = d.getSystemMessage
            eui.webMasterUI.txtName.value = d.getWebMaster
            eui.webMasterUI.txtEmail.value = d.getWebMasterAddress
            eui.serverMasterUI.txtName.value = d.getServerMaster
            eui.serverMasterUI.txtEmail.value = d.getServerMasterAddress
          }

          dlg.setOkButtonHandler {
            app.privileged(permission) {
              val systemData = Imcms.getServices.getSystemData |>> { d =>
                d setStartDocument eui.txtStartPageNumber.value.toInt
                d setSystemMessage eui.txaSystemMessage.value
                d setWebMaster eui.webMasterUI.txtName.value
                d setWebMasterAddress eui.webMasterUI.txtEmail.value
                d setServerMaster eui.serverMasterUI.txtName.value
                d setServerMasterAddress eui.serverMasterUI.txtEmail.value
              }

              Ex.allCatch.either(Imcms.getServices.setSystemData(systemData)) match {
                case Right(_) =>
                  app.getMainWindow.showInfoNotification("System properties has been updated")
                  reload()
                case Left(ex) =>
                  app.getMainWindow.showErrorNotification("Internal error")
                  throw ex
              }
            }
          }
        }
      }
    }
  }

  reload()
  // END OF PRIMARY CONSTRUCTOR

  def canManage = app.imcmsUser.isSuperAdmin
  def permission = if (canManage) PermissionGranted else PermissionDenied("No permissions to manage system properties")

  def reload() {
    import ui.dataUI._

    doto(txtStartPageNumber, txaSystemMessage, webMasterUI.txtName, webMasterUI.txtEmail, serverMasterUI.txtName, serverMasterUI.txtEmail) { _ setReadOnly false}
    Imcms.getServices.getSystemData |> { d =>
      txtStartPageNumber.value = d.getStartDocument.toString
      txaSystemMessage.value = d.getSystemMessage
      webMasterUI.txtName.value = d.getWebMaster
      webMasterUI.txtEmail.value = d.getWebMasterAddress
      serverMasterUI.txtName.value = d.getServerMaster
      serverMasterUI.txtEmail.value = d.getServerMasterAddress
    }
    doto(txtStartPageNumber, txaSystemMessage, webMasterUI.txtName, webMasterUI.txtEmail, serverMasterUI.txtName, serverMasterUI.txtEmail) { _ setReadOnly true}

    doto(ui.miEdit) { _ setEnabled canManage }
  }
} // class PropertyManager

class PropertyManagerUI extends VerticalLayout with Spacing with UndefinedSize {
  import Theme.Icon._

  val mb = new MenuBar
  val miEdit = mb.addItem("Edit", Edit16)
  val miHelp = mb.addItem("Help", Help16)
  val dataUI = new PropertyEditorUI
  val dataPanel = new Panel(new VerticalLayout with UndefinedSize with Margin) with UndefinedSize
  val rc = new ReloadableContentUI(dataPanel)

  dataPanel.addComponent(dataUI)
  addComponentsTo(this, mb, rc)
}


class PropertyEditorUI extends FormLayout with UndefinedSize {
  class ContactUI(caption: String) extends VerticalLayout() with UndefinedSize with Spacing {
    val txtName = new TextField("Name")
    val txtEmail = new TextField("e-mail")

    setCaption(caption)
    addComponentsTo(this, txtName, txtEmail)
  }

  val txtStartPageNumber = new TextField("Start page number")
  val txaSystemMessage = new TextArea("System message") |>> { _.setRows(5) }
  val serverMasterUI = new ContactUI("Server master")
  val webMasterUI = new ContactUI("Web master")

  addComponentsTo(this, txtStartPageNumber, txaSystemMessage, serverMasterUI, webMasterUI)
}