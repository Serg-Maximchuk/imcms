package com.imcode
package imcms
package admin.doc

import scala.collection.JavaConversions._
import com.imcode.imcms.vaadin.{ContainerProperty => CP, _}

import vaadin.{ImcmsApplication, FullSize}
import com.vaadin.ui.Table.CellStyleGenerator
import com.vaadin.ui._
import imcode.server.Imcms
import imcode.server.document.textdocument.TextDocumentDomainObject
import com.vaadin.terminal.{ExternalResource, Resource}
import com.vaadin.event.Action
import com.vaadin.data.{Property, Item, Container}
import java.lang.Class
import collection.immutable.{SortedSet, ListMap}
import api.Document
import imcode.server.document.{DocumentTypeDomainObject, DocumentDomainObject}
import PartialFunction.condOpt
import admin.access.user.UserSearchDialog
import java.util.{Calendar, Date}
import java.util.concurrent.atomic.AtomicReference

case class DocFilter()

/**
 * Doc search consists of two forms for specifying search params and a table that displays search result.
 */
class DocSearch(val docProvider: DocProvider) {
  val basicSearchForm = new DocBasicSearchForm
  val advancedSearchForm = new DocAdvancedSearchForm
  val advancedSearchFormPanel = new Panel with Scrollable with UndefinedSize with FullHeight {
    setStyleName(Panel.STYLE_LIGHT)
    setContent(advancedSearchForm.ui)
  }

  val docViewUI = new DocViewUI(new DocContainer) with FullSize

  val ui = letret(new GridLayout(1, 2) with Spacing with FullSize) { ui =>
    ui.addComponent(basicSearchForm.ui)
    ui.addComponent(docViewUI)
    ui.setRowExpandRatio(1, 1f)

    basicSearchForm.ui.btnAdvanced.addClickHandler {
      val component = ui.getComponent(0, 1) match {
        case `docViewUI` => advancedSearchFormPanel
        case _ => docViewUI
      }

      ui.removeComponent(0, 1)
      ui.addComponent(component, 0, 1)
    }

    basicSearchForm.ui.lytButtons.btnSearch.addClickHandler {
      // check
      ui.removeComponent(0, 1)
      ui.addComponent(docViewUI, 0, 1)
      submit()
    }

    basicSearchForm.ui.lytButtons.btnClear.addClickHandler { reset() }

    basicSearchForm.ui.chkAdvanced.addValueChangeHandler {
      if (!basicSearchForm.ui.chkAdvanced.booleanValue) {
        // check
        ui.removeComponent(0, 1)
        ui.addComponent(docViewUI, 0, 1)
      }
    }
  }

  reset()

  def reset() {
    basicSearchForm.reset()
    advancedSearchForm.reset()
    update()
    submit()
  }

  def update() {
    basicSearchForm.setRangeInputPrompt(docProvider.range)
  }

  def submit() {
    //if (basicSearchForm.validate() && (basicSearchForm.ui.chkAdvanced.checked advancedSearchForm.validate()))
    System.out.println(">>>>QUERY: " + basicSearchForm.query.toString)
    docViewUI.setContainerDataSource(new DocContainer(docProvider.docIds))
  }
}


trait DocProvider {
  def range: Option[(DocId, DocId)]
  def docIds: Seq[DocId]
}


class AllDocProvider extends DocProvider {
  private val docMapper = Imcms.getServices.getDocumentMapper

  def range = let(docMapper.getDocumentIdRange) { idsRange =>
    Some(Int box idsRange.getMinimumInteger, Int box idsRange.getMaximumInteger)
  }

  def docIds = docMapper.getAllDocumentIds.toSeq
}


class CustomDocProvider extends DocProvider {

  private var customDocIds = Seq.empty[DocId]

  def range = condOpt(docIds) { case ids if ids.nonEmpty => (ids.min, ids.max) }

  def docIds = customDocIds

  def addDocId(docId: DocId) = customDocIds :+= docId

  def removeDocId(docId: DocId) = customDocIds.remove(docId)
}


class DocContainer(docIds: Seq[DocId] = Seq.empty) extends Container with Container.Ordered {

  private val propertyIdToType = ListMap(
      "doc.tbl.col.id" -> classOf[DocId],
      "doc.tbl.col.type" -> classOf[JInteger],
      "doc.tbl.col.status" -> classOf[String],
      "doc.tbl.col.alias" -> classOf[String],
      "doc.tbl.col.parents" -> classOf[Component],
      "doc.tbl.col.children" -> classOf[Component])

  private val propertyIds = propertyIdToType.keys.toList

  case class DocItem(docId: DocId) extends Item {

    lazy val doc = Imcms.getServices.getDocumentMapper.getDocument(docId)

    def removeItemProperty(id: AnyRef) = throw new UnsupportedOperationException

    def addItemProperty(id: AnyRef, property: Property) = throw new UnsupportedOperationException

    def getItemPropertyIds = propertyIds

    def getItemProperty(id: AnyRef) = FunctionProperty(id match {
      case "doc.tbl.col.id" => doc.getId
      case "doc.tbl.col.type" => doc.getDocumentTypeId
      case "doc.tbl.col.alias" => doc.getAlias
      case "doc.tbl.col.status" =>
        () => doc.getPublicationStatus match {
          case Document.PublicationStatus.NEW => "doc.pub.status.new".i
          case Document.PublicationStatus.APPROVED => "doc.pub.status.approved".i
          case Document.PublicationStatus.DISAPPROVED => "doc.pub.status.disapproved".i
        }

      case "doc.tbl.col.parents" =>
        () => Imcms.getServices.getDocumentMapper.getDocumentMenuPairsContainingDocument(doc).toList match {
          case List() => null
          case List(pair) =>
            letret(new Tree with ItemIdType[DocumentDomainObject] with NotSelectable with DocStatusItemIcon) { tree =>
              val parentDoc = pair.getDocument
              tree.addItem(parentDoc)
              tree.setChildrenAllowed(parentDoc, false)
              tree.setItemCaption(parentDoc, "%s - %s" format (parentDoc.getId, parentDoc.getHeadline))
            }

          case pairs => letret(new Tree with ItemIdType[DocumentDomainObject] with NotSelectable with DocStatusItemIcon) { tree =>
            val root = new {}
            tree.addItem(root)
            tree.setItemCaption(root, pairs.size.toString)
            for (pair <- pairs; parentDoc = pair.getDocument) {
              tree.addItem(parentDoc)
              tree.setChildrenAllowed(parentDoc, false)
              tree.setItemCaption(parentDoc, "%s - %s" format (parentDoc.getId, parentDoc.getHeadline))
              tree.setParent(parentDoc, root)
            }
          }
        }

      case "doc.tbl.col.children" =>
        () => doc match {
          case textDoc: TextDocumentDomainObject =>
            Imcms.getServices.getDocumentMapper.getDocuments(textDoc.getChildDocumentIds).toList match {
              case List() => null
              case List(childDoc) =>
                letret(new Tree with ItemIdType[DocumentDomainObject] with DocStatusItemIcon with NotSelectable) { tree =>
                  tree.addItem(childDoc)
                  tree.setChildrenAllowed(childDoc, false)
                  tree.setItemCaption(childDoc, "%s - %s" format (childDoc.getId, childDoc.getHeadline))
                }

              case childDocs =>letret(new Tree with ItemIdType[DocumentDomainObject] with DocStatusItemIcon with NotSelectable) { tree =>
                val root = new {}
                tree.addItem(root)
                tree.setItemCaption(root, childDocs.size.toString)
                for (childDoc <- childDocs) {
                  tree.addItem(childDoc)
                  tree.setChildrenAllowed(childDoc, false)
                  tree.setItemCaption(childDoc, "%s - %s" format (childDoc.getId, childDoc.getHeadline))
                  tree.setParent(childDoc, root)
                  // >>> link to list documents
                }
              }
            }

          case _ => null
        }
    })
  }

  def getContainerPropertyIds = propertyIds

  def addItem() = throw new UnsupportedOperationException

  def removeItem(itemId: AnyRef) = throw new UnsupportedOperationException

  def addItem(itemId: AnyRef) = throw new UnsupportedOperationException

  def removeAllItems = throw new UnsupportedOperationException

  def getType(propertyId: AnyRef) = propertyIdToType(propertyId.asInstanceOf[String])

  def getItem(itemId: AnyRef) = DocItem(itemId.asInstanceOf[JInteger])

  def getContainerProperty(itemId: AnyRef, propertyId: AnyRef) = getItem(itemId).getItemProperty(propertyId)

  def size = docIds.size

  def containsId(itemId: AnyRef) = docIds.contains(itemId.asInstanceOf[JInteger])

  def addContainerProperty(propertyId: AnyRef, `type` : Class[_], defaultValue: AnyRef) = throw new UnsupportedOperationException

  def removeContainerProperty(propertyId: AnyRef) = throw new UnsupportedOperationException

  def getItemIds = docIds

  def addItemAfter(previousItemId: AnyRef, newItemId: AnyRef) = null

  def addItemAfter(previousItemId: AnyRef) = null

  def isLastId(itemId: AnyRef) = itemId == lastItemId

  def isFirstId(itemId: AnyRef) = itemId == firstItemId

  def lastItemId() = docIds.last

  def firstItemId() = docIds.head

  // extremely ineffective prototype
  def prevItemId(itemId: AnyRef) = let(docIds.toIndexedSeq) { seq =>
    seq.indexOf(itemId.asInstanceOf[DocId]) match {
      case index if index > 0 => seq(index - 1)
      case _ => null
    }
  }

  // extremely ineffective prototype
  def nextItemId(itemId: AnyRef) = let(docIds.toIndexedSeq) { seq =>
    seq.indexOf(itemId.asInstanceOf[DocId]) match {
      case index if index < (size - 1) => seq(index + 1)
      case _ => null
    }
  }
}


class DocViewUI(container: DocContainer) extends Table(null, container)
    with MultiSelectBehavior[DocId] with DocTableItemIcon with Selectable {

  setColumnCollapsingAllowed(true)
  setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY)

  override def setContainerDataSource(container: Container) {
    val collapsedColumns = getContainerPropertyIds filter isColumnCollapsed
    super.setContainerDataSource(container)

    setColumnHeaders(container.getContainerPropertyIds map (_.toString.i) toArray )
    List("doc.tbl.col.parents", "doc.tbl.col.children") foreach { setColumnCollapsed(_, true) }
    collapsedColumns foreach (setColumnCollapsed(_, true))
  }
}

//object DocViewUI {
//  def apply(fullSize: Boolean = false) = let(new DocContainer) { container =>
//    new Table(null, container) with DocTableItemIcon with MultiSelect2[DocId] with Selectable { table =>
//      if (fullSize) table.setSizeFull
//      setColumnCollapsingAllowed(true)
//      setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY)
//      setColumnHeaders(container.getContainerPropertyIds map (_.i) toArray )
//      List("doc.tbl.col.parents", "doc.tbl.col.children") foreach { setColumnCollapsed(_, true) }
//    }
//  }
//
//  def apply2(fullSize: Boolean = false) = new Table with DocStatusItemIcon with MultiSelect2[DocumentDomainObject] with Selectable { table =>
//    addContainerProperties(table,
//      CP[JInteger]("doc.tbl.col.id"),
//      CP[JInteger]("doc.tbl.col.type"),
//      CP[String]("doc.tbl.col.status"),
//      CP[String]("doc.tbl.col.alias"))
//
//
//    if (fullSize) table.setSizeFull
//
////    table.setCellStyleGenerator(new CellStyleGenerator {
////      def getStyle(itemId: AnyRef, propertyId: AnyRef) {
////        if (propertyId == null) {
////            // no propertyId, styling row
////            return (markedRows.contains(itemId) ? "marked" : null);
////        } else if (ExampleUtil.iso3166_PROPERTY_NAME.equals(propertyId)) {
////            return "bold";
////        } else {
////            // no style
////            return null;
////        }
////      }
////    })
//
//    // alias VIEW -> 1003
//    // status EDIT META -> http://imcms.dev.imcode.com/servlet/AdminDoc?meta_id=1003&flags=1
//    // admin: VIWE + ADMIN PANEL 1009 - Start page swe(Copy/Kopia) -> http://imcms.dev.imcode.com/servlet/AdminDoc?meta_id=1009
//    // ref -> DocumentReferences! 3 -> http://imcms.dev.imcode.com/servlet/DocumentReferences?returnurl=ListDocuments%3Fstart%3D1001%26end%3D1031%26showspan%3D%2BLista%2B&id=1001
//    // children LIST DOCS -> 1023 - Testdoc-swe -> http://imcms.dev.imcode.com/servlet/ListDocuments?start=1023&end=1023
//
//    // >>> Html.getLinkedStatusIconTemplate( document, user, request )
//
//    val docMapper = Imcms.getServices.getDocumentMapper
//
//
//    trait TreeActionHandler extends Tree {
//      addActionHandler(new Action.Handler {
//        import Actions._
//
//        def getActions(target: AnyRef, sender: AnyRef) = target match {
//          case doc: DocumentDomainObject => Array(AddToSelection, View)
//          case _ => Array.empty[Action]
//        }
//
//        def handleAction(action: Action, sender: AnyRef, target: AnyRef) =
//          action match {
//            case AddToSelection => //docSelection.ui.tblDocs.addItem(target)
//            case _ =>
//          }
//      })
//    }
//
//    table.addGeneratedColumn("doc.tbl.col.parents", new Table.ColumnGenerator {
//      def generateCell(source: Table, itemId: AnyRef, columnId: AnyRef) =
//        docMapper.getDocumentMenuPairsContainingDocument(itemId.asInstanceOf[DocumentDomainObject]).toList match {
//          case List() => null
//          case List(pair) =>
//            letret(new Tree with TreeActionHandler with ItemIdType[DocumentDomainObject] with DocStatusItemIcon) { tree =>
//              val parentDoc = pair.getDocument
//              tree.addItem(parentDoc)
//              tree.setChildrenAllowed(parentDoc, false)
//              tree.setItemCaption(parentDoc, "%s - %s" format (parentDoc.getId, parentDoc.getHeadline))
//            }
//
//          case pairs => letret(new Tree with TreeActionHandler with ItemIdType[DocumentDomainObject] with DocStatusItemIcon) { tree =>
//            val root = new {}
//            tree.addItem(root)
//            tree.setItemCaption(root, pairs.size.toString)
//            for (pair <- pairs; parentDoc = pair.getDocument) {
//              tree.addItem(parentDoc)
//              tree.setChildrenAllowed(parentDoc, false)
//              tree.setItemCaption(parentDoc, "%s - %s" format (parentDoc.getId, parentDoc.getHeadline))
//              tree.setParent(parentDoc, root)
//            }
//          }
//        }
//    })
//
//    table.addGeneratedColumn("doc.tbl.col.children", new Table.ColumnGenerator {
//      def generateCell(source: Table, itemId: AnyRef, columnId: AnyRef) =
//        itemId match {
//          case textDoc: TextDocumentDomainObject =>
//            docMapper.getDocuments(textDoc.getChildDocumentIds).toList match {
//              case List() => null
//              case List(childDoc) =>
//                letret(new Tree with TreeActionHandler with ItemIdType[DocumentDomainObject] with DocStatusItemIcon) { tree =>
//                  tree.addItem(childDoc)
//                  tree.setChildrenAllowed(childDoc, false)
//                  tree.setItemCaption(childDoc, "%s - %s" format (childDoc.getId, childDoc.getHeadline))
//                }
//
//              case childDocs =>letret(new Tree with TreeActionHandler with ItemIdType[DocumentDomainObject] with DocStatusItemIcon) { tree =>
//                val root = new {}
//                tree.addItem(root)
//                tree.setItemCaption(root, childDocs.size.toString)
//                for (childDoc <- childDocs) {
//                  tree.addItem(childDoc)
//                  tree.setChildrenAllowed(childDoc, false)
//                  tree.setItemCaption(childDoc, "%s - %s" format (childDoc.getId, childDoc.getHeadline))
//                  tree.setParent(childDoc, root)
//                  // >>> link to list documents
//                }
//              }
//            }
//
//          case _ => null
//        }
//    })
//
//    table.setColumnHeaders(Array("doc.tbl.col.id".i, "doc.tbl.col.type".i, "doc.tbl.col.status".i,
//      "doc.tbl.col.alias".i, "doc.tbl.col.parents".i, "doc.tbl.col.children".i))
//
//    table.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY)
//  }
//}

trait DocStatusItemIcon extends AbstractSelect {
  override def getItemIcon(itemId: AnyRef) = itemId match {
    case doc: DocumentDomainObject => new ExternalResource("imcms/eng/images/admin/status/%s.gif" format
      itemId.asInstanceOf[DocumentDomainObject].getLifeCyclePhase.toString)

    case _ => null
  }
}

trait DocTableItemIcon extends AbstractSelect with XSelect[DocId] {
  override def getItemIcon(itemId: AnyRef) = item(itemId.asInstanceOf[DocId]) match {
    case docItem: DocContainer#DocItem =>
      new ExternalResource("imcms/eng/images/admin/status/%s.gif" format docItem.doc.getLifeCyclePhase.toString)

    case _ => null
  }
}


class DocBasicSearchForm {
  private var state = DocBasicSearchFormState()

  val ui: DocBasicFormSearchUI = letret(new DocBasicFormSearchUI) { ui =>
    ui.chkRange.addClickHandler { state = alterRange(state) }

    ui.chkText.addClickHandler { state = alterText(state) }

    ui.chkType.addClickHandler { state = alterType(state) }

    ui.chkAdvanced.addValueChangeHandler {
      ui.btnAdvanced.setEnabled(ui.chkAdvanced.isChecked)
    }
  }

  def setRangeInputPrompt(range: Option[(DocId, DocId)]) {
    let(range map { case (start, end) => (start.toString, end.toString) } getOrElse ("", "")) {
      case (start, end) =>
        ui.lytRange.txtStart.setInputPrompt(start)
        ui.lytRange.txtEnd.setInputPrompt(end)
    }
  }

  /** @return right 'solr query' or left 'validation error' */
  def query: Either[Throwable, String] = EX.allCatch.either {
    val rangeOpt = whenOpt(ui.chkRange.isChecked) {
      DocSearchRange(
        condOpt(ui.lytRange.txtStart.trim) {
          case value if value.nonEmpty => value match {
            case PosInt(start) => start
            case _ => error("-range must be pos nums-")
          }
        },
        condOpt(ui.lytRange.txtEnd.trim) {
          case value if value.nonEmpty => value match {
            case PosInt(end) => end
            case _ => error("-range must be pos nums-")
          }
        }
      )
    }


    val textOpt: Option[String] =
      if (ui.chkText.isUnchecked) None
      else condOpt(ui.txtText.trim) {
        case value if value.nonEmpty => value
      }


    val typesOpt: Option[List[String]] = whenOpt(ui.chkType.isChecked) {
      Map(ui.lytType.chkFile -> "file",
        ui.lytType.chkText -> "text",
        ui.lytType.chkHtml -> "html"
      ).filterKeys(_.isChecked).values.toList match {
        case Nil => error("-when type is checled then at least one type should be selected-")
        case types => types
      }
    }

    List(
      rangeOpt.map(range => "range:[%s TO %s]" format (range.start.getOrElse("*"), range.end.getOrElse("*"))),
      textOpt.map("text:" + _),
      typesOpt.map(_.mkString("type:(", "AND", ")"))
    ).flatten match {
      case Nil => ""
      case terms => terms.mkString(" ")
    }
  }

  def reset() {
    ui.chkRange.checked = true
    ui.chkText.checked = true
    ui.chkType.checked = true
    ui.chkAdvanced.checked = false

    setState(DocBasicSearchFormState())
  }

  def setState(newState: DocBasicSearchFormState) {
    alterRange(newState)
    alterText(newState)
    alterType(newState)

    state = newState
  }

  def getState() = state

  private def alterRange(currentState: DocBasicSearchFormState) = {
    if (ui.chkRange.isChecked) {
      ui.lytRange.setEnabled(true)
      ui.lytRange.txtStart.value = currentState.range.flatMap(_.start).map(_.toString).getOrElse("")
      ui.lytRange.txtEnd.value = currentState.range.flatMap(_.end).map(_.toString).getOrElse("")
      currentState
    } else {
      val start = condOpt(ui.lytRange.txtStart.value.trim) { case value if value.nonEmpty => value.toInt }
      val end = condOpt(ui.lytRange.txtEnd.value.trim) { case value if value.nonEmpty => value.toInt }
      val newState = currentState.copy(
        range = if (start.isEmpty && end.isEmpty) None else Some(DocSearchRange(start, end))
      )

      ui.lytRange.setEnabled(true)
      ui.lytRange.txtStart.value = "" // range get default value
      ui.lytRange.txtEnd.value = ""  // range get default value
      ui.lytRange.setEnabled(false)
      newState
    }
  }

  private def alterText(currentState: DocBasicSearchFormState) = {
    if (ui.chkText.isChecked) {
      ui.txtText.setEnabled(true)
      ui.txtText.value = currentState.text.getOrElse("")
      currentState
    } else {
      val newState = currentState.copy(
        text = condOpt(ui.txtText.value.trim) { case value if value.nonEmpty => value }
      )

      ui.txtText.setEnabled(true)
      ui.txtText.value = "" // default text
      ui.txtText.setEnabled(false)
      newState
    }
  }

  private def alterType(currentState: DocBasicSearchFormState) = {
    if (ui.chkType.isChecked) {
      ui.lytType.setEnabled(true)
      ui.lytType.chkText.checked = currentState.docType.map(_(DocumentTypeDomainObject.TEXT)).getOrElse(true)
      ui.lytType.chkFile.checked = currentState.docType.map(_(DocumentTypeDomainObject.FILE)).getOrElse(true)
      ui.lytType.chkHtml.checked = currentState.docType.map(_(DocumentTypeDomainObject.HTML)).getOrElse(true)
      currentState
    } else {
      val types =
        Set(
          condOpt(ui.lytType.chkText.isChecked) { case true => DocumentTypeDomainObject.TEXT },
          condOpt(ui.lytType.chkFile.isChecked) { case true => DocumentTypeDomainObject.FILE },
          condOpt(ui.lytType.chkHtml.isChecked) { case true => DocumentTypeDomainObject.HTML }
        ).flatten

      val newState = currentState.copy(
        docType = if (types.size == 3) None else Some(types)
      )

      ui.lytType.setEnabled(true)
      forlet(ui.lytType.chkText, ui.lytType.chkFile, ui.lytType.chkHtml) { _.check }
      ui.lytType.setEnabled(false)
      newState
    }
  }
}

case class DocSearchRange(start: Option[Int] = None, end: Option[Int] = None)

case class DocBasicSearchFormState(
  range: Option[DocSearchRange] = None,
  text: Option[String] = None,
  docType: Option[Set[DocumentTypeDomainObject]] = None
)


class DocBasicFormSearchUI extends CustomLayout("admin/doc/search/basic_form") with FullWidth {

  val chkRange = new CheckBox("doc.search.basic.frm.chk.range".i) with Immediate
  val lytRange = new HorizontalLayout with Spacing with UndefinedSize {
    val txtStart = new TextField { setColumns(5) }
    val txtEnd = new TextField { setColumns(5) }

    addComponents(this, txtStart, txtEnd)
  }

  val chkText = new CheckBox("doc.search.basic.frm.chk.text".i) with Immediate
  val txtText = new TextField { setInputPrompt("doc.search.basic.frm.txt.text.prompt".i) }

  val chkType = new CheckBox("doc.search.basic.frm.chk.type".i) with Immediate
  val lytType = new HorizontalLayout with UndefinedSize with Spacing {
    val chkText = new CheckBox("doc.search.basic.frm.chk.type.text".i)
    val chkFile = new CheckBox("doc.search.basic.frm.chk.type.file".i)
    val chkHtml = new CheckBox("doc.search.basic.frm.chk.type.html".i)

    addComponents(this, chkText, chkFile, chkHtml)
  }

  val chkAdvanced = new CheckBox("doc.search.basic.frm.chk.advanced".i) with Immediate
  val btnAdvanced = new Button("doc.search.basic.frm.btn.advanced".i) with LinkStyle


  val lytButtons = new HorizontalLayout with UndefinedSize with Spacing {
    val btnClear = new Button("doc.search.basic.frm.btn.clear".i) { setStyleName("small") }
    val btnSearch = new Button("doc.search.basic.frm.btn.search".i) { setStyleName("small") }

    addComponents(this, btnClear, btnSearch)
  }

  addNamedComponents(this,
    "doc.search.basic.frm.chk.range" -> chkRange,
    "doc.search.basic.frm.range" -> lytRange,
    "doc.search.basic.frm.chk.text" -> chkText,
    "doc.search.basic.frm.txt.text" -> txtText,
    "doc.search.basic.frm.chk.type" -> chkType,
    "doc.search.basic.frm.type" -> lytType,
    "doc.search.basic.frm.chk.advanced" -> chkAdvanced,
    "doc.search.basic.frm.btn.advanced" -> btnAdvanced,
    "doc.search.basic.frm.buttons" -> lytButtons
  )
}


class DocAdvancedSearchForm {
  val ui = new DocAdvancedSearchFormUI

  ui.chkCategories.addClickHandler { switchCategories() }
  ui.chkDates.addClickHandler { switchDates() }
  ui.chkRelationships.addClickHandler { switchRelationships() }
  ui.chkMaintainers.addClickHandler { switchMaintainers() }
  ui.chkStatus.addClickHandler { switchStatus() }

  def reset() {
    forlet(ui.chkCategories, ui.chkDates, ui.chkRelationships, ui.chkMaintainers, ui.chkStatus)(_.uncheck)

    switchCategories()
    switchMaintainers()
    switchRelationships()
    switchDates()
    switchStatus()

    for {
      categoryType <- Imcms.getServices.getCategoryMapper.getAllCategoryTypes
      category <- Imcms.getServices.getCategoryMapper.getAllCategoriesOfType(categoryType)
    } {
      ui.tcsCategories.addItem(category)
      ui.tcsCategories.setItemCaption(category, categoryType.getName + ":" + category.getName)
      ?(category.getImageUrl).foreach(url => ui.tcsCategories.setItemIcon(category, new ExternalResource(url)))
    }
  }

  private def switch(checkBox: CheckBox, component: Component, name: String) {
    ui.addComponent(
      if (checkBox.checked) component else new Label("doc.search.advanced.lbl.all".i) with UndefinedSize,
      name)
  }

  private def switchCategories() = switch(ui.chkCategories, ui.tcsCategories, "doc.search.advanced.frm.tcs.categories")
  private def switchMaintainers() = switch(ui.chkMaintainers, ui.lytMaintainers, "doc.search.advanced.frm.lyt.maintainers")
  private def switchRelationships() = switch(ui.chkRelationships, ui.lytRelationships, "doc.search.advanced.frm.lyt.relationship")
  private def switchDates() = switch(ui.chkDates, ui.lytDates, "doc.search.advanced.frm.lyt.dates")
  private def switchStatus() = switch(ui.chkStatus, ui.lytStatus, "doc.search.advanced.frm.status")
}


class DocAdvancedSearchFormUI extends CustomLayout("admin/doc/search/advanced_form") with FullWidth {
  val lblPredefined = new Label("doc.search.advanced.frm.lbl.predefined".i) with UndefinedSize
  val cbPredefined = new ComboBox

  val chkStatus = new CheckBox("doc.search.advanced.frm.chk.status".i) with Immediate
  val lytStatus = new HorizontalLayout with Spacing with UndefinedSize {
    val chkNew = new CheckBox("doc.search.advanced.frm.ckh.status.new".i)
    val chkPublished = new CheckBox("doc.search.advanced.frm.chk.status.published".i)
    val chkUnpublished = new CheckBox("doc.search.advanced.frm.chk.status.unpublished".i)
    val chkApproved = new CheckBox("doc.search.advanced.frm.chk.status.approved".i)
    val chkDisapproved = new CheckBox("doc.search.advanced.frm.chk.status.disapproved".i)
    val chkExpired = new CheckBox("doc.search.advanced.frm.chk.status.expired".i)

    addComponents(this, chkNew, chkPublished, chkUnpublished, chkApproved, chkDisapproved, chkExpired)
  }

  val chkDates = new CheckBox("doc.search.advanced.frm.chk.dates".i) with Immediate
  val lytDates = new FormLayout with UndefinedSize {
    val drCreated = new DocDateRangeUI("created") with DocDateRangeUISetup
    val drModified = new DocDateRangeUI("modified") with DocDateRangeUISetup
    val drPublished = new DocDateRangeUI("published") with DocDateRangeUISetup
    val drExpired = new DocDateRangeUI("expired") with DocDateRangeUISetup

    addComponents(this, drCreated, drModified, drPublished, drExpired)
  }

  val chkCategories = new CheckBox("doc.search.advanced.frm.chk.categories".i) with Immediate
  val tcsCategories = new TwinColSelect

  val chkRelationships = new CheckBox("doc.search.advanced.frm.chk.relationship".i) with Immediate
  val lytRelationships = new HorizontalLayout with Spacing with UndefinedSize {
    val cbParents = new ComboBox("doc.search.advanced.frm.chk.relationship.parents".i)
    val cbChildren = new ComboBox("doc.search.advanced.frm.chk.relationship.children".i)

    cbParents.addItem("-not specified-")
    cbParents.addItem("With parents")
    cbParents.addItem("Without parents")

    cbChildren.addItem("-not specified-")
    cbChildren.addItem("With children")
    cbChildren.addItem("Without children")

    cbParents.setNullSelectionItemId("-not specified-")
    cbChildren.setNullSelectionItemId("-not specified-")

    addComponents(this, cbParents, cbChildren)
  }

  val chkMaintainers = new CheckBox("doc.search.advanced.frm.chk.maintainers".i) with Immediate
  val lytMaintainers = new HorizontalLayout with Spacing with UndefinedSize{
    val ulCreators = new UserListUI("doc.search.advanced.frm.chk.maintainers.creators".i) with UserListUISetup {
      val searchDialogCaption = "Select creator"
    }

    val ulPublishers = new UserListUI("doc.search.advanced.frm.chk.maintainers.publishers".i) with UserListUISetup {
      val searchDialogCaption = "Select publisher"
    }

    addComponents(this, ulCreators, ulPublishers)
  }

  addNamedComponents(this,
    "doc.search.advanced.frm.chk.status" -> chkStatus,
    "doc.search.advanced.frm.status" -> lytStatus,
    "doc.search.advanced.frm.lbl.predefined" -> lblPredefined,
    "doc.search.advanced.frm.cb.predefined" -> cbPredefined,
    "doc.search.advanced.frm.chk.dates" -> chkDates,
    "doc.search.advanced.frm.lyt.dates" -> lytDates,
    "doc.search.advanced.frm.chk.relationship" -> chkRelationships,
    "doc.search.advanced.frm.lyt.relationship" -> lytRelationships,
    "doc.search.advanced.frm.chk.categories" -> chkCategories,
    "doc.search.advanced.frm.tcs.categories" -> tcsCategories,
    "doc.search.advanced.frm.chk.maintainers" -> chkMaintainers,
    "doc.search.advanced.frm.lyt.maintainers" -> lytMaintainers
  )
}


trait UserListUISetup { this: UserListUI =>
  val searchDialogCaption: String

  btnAdd.addClickHandler {
    getApplication.initAndShow(new OkCancelDialog(searchDialogCaption) with UserSearchDialog) { dlg =>
      dlg.wrapOkHandler {
        for (user <- dlg.search.selection) lstUsers.addItem(Int box user.getId, "#" + user.getLoginName)
      }
    }
  }

  btnRemove.addClickHandler {
    lstUsers.value.foreach(lstUsers.removeItem)
  }
}


/**
 * Component for managing list of users.
 */
class UserListUI(caption: String = "") extends GridLayout(2, 1) {
  val lstUsers = new ListSelect(caption) with MultiSelectBehavior[UserId] with NoNullSelection {
    setColumns(20)
  }
  val btnAdd = new Button("+")
  val btnRemove = new Button("-")
  val lytButtons = new VerticalLayout with UndefinedSize

  addComponents(lytButtons, btnRemove, btnAdd)
  addComponents(this, lstUsers, lytButtons)

  setComponentAlignment(lytButtons, Alignment.BOTTOM_LEFT)
}


object DocRangeType extends Enumeration {
  val Undefined, Custom, Day, Week, Month, Quarter, Year = Value
}


class DocDateRangeUI(caption: String = "") extends HorizontalLayout with Spacing with UndefinedSize {
  val cbRangeType = new ComboBox with ValueType[DocRangeType.Value] with NoNullSelection with Immediate
  val dtFrom = new PopupDateField with DayResolution
  val dtTo = new PopupDateField with DayResolution

  dtFrom.setInputPrompt("From")
  dtTo.setInputPrompt("To")

  setCaption(caption)

  addComponents(this, cbRangeType, dtFrom, dtTo)
}


trait DocDateRangeUISetup { this: DocDateRangeUI =>
  import DocRangeType._

  cbRangeType.addValueChangeHandler {
    forlet(dtFrom, dtTo) { _ setEnabled false }
    val now = new Date
    val calendar = Calendar.getInstance

    cbRangeType.value match {
      case Undefined =>
        dtFrom.setValue(null)
        dtTo.setValue(null)

      case Custom =>
        forlet(dtFrom, dtTo) { dt => dt setEnabled true; dt.value = now }

      case Day =>
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        dtFrom.value = calendar.getTime
        dtTo.value = now

      case Week =>
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        dtFrom.value = calendar.getTime
        dtTo.value = now

      case Month =>
        calendar.add(Calendar.MONTH, -1)
        dtFrom.value = calendar.getTime
        dtTo.value = now

      case Quarter =>
        calendar.add(Calendar.MONTH, -3)
        dtFrom.value = calendar.getTime
        dtTo.value = now

      case Year =>
        calendar.add(Calendar.YEAR, -1)
        dtFrom.value = calendar.getTime
        dtTo.value = now
    }
  }

  DocRangeType.values foreach { v => cbRangeType addItem v; println("added: " + v) }
  cbRangeType.value = Undefined
}