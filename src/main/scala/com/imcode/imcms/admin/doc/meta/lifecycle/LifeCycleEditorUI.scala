package com.imcode
package imcms
package admin.doc.meta.lifecycle

import com.imcode.imcms.admin.access.user.{UserSingleSelectUI, UserSingleSelect}
import com.imcode.imcms.api.Document
import com.vaadin.ui._
import com.imcode.imcms.vaadin.ui._


class LifeCycleEditorUI extends VerticalLayout with Spacing with FullWidth {

  class DateUI(caption: String, ussUI: UserSingleSelectUI) extends HorizontalLayoutUI(caption, margin = false) {
    val calDate = new PopupDateField with MinuteResolution with Now
    val lblBy = new Label("by") with UndefinedSize

    this.addComponents(calDate, lblBy, ussUI)
  }

  object info {
    val ussCreator = new UserSingleSelect
    val ussModifier = new UserSingleSelect

    val dCreated = new DateUI("Created", ussCreator.ui)
    val dModified = new DateUI("Modified", ussModifier.ui)
  }

  object publication {
    val ussPublisher = new UserSingleSelect

    val sltStatus = new Select("doc_publication_status".i) with SingleSelect[Document.PublicationStatus] with NoNullSelection with Immediate {
      addItem(Document.PublicationStatus.NEW, "doc_publication_status.new".i)
      addItem(Document.PublicationStatus.DISAPPROVED, "doc_publication_status.disapproved".i)
      addItem(Document.PublicationStatus.APPROVED, "doc_publication_status.approved".i)
    }

    val lytPhase = new HorizontalLayout with Spacing with UndefinedSize |>> { lyt =>
      lyt.setCaption("Status")
      lyt.setStyleName("im-border-top-doc_lifecycle")
    }

    val sltVersion = new Select("Version") with SingleSelect[DocVersionNo] with NoNullSelection

    val calStart = new PopupDateField with MinuteResolution with Immediate with Now
    val calArchive = new PopupDateField with MinuteResolution with Immediate
    val calEnd = new PopupDateField with MinuteResolution with Immediate
    val chkStart = new CheckBox("start") with Checked with ReadOnly // decoration, always read-only
    val chkArchive = new CheckBox("archive") with Immediate with AlwaysFireValueChange
    val chkEnd = new CheckBox("expiration") with Immediate with AlwaysFireValueChange

    ussPublisher.ui.setCaption("Publisher")
  }

  private val pnlInfo = new Panel("Info") with FullWidth {
    val content = new FormLayout with Margin with FullWidth
    setContent(content)

    content.addComponents(info.dCreated, info.dModified)
  }

  private val pnlPublication = new Panel("Publication") with FullWidth {
    val content = new FormLayout with Margin with FullWidth
    setContent(content)

    val lytDate = new GridLayout(2, 2) with Spacing {
      setCaption("Date")

      this.addComponents(
        publication.chkStart, publication.calStart,
        publication.chkArchive, publication.calArchive,
        publication.chkEnd, publication.calEnd
      )
    }

    content.addComponents(publication.sltStatus, publication.sltVersion, lytDate, publication.ussPublisher.ui, publication.lytPhase)
  }

  this.addComponents(pnlInfo, pnlPublication)
}