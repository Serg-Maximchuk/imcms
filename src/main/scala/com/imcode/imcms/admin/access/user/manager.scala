package com.imcode
package imcms
package admin.access.user

import imcode.server.user._
import com.vaadin.ui._

import com.imcode.imcms.vaadin._
import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.ui.dialog._
import com.imcode.imcms.vaadin.data._

import scala.collection.JavaConverters._
import com.imcode.imcms.admin.access.user.projection.UsersProjection

// todo add security check, add editAndSave, add external UI
class UserManager(app: ImcmsUI) extends ImcmsServicesSupport {
  private val search = new UsersProjection

  val ui = new UserManagerUI(search.ui) |>> { ui =>
    val roleMapper = imcmsServices.getImcmsAuthenticatorAndUserAndRoleMapper

    ui.miNew.setCommandHandler {
      new OkCancelDialog("user.dlg.new.caption".i) |>> { dlg =>
        dlg.mainUI = new UserEditorUI |>> { c =>
          for (role <- roleMapper.getAllRoles if role.getId != RoleId.USERS) {
            c.tcsRoles.addItem(role.getId, role.getName)
          }

          imcmsServices.getLanguageMapper.getDefaultLanguage |> { l =>
            c.sltUILanguage.addItem(l)
            c.sltUILanguage.select(l)
          }

          c.chkActivated.setValue(true)

          dlg.setOkButtonHandler {
            new UserDomainObject |> { u =>
              u setActive c.chkActivated.booleanValue
              u setFirstName c.txtFirstName.value
              u setLastName c.txtLastName.value
              u setLoginName c.txtLogin.value
              u setPassword c.txtPassword.value
              u setRoleIds c.tcsRoles.value.asScala.toArray
              u setLanguageIso639_2 c.sltUILanguage.value

              roleMapper.addUser(u)
              search.reset()
            }
          }
        }
      } |> UI.getCurrent.addWindow
    }

    ui.miEdit.setCommandHandler {
      whenSingle(search.selection) { user =>
        new OkCancelDialog("user.dlg.edit.caption".f(user.getLoginName)) |>> { dlg =>
          dlg.mainUI = new UserEditorUI |>> { c =>
            c.chkActivated setValue user.isActive
            c.txtFirstName setValue user.getFirstName
            c.txtLastName setValue user.getLastName
            c.txtLogin setValue user.getLoginName
            c.txtPassword setValue user.getPassword

            for (role <- roleMapper.getAllRoles if role.getId != RoleId.USERS) {
              c.tcsRoles.addItem(role.getId, role.getName)
            }

            c.tcsRoles.value = user.getRoleIds.filterNot(RoleId.USERS ==).toSeq.asJava

            imcmsServices.getLanguageMapper.getDefaultLanguage |> { l =>
              c.sltUILanguage.addItem(l)
            }

            c.sltUILanguage.select(user.getLanguageIso639_2)

            dlg.setOkButtonHandler {
              user.setActive(c.chkActivated.booleanValue)
              user.setFirstName(c.txtFirstName.value)
              user.setLastName(c.txtLastName.value)
              user.setLoginName(c.txtLogin.value)
              user.setPassword(c.txtPassword.value)
              user.setRoleIds(c.tcsRoles.value.asScala.toArray)
              user.setLanguageIso639_2(c.sltUILanguage.value)

              roleMapper.saveUser(user)
              search.reset()
            }
          }
        } |> UI.getCurrent.addWindow
      }
    }

    search.listen { ui.miEdit setEnabled _.size == 1 }
    search.notifyListeners()
  }
}


class UserManagerUI(val searchUI: Component) extends VerticalLayout with Spacing {
  import Theme.Icon._

  val mb = new MenuBar
  val miNew = mb.addItem("mi.new".i, New16)
  val miEdit = mb.addItem("mi.edit".i, Edit16)
  val miHelp = mb.addItem("mi.help".i, Help16)

  this.addComponents(mb, searchUI)
}


/**
 * Add/Edit user dialog content.
 */
class UserEditorUI extends FormLayout with UndefinedSize {
  val txtLogin = new TextField("user.editor.frm.fld.txt_login".i)
  val txtPassword = new PasswordField("user.editor.frm.fld.pwd_password".i)
  val txtVerifyPassword = new PasswordField("user.editor.frm.fld.pwd_password_retype".i)
  val txtFirstName = new TextField("user.editor.frm.fld.txt_first_name".i)
  val txtLastName = new TextField("user.editor.frm.fld.txt_last_name".i)
  val chkActivated = new CheckBox("user.editor.frm.fld.chk_activated".i)
  val tcsRoles = new TwinColSelect("user.editor.frm.fld.tcs_roles".i) with MultiSelect[RoleId] with TCSDefaultI18n
  val sltUILanguage = new ComboBox("user.editor.frm.fld.interface_language".i) with SingleSelect[String] with NoNullSelection
  val txtEmail = new TextField("user.editor.frm.fld.email".i)

  val lytPassword = new HorizontalLayoutUI("user.editor.frm.fld.password".i) with UndefinedSize {
      addComponent(txtPassword)
      addComponent(txtVerifyPassword)
  }

  val lytName = new HorizontalLayoutUI("user.editor.frm.fld.name".i) with UndefinedSize {
      addComponent(txtFirstName)
      addComponent(txtLastName)
  }

  val lytLogin = new HorizontalLayoutUI("user.editor.frm.fld.account".i) with UndefinedSize {
    this.addComponents(txtLogin, chkActivated)
    setComponentAlignment(chkActivated, Alignment.BOTTOM_LEFT)
  }

  val btnEditContacts = new Button("user.editor.frm.fld.btn_edit_contacts".i) with LinkStyle with Disabled

  val lytContacts = new HorizontalLayoutUI("user.editor.frm.fld.contacts".i) with UndefinedSize {
    addComponent(btnEditContacts)
  }

  doto(txtLogin, txtPassword, txtVerifyPassword, txtEmail) { _ setRequired true }

  this.addComponents(lytLogin, lytPassword, lytName, txtEmail, sltUILanguage, tcsRoles, lytContacts)
}