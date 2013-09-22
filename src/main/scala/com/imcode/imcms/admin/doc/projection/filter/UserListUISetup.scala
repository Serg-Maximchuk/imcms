package com.imcode
package imcms
package admin.doc.projection.filter

import scala.collection.JavaConverters._
import com.imcode.imcms.admin.access.user.{UserMultiSelectDialog}
import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.data._
import com.imcode.imcms.vaadin.event._
import com.vaadin.ui.UI

trait UserListUISetup { this: UserListUI =>
  val projectionDialogCaption: String

  chkEnabled.addValueChangeHandler { _ =>
    Seq(lstUsers, lytButtons).foreach(_.setEnabled(chkEnabled.value))
  }

  btnAdd.addClickHandler { _ =>
    new UserMultiSelectDialog |>> { dlg =>
      dlg.setOkButtonHandler {
        for (user <- dlg.search.selection) lstUsers.addItem(user.getId: JInteger, "#" + user.getLoginName)
      }
    } |> UI.getCurrent.addWindow
  }

  btnRemove.addClickHandler { _ =>
    lstUsers.value.asScala.foreach(lstUsers.removeItem)
  }
}