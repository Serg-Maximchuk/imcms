package com.imcode
package imcms
package admin.docadmin.loop

import com.imcode.imcms.vaadin.component._
import com.vaadin.ui._

class LoopEditorView extends CustomComponent with FullSize {

  val lytEntries = new VerticalLayout with Margin with Spacing
  val pnlEntries = new Panel(lytEntries) with FullSize

  val mb = new MenuBar
  val miAddFirst = mb.addItem("Add first")
  val miAddLast = mb.addItem("Add last")
  val miClear = mb.addItem("Clear")
  val miHelp = mb.addItem("Help")

  setCompositionRoot(new VerticalLayout(mb, pnlEntries) with Spacing)
}
