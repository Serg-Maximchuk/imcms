package com.imcode
package imcms
package admin.doc.content

import scala.collection.mutable.{Map => MMap}
import scala.collection.breakOut
import scala.collection.JavaConversions._
import com.imcode.imcms.dao.{MetaDao, SystemDao, LanguageDao, IPAccessDao}
import com.imcode.imcms.api._
import imcode.server.user._
import imcode.server.{Imcms}
import imcode.server.document._
import com.imcode.imcms.vaadin._

import java.net.{MalformedURLException, URL}
import imcode.server.document.FileDocumentDomainObject.FileDocumentFile
import textdocument.TextDocumentDomainObject
import java.util.{EnumSet}
import imcms.mapping.DocumentMapper.SaveOpts
import imcms.mapping.{DocumentMapper, DocumentSaver}
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import java.io.{FileInputStream, ByteArrayInputStream}
import com.imcode.imcms.admin.instance.file.{UploadedFile, FileUploaderDialog}
import scala.collection.immutable.ListMap
import imcode.util.io.{FileInputStreamSource, InputStreamSource}
import com.vaadin.ui.Table.ColumnGenerator
import com.vaadin.terminal.{ThemeResource, ExternalResource}
import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.ui.dialog._
import com.vaadin.ui._


trait DocContentEditor extends Editor {
  type Data <: DocumentDomainObject
}


class TextDocContentEditor(doc: TextDocumentDomainObject) extends DocContentEditor {
  type Data = TextDocumentDomainObject

  val ui = new TextDocContentEditorUI |>> { ui =>
  } //ui

  def resetValues() {}

  def collectValues() = Right(doc)
}







/**
 * Used with deprecated docs such as Browser.
 */
class UnsupportedDocContentEditor(doc: DocumentDomainObject) extends DocContentEditor {
  type Data = DocumentDomainObject

  val ui = new UnsupportedDocContentEditorUI

  def resetValues() {}

  def collectValues() = Right(doc)
}


//case class MimeType(name: String, displayName: String)






/**
 *  Unsupported document editor UI
 */
class UnsupportedDocContentEditorUI extends Panel("Unsupported".i) with FullWidth {
  private val content = new VerticalLayout with FullWidth with Spacing with Margin
  val lblInfo = new Label("Unsupported document type".i)

  setContent(content)

  addComponentsTo(content, lblInfo)
}



class TextDocContentEditorUI extends VerticalLayout with FullSize with Spacing with Margin {
  // todo: show outline/redirect external doc editor
}


/**
 * This page is shown as a second page in the flow - next after meta.
 * User may choose whether copy link texts (filled in meta page) into the text fields no 1 and 2.
 * Every language's texts is shown in its tab.
 */
class NewTextDocContentEditorUI extends VerticalLayout with FullSize with Spacing with Margin {
  class TextsUI extends FormLayout with FullSize {
    val txtText1 = new TextField("No 1")
    val txtText2 = new TextField("No 2")

    addComponentsTo(this, txtText1, txtText2)
  }

  val chkCopyI18nMetaTextsToTextFields = new CheckBox("Copy link heading & subheading to text 1 & text 2 in page")
                                           with Immediate
  val tsTexts = new TabSheet with UndefinedSize with FullSize

  addComponentsTo(this, chkCopyI18nMetaTextsToTextFields, tsTexts)
  setExpandRatio(tsTexts, 1.0f)
}