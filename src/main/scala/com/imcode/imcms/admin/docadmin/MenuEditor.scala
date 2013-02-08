package com.imcode
package imcms
package admin.docadmin

import com.vaadin.ui._

import com.imcode.imcms.vaadin.ui._
import com.imcode.imcms.vaadin.data._
import com.imcode.imcms.vaadin.server._
import imcode.server.document.textdocument.{MenuItemDomainObject, MenuDomainObject, TextDocumentDomainObject}
import com.imcode.imcms.vaadin.Editor
import com.vaadin.ui.AbstractSelect.{VerticalLocationIs, ItemDescriptionGenerator}
import com.imcode.imcms.vaadin.data.PropertyDescriptor
import com.imcode.imcms.admin.doc.{DocEditorDialog, DocViewer, DocSelectDialog}
import scala.annotation.tailrec
import com.vaadin.data.util.HierarchicalContainer
import com.vaadin.event.dd.{DragAndDropEvent, DropHandler}
import com.vaadin.shared.ui.dd.VerticalDropLocation
import com.vaadin.server.Page
import com.vaadin.event.dd.acceptcriteria.{AcceptAll, Not, AcceptCriterion}
import scala.collection.JavaConverters._


// todo: ???revert btn (in save or in menu)???
class MenuEditor(doc: TextDocumentDomainObject, menu: MenuDomainObject) extends Editor with ImcmsServicesSupport {

  type Data = MenuDomainObject

  private var state = menu.clone()

  val ui = new MenuEditorUI |>> { ui =>

    ui.ttMenu.setItemDescriptionGenerator(new ItemDescriptionGenerator {
      def generateDescription(source: Component, itemId: AnyRef, propertyId: AnyRef) = "menu item tooltip"
    })

    ui.ttMenu.addValueChangeHandler {
      doto(ui.miEditSelectedDoc, ui.miExcludeSelectedDoc, ui.miShowSelectedDoc) {
        _.setEnabled(ui.ttMenu.isSelected)
      }
    }

    addContainerProperties(ui.ttMenu,
      PropertyDescriptor[DocId]("docs_projection.container_property.meta_id".i),
      PropertyDescriptor[String]("docs_projection.container_property.headline".i),
      PropertyDescriptor[String]("docs_projection.container_property.alias".i),
      PropertyDescriptor[String]("docs_projection.container_property.type".i),
      PropertyDescriptor[String]("docs_projection.container_property.status".i)
    )

    // todo: ??? search for current language + default version ???
    ui.miIncludeDocs.setCommandHandler {
      new DocSelectDialog("Choose documents", UI.getCurrent.imcmsUser) |>> { dlg =>
        dlg.setOkButtonHandler {
          for {
            doc <- dlg.projection.selection
            docId = doc.getId
            if !state.getItemsMap.containsKey(docId)
            doc <- imcmsServices.getDocumentMapper.getDefaultDocument(docId).asOption
          } {
            val docRef = imcmsServices.getDocumentMapper.getDocumentReference(doc)
            val menuItem = new MenuItemDomainObject(docRef)
            state.addMenuItemUnchecked(menuItem)
          }

          updateMenuUI()
          dlg.close()
        }
      } |> UI.getCurrent.addWindow
    }

    ui.miExcludeSelectedDoc.setCommandHandler {
      for (docId <- ui.ttMenu.selectionOpt) {
        state.removeMenuItemByDocumentId(docId)
        updateMenuUI()
      }
    }

    ui.miEditSelectedDoc.setCommandHandler {
      for (docId <- ui.ttMenu.selectionOpt) {
        imcmsServices.getDocumentMapper.getDocument(docId) match {
          case null =>
            Page.getCurrent.showWarningNotification("Document does not exist")
            state.removeMenuItemByDocumentId(docId)
            updateMenuUI()

          case doc =>
            new DocEditorDialog( "Edit document properties", doc) |>> { dlg =>
              dlg.setOkButtonHandler {
                dlg.docEditor.collectValues() match {
                  case Left(errors) => Page.getCurrent.showErrorNotification(errors.mkString(", "))
                  case Right((modifiedDoc, i18nMetas)) =>
                    try {
                      imcmsServices.getDocumentMapper.saveDocument(modifiedDoc, i18nMetas.asJava, UI.getCurrent.imcmsUser)
                      updateMenuUI()
                      dlg.close()
                    } catch {
                      case e =>
                        Page.getCurrent.showErrorNotification("Can't save document", e.getMessage)
                        throw e
                    }
                }
              }
            } |> UI.getCurrent.addWindow
        }
      }
    }

    ui.miShowSelectedDoc.setCommandHandler {
      for (docId <- ui.ttMenu.selectionOpt) {
        DocViewer.showDocViewDialog(ui, docId)
      }
    }

    ui.cbSortOrder.addValueChangeHandler {
      state.setSortOrder(ui.cbSortOrder.selection)
      updateMenuUI()
    }
  }

  resetValues()

  private object MenuDropHandlers {
    private val container = ui.ttMenu.getContainerDataSource.asInstanceOf[HierarchicalContainer]

    private abstract class AbstractDropHandler extends DropHandler {
      def drop(event: DragAndDropEvent) {
        val transferable = event.getTransferable.asInstanceOf[Table#TableTransferable]
        val target = event.getTargetDetails.asInstanceOf[AbstractSelect#AbstractSelectTargetDetails]

        val targetItemId = target.getItemIdOver
        val sourceItemId = transferable.getItemId

        target.getDropLocation match {
          case VerticalDropLocation.TOP =>
            val parentId = container.getParent(targetItemId)
            container.setParent(sourceItemId, parentId)
            container.moveAfterSibling(sourceItemId, targetItemId)
            container.moveAfterSibling(targetItemId, sourceItemId)

          case VerticalDropLocation.BOTTOM =>
            val parentId = container.getParent(targetItemId)
            container.setParent(sourceItemId, parentId)
            container.moveAfterSibling(sourceItemId, targetItemId)

          case VerticalDropLocation.MIDDLE =>
            container.setParent(sourceItemId, targetItemId)
        }

        updateItemsSortIndex()
      }

      protected def updateItemsSortIndex()
    }

    val singleLevel: DropHandler = new AbstractDropHandler {
      val getAcceptCriterion: AcceptCriterion = new Not(VerticalLocationIs.MIDDLE)

      protected def updateItemsSortIndex() {
        val menuItems = state.getItemsMap

        for {
          nodes <- container.rootItemIds().asOption
          (docId, index) <- nodes.asInstanceOf[JCollection[DocId]].asScala.zipWithIndex
        } {
          menuItems.get(docId) |> { _.setSortKey(index + 1) }
        }
      }
    }

    val multilevel: DropHandler = new AbstractDropHandler {
      val getAcceptCriterion: AcceptCriterion = AcceptAll.get()

      protected def updateItemsSortIndex() {
        def updateItemsTreeSortIndex(parentSortIndex: Option[String], nodes: JCollection[_]) {
          if (nodes != null) {
            for ((docId, index) <- nodes.asInstanceOf[JCollection[DocId]].asScala.zipWithIndex) {
              state.getItemsMap.get(docId) |> { menuItem =>
                val sortIndex = parentSortIndex.map(_ + ".").mkString + (index + 1)

                menuItem.setTreeSortIndex(sortIndex)
                updateItemsTreeSortIndex(Some(sortIndex), container.getChildren(docId))
              }
            }
          }
        }

        updateItemsTreeSortIndex(None, container.rootItemIds())
      }
    }
  }


  private def updateMenuUI() {
    val sortOrder = state.getSortOrder
    val isMultilevel = sortOrder == MenuDomainObject.MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER
    val isManualSort = Set(
      MenuDomainObject.MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER,
      MenuDomainObject.MENU_SORT_ORDER__BY_MANUAL_ORDER_REVERSED
    ).contains(sortOrder)


    ui.ttMenu.removeAllItems()
    ui.ttMenu.setDragMode(if (isManualSort) Table.TableDragMode.ROW else Table.TableDragMode.NONE)
    ui.ttMenu.setDropHandler(sortOrder match {
      case MenuDomainObject.MENU_SORT_ORDER__BY_MANUAL_TREE_ORDER => MenuDropHandlers.multilevel
      case MenuDomainObject.MENU_SORT_ORDER__BY_MANUAL_ORDER_REVERSED => MenuDropHandlers.singleLevel
      case _ => null
    })


    val menuItems = state.getMenuItems
    for (menuItem <- menuItems) {
      val doc = menuItem.getDocument
      val docId = doc.getId
      // doc.getDocumentType.getName.toLocalizedString(ui.getApplication.imcmsUser)
      ui.ttMenu.addRow(docId, docId: JInteger, doc.getHeadline, doc.getAlias, doc.getDocumentType.getName.toLocalizedString("eng"), doc.getLifeCyclePhase.toString)
      ui.ttMenu.setChildrenAllowed(docId, isMultilevel)
      ui.ttMenu.setCollapsed(docId, false)
    }

    if (isMultilevel) {
      @tailrec
      def findParentMenuItem(menuIndex: String): Option[MenuItemDomainObject] = {
        menuIndex.lastIndexOf('.') match {
          case -1 => None
          case  n =>
            val parentMenuIndex = menuIndex.substring(0, n)
            val parentMenuItemOpt = menuItems.find(_.getTreeSortIndex == parentMenuIndex)

            if (parentMenuItemOpt.isDefined) parentMenuItemOpt else findParentMenuItem(parentMenuIndex)
        }
      }

      for {
        menuItem <- menuItems
        parentMenuItem <- findParentMenuItem(menuItem.getTreeSortIndex)
      } {
        ui.ttMenu.setParent(menuItem.getDocumentId, parentMenuItem.getDocumentId)
      }
    }
  }

  def resetValues() {
    state = menu.clone()
    ui.cbSortOrder.selection = state.getSortOrder
    ui.ttMenu.selection = null
  }

  def collectValues(): ErrorsOrData = Right(state.clone())
}
