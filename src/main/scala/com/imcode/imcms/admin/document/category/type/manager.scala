package com.imcode
package imcms.admin.document.category.`type`

import scala.collection.JavaConversions._
import com.vaadin.ui._
import imcode.server.user._
import imcode.server.{Imcms}
import com.imcode.imcms.vaadin.{ContainerProperty => CP, _}
import imcode.server.document.{CategoryTypeDomainObject}
import com.imcode.imcms.admin.document.category.{CategoryTypeId}
import com.vaadin.ui.Window.Notification

class CategoryTypeManager(app: ImcmsApplication) {
  private val categoryMapper = Imcms.getServices.getCategoryMapper

  val ui = letret(new CategoryTypeManagerUI) { ui =>
    ui.tblTypes.itemsProvider = () => {
      for {
        vo <- categoryMapper.getAllCategoryTypes
        id = Int box vo.getId
      } yield {
        id -> Seq(id, vo.getName, Boolean box (vo.getMaxChoices > 0), Boolean box vo.isInherited, Boolean box vo.isImageArchive)
      }
    }

    ui.rc.btnReload addListener block { reload() }
    ui.tblTypes addListener block { handleSelection() }

    ui.miNew setCommand block { editInPopUp(new CategoryTypeDomainObject) }
    ui.miEdit setCommand block {
      whenSelected(ui.tblTypes) { id =>
        categoryMapper.getCategoryTypeById(id.intValue) match {
          case null => reload()
          case vo => editInPopUp(vo)
        }
      }
    }
    ui.miDelete setCommand block {
      whenSelected(ui.tblTypes) { id =>
        app.initAndShow(new ConfirmationDialog("Delete category type")) { dlg =>
          dlg addOkHandler {
            ?(categoryMapper getCategoryTypeById id.intValue) foreach { vo =>
              if (canManage) categoryMapper deleteCategoryTypeFromDb vo
              else error("NO PERMISSIONS")
            }
            reload()
          }
        }
      }
    }
  } // val ui

  reload()
  // END OF PRIMARY CONSTRUCTOR

  def canManage = app.user.isSuperAdmin

  /** Edit in modal dialog. */
  private def editInPopUp(vo: CategoryTypeDomainObject) {
    val id = vo.getId
    val isNew = id == 0
    val dialogTitle = if(isNew) "Create new category type" else "Edit category type"

    app.initAndShow(new OkCancelDialog(dialogTitle)) { dlg =>
      let(dlg.setMainContent(new CategoryTypeDialogContentUI(app))) { c =>
        c.txtId.value = if (isNew) "" else id.toString
        c.txtName.value = ?(vo.getName) getOrElse ""
        c.chkImageArchive.value = Boolean box vo.isImageArchive
        c.chkInherited.value = Boolean box vo.isInherited
        c.chkMultiSelect.value = Boolean box vo.isMultiselect

        dlg addOkHandler {
          let(vo.clone()) { voc =>
            voc setName c.txtName.value.trim
            voc setInherited c.chkInherited.booleanValue
            voc setImageArchive c.chkImageArchive.booleanValue
            voc setMultiselect c.chkMultiSelect.booleanValue

            // todo: move validate into separate fn
            val validationError: Option[String] = voc.getName match {
              case "" => ?("Category type name is not set")
              case name => ?(categoryMapper.getCategoryTypeByName(name)) collect {
                case categoryType if categoryType.getId != voc.getId =>
                  "Category type with such name already exists"
              }
            }

            validationError foreach { msg =>
              app.getMainWindow.showNotification(msg, Notification.TYPE_WARNING_MESSAGE)
              error(msg)
            }

            if (!canManage) {
              app.getMainWindow.showNotification("You are not allowed to manage categories types", Notification.TYPE_ERROR_MESSAGE)
            } else {
              EX.allCatch.either(categoryMapper saveCategoryType voc) match {
                case Left(ex) =>
                  // todo: log ex, provide custom dialog with details -> show stack
                  app.getMainWindow.showNotification("Internal error, please contact your administrator", Notification.TYPE_ERROR_MESSAGE)
                  throw ex
                case _ =>
                  let(if (isNew) "New category type has been added" else "Category type has been updated") { msg =>
                    app.getMainWindow.showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE)
                  }

                  reload()
              }
            }
          }
        }
      }
    }
  } // editInPopUp

  private def reload() {
    ui.tblTypes.reload()

    let(canManage) { canManage =>
      ui.tblTypes.setSelectable(canManage)
      forlet[{def setEnabled(e: Boolean)}](ui.mb, ui.miNew, ui.miEdit, ui.miDelete) { _ setEnabled canManage }
    }

    handleSelection()
  }

  private def handleSelection() {
    let(canManage && ui.tblTypes.isSelected) { isSelected =>
      ui.miEdit.setEnabled(isSelected)
      ui.miDelete.setEnabled(isSelected)
    }
  }
}

class CategoryTypeManagerUI extends VerticalLayout with Spacing with UndefinedSize {
  val mb = new MenuBar
  val miNew = mb.addItem("New", null)
  val miEdit = mb.addItem("Edit", null)
  val miDelete = mb.addItem("Delete", null)
  val tblTypes = new Table with Reloadable with SingleSelect2[CategoryTypeId] with Selectable with Immediate
  val rc = new ReloadableContentUI(tblTypes)

  addContainerProperties(tblTypes,
    CP[JInteger]("Id"),
    CP[String]("Name"),
    CP[JBoolean]("Multi select?"),
    CP[JBoolean]("Inherited to new documents?"),
    CP[JBoolean]("Used by image archive?"))

  addComponents(this, mb, rc)
}


class CategoryTypeDialogContentUI(app: ImcmsApplication) extends FormLayout with UndefinedSize {
  val txtId = new TextField("Id") with Disabled
  val txtName = new TextField("Name") with Required
  val chkMultiSelect = new CheckBox("Multiselect")
  val chkInherited = new CheckBox("Inherited to new documents")
  val chkImageArchive = new CheckBox("Used by image archive")

  addComponents(this, txtId, txtName, chkMultiSelect, chkInherited, chkImageArchive)
}


