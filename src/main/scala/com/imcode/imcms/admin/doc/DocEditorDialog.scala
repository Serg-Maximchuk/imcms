package com.imcode
package imcms
package admin.doc

import _root_.imcode.server.document.DocumentDomainObject
import com.imcode.imcms.vaadin.component._
import com.imcode.imcms.vaadin.component.dialog._
import com.vaadin.ui.VerticalLayout


class DocEditorDialog(caption: String, doc: DocumentDomainObject) extends OkCancelDialog(caption) with OKCaptionIsSave
    with CustomSizeDialog with BottomContentMarginDialog with Resizable {

  val docEditor = new DocEditor(doc)

  mainComponent = new VerticalLayout(docEditor.view) with FullSize |>> { _.addStyleName("doc_editor") }

  this.setSize(750, 600)
}


