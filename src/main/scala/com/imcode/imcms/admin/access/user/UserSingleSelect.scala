package com.imcode
package imcms
package admin.access.user

import java.util.concurrent.atomic.AtomicReference
import _root_.imcode.server.user.UserDomainObject
import com.vaadin.ui.UI
import com.imcode.imcms.vaadin.data._

class UserSingleSelect {
  private val selectionRef = new AtomicReference(Option.empty[UserDomainObject])
  val ui = new UserSingleSelectUI |>> { ui =>
    ui.btnSelect.addClickHandler { _ =>
      new UserSingleSelectDialog |>> { dlg =>
        dlg.setOkButtonHandler {
          selection = dlg.search.selection.headOption
          dlg.close()
        }
      } |> UI.getCurrent.addWindow
    }

    ui.btnClear.addClickHandler { _ => selection = None }
  }

  def selection: Option[UserDomainObject] = selectionRef.get
  def selection_=(userOpt: Option[UserDomainObject]) {
    ui.btnClear.setEnabled(userOpt.isDefined)
    ui.lblName.value = userOpt match {
      case Some(user) => s"[ <b>${user.getLoginName}</b> ]"
      case _ => "[ <i>not selected</i> ]"
    }

    selectionRef.set(userOpt)
  }

  selection = None
}
