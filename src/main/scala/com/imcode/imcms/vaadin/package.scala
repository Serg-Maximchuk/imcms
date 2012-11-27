package com.imcode
package imcms

import scala.collection.JavaConverters._

import com.vaadin.Application
import com.vaadin.data.Property.{ValueChangeNotifier, ValueChangeEvent, ValueChangeListener}
import com.vaadin.event.ItemClickEvent
import com.vaadin.data.{Item, Container, Property}
import com.vaadin.ui.Table.CellStyleGenerator
import com.vaadin.ui._
import com.vaadin.terminal.gwt.server.WebApplicationContext
import javax.servlet.http.HttpSession
import javax.servlet.ServletContext
import com.vaadin.ui.Window.Notification
import com.vaadin.data.Container.ItemSetChangeListener
import com.vaadin.terminal.{Resource, UserError, Sizeable}

package object vaadin {

  type PropertyId = AnyRef
  type PropertyValue = AnyRef
  type ItemId = AnyRef
  type ColumnId = AnyRef




  /**
   * Property value type.
   *
   * Adds type-checked access to property value.
   */
  trait GenericProperty[A <: PropertyValue] extends Property {
    def value = getValue.asInstanceOf[A]
    def value_=(v: A): Unit = setValue(v)

    def clear(implicit ev: A =:= String): Unit =  setValue("")
    def trim(implicit ev: A =:= String): String = value.trim
    def trimOpt(implicit ev: A =:= String): Option[String] = trim match {
      case "" => None
      case v => Some(v)
    }
    def isBlank(implicit ev: A =:= String): Boolean = trim.isEmpty
    def notBlank(implicit ev: A =:= String): Boolean = !isBlank
  }


//case class ByNameProperty[A >: Null <: AnyRef](byName: => A)(implicit mf: Manifest[A]) extends Property {
//
//  def setReadOnly(newStatus: Boolean) = throw new UnsupportedOperationException
//
//  val isReadOnly = true
//
//  val getType = mf.erasure
//
//  def setValue(newValue: AnyRef) = throw new UnsupportedOperationException
//
//  def getValue = byName //.asInstanceOf[AnyRef]
//
//  override def toString = ?(getValue) map { _.toString } getOrElse ""
//}

// add memoized byNameProperty

  trait NullableProperty[A <: PropertyValue] extends GenericProperty[A] {
    def valueOpt: Option[A] = Option(value)
  }

  // todo: itemsIds as Seq?
  trait GenericContainer[A <: ItemId] extends Container {
    def itemIds: JCollection[A] = getItemIds.asInstanceOf[JCollection[A]]
    def itemIds_=(ids: JCollection[A]) {
      removeAllItems()
      ids.asScala.foreach(addItem _)
    }

    def item(id: A): Item = getItem(id)

    def firstItemIdOpt: Option[A] = itemIds.asScala.headOption
  }


  /**
   * Component data type.
   *
   * Adds type-checked access to data.
   */
  trait GenericData[A <: AnyRef] extends AbstractComponent {
    def data: A = getData.asInstanceOf[A]
    def data_=(d: A) { setData(d) }
  }

  def menuCommand(handler: MenuBar#MenuItem => Unit) = new MenuBar.Command {
    def menuSelected(mi: MenuBar#MenuItem): Unit = handler(mi)
  }

  implicit def fn0ToMenuCommand(f: () => Unit) = menuCommand { _ => f() }

  def addComponentsTo(container: ComponentContainer, component: Component, components: Component*) = {
    component +: components foreach { c => container addComponent c }
    container
  }

  def addNamedComponents(container: CustomLayout, component: (String, Component), components: (String, Component)*) = {
    for ((location, component) <- component +: components) container.addComponent(component, location)
    container
  }

  def addContainerProperties(container: Container, descriptors: PropertyDescriptor[_]*): Unit =
    descriptors.foreach { pd =>
      container.addContainerProperty(pd.id, pd.clazz, pd.defaultValue)
    }

  implicit def fnToTableCellStyleGenerator(fn: (ItemId,  PropertyId) => String ) =
    new Table.CellStyleGenerator {
      def getStyle(itemId: AnyRef, propertyId: AnyRef) = fn(itemId, propertyId)
    }

  implicit def fnToTableColumnGenerator(fn: (Table, ItemId, ColumnId) => AnyRef) =
    new Table.ColumnGenerator {
      def generateCell(source: Table, itemId: ItemId, columnId: AnyRef) = fn(source, itemId, columnId)
    }





//  def whenSelected[A, B](property: Property)(fn: A => B): Option[B] = property.getValue match {
//    case null => None
//    case value: A => Some(fn(value))
//    case other => sys.error("Unexpected field value: %s." format other)
//  }

  def whenSelected[A <: AnyRef, B](property: GenericProperty[A] with AbstractSelect)(fn: A => B): Option[B] = property.value match {
    case null => None
    case value: JCollection[_] if value.isEmpty => None
    case value => Some(fn(value))
  }


  def whenSingle[A, B](seq: Seq[A])(fn: A => B): Option[B] = seq match {
    case Seq(a) => Some(fn(a))
    case _ => None
  }

  def whenNotEmpty[A, B](seq: Seq[A])(fn: Seq[A] => B): Option[B] = if (seq.isEmpty) None else Some(fn(seq))


  implicit def applicationToImcmsApplication(app: Application) = app.asInstanceOf[ImcmsApplication]


  implicit def wrapApplication(app: Application) = new ApplicationWrapper(app)



  implicit def wrapValueChangeNotifier(vcn: Property.ValueChangeNotifier) = new {
    def addValueChangeListener(listener: Property.ValueChangeEvent => Unit): Unit =
      vcn.addListener(new Property.ValueChangeListener {
        def valueChange(event: ValueChangeEvent): Unit = listener(event)
      })

    def addValueChangeHandler(handler: => Unit): Unit = addValueChangeListener(_ => handler)
  }

  implicit def wrapItemClickNotifier(notifier: ItemClickEvent.ItemClickNotifier) = new {
    def addItemClickListener(listener: ItemClickEvent => Unit): Unit =
      notifier.addListener(new ItemClickEvent.ItemClickListener {
        def itemClick(event: ItemClickEvent): Unit = listener(event)
      })
  }

  trait ContainerItemSetChangeNotifier extends Container.ItemSetChangeNotifier { container: Container =>

    private var listeners = Set.empty[ItemSetChangeListener]

    def removeListener(listener: ItemSetChangeListener) {
      listeners -= listener
    }

    def addListener(listener: ItemSetChangeListener) {
      listeners += listener
    }

    protected def notifyItemSetChanged() {
      val event = new Container.ItemSetChangeEvent {
        def getContainer = container
      }

      listeners.foreach(_ containerItemSetChange event)
    }
  }




  implicit def stringToUserError(string: String) = new UserError(string)
}