package com.imcode
package imcms
package vaadin

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.vaadin.ui._
import com.vaadin.data.{Item, Container, Property}
import com.vaadin.terminal.Sizeable
import java.util.Collections

trait TCSDefaultI18n extends TwinColSelect {
  setLeftColumnCaption("tcs.col.available.caption".i)
  setRightColumnCaption("tcs.col.selected.caption".i)
}

/**
 * A container property.
 *
 * @param A container property class
 * @pram id container property id
 * @pram defaultValue container property default value
 */
case class ContainerProperty[A <: AnyRef : Manifest](id: AnyRef, defaultValue: A = null) {
  val clazz = implicitly[Manifest[A]].erasure
}

/**
 * Property value type.
 *
 * Adds type-checked access to property value.
 */
trait ValueType[A <: AnyRef] extends Property {
  def value = getValue.asInstanceOf[A]
  def value_=(v: A) = setValue(v)
  def valueOpt: Option[A] = Option(value) // multiselect?

  def clear(implicit ev: A =:= String) { setValue("") }
  def trim(implicit ev: A =:= String): String = value.trim
  def trimOpt(implicit ev: A =:= String): Option[String] = trim match {
    case "" => None
    case v => Some(v)
  }
  def isBlank(implicit ev: A =:= String) = trim.isEmpty
  def notBlank(implicit ev: A =:= String) = !isBlank
}

trait ItemIdType[A <: AnyRef] extends Container {
  def itemIds = getItemIds.asInstanceOf[JCollection[A]]
  def itemIds_=(ids: JCollection[A]) {
    removeAllItems()
    ids.foreach(addItem _)
  }

  def item(id: A) = getItem(id)

  def firstItemIdOpt = itemIds.headOption
}



/**
 * Component data type.
 *
 * Adds type-checked access to data.
 */
trait DataType[A <: AnyRef] extends AbstractComponent {
  def data = getData.asInstanceOf[A]
  def data_=(d: A) = setData(d)
}

trait Disabled { this: Component =>
  setEnabled(false)
}


trait ReadOnly { this: Component =>
  setReadOnly(true)
}

trait Checked { this: CheckBox =>
  setValue(true)
}

trait Unchecked { this: CheckBox =>
  setValue(false)
}

/** Changes fireClick visibility from protected to public. */
trait ExposeFireClick extends Button {
  override def fireClick() = super.fireClick()
}

trait ExposeValueChange extends AbstractField {
  override def fireValueChange(repaintIsNotNeeded: Boolean = true) = super.fireValueChange(repaintIsNotNeeded)
}

trait OnceOnlyAttachAction extends AbstractComponent {

  var attachAction = Option.empty[this.type => Unit]

  override def attach() {
    attachAction foreach (_ apply this)
    attachAction = None
  }
}

/**
 * Always fires value change event on value change even if new and old values are equal.
 */
trait AlwaysFireValueChange extends AbstractField {
  override def setValue(value: AnyRef) {
    if (getValue == value) super.fireValueChange(true)
    else super.setValue(value)
  }
}

trait Margin { this: Layout =>
  setMargin(true)
}

trait NoMargin { this: Layout =>
  setMargin(false)
}

trait BottomMarginDialog extends Dialog {
  content.setMargin(false, false, true, false)
}

trait NoMarginDialog extends Dialog {
  content.setMargin(false)
}

trait Spacing { this: Layout.SpacingHandler =>
  setSpacing(true)
}

trait NoSpacing { this: Layout.SpacingHandler =>
  setSpacing(false)
}

trait UndefinedSize { this: AbstractComponent =>
  setSizeUndefined
}


trait Scrollable { this: com.vaadin.terminal.Scrollable =>
  setScrollable(true)
}

trait FullSize { this: AbstractComponent =>
  setSizeFull
}

trait FullWidth { this: AbstractComponent =>
  setWidth("100%")
}

trait FullHeight { this: AbstractComponent =>
  setHeight("100%")
}

trait LinkStyle { this: Button =>
  setStyleName(Button.STYLE_LINK)
}

trait SmallStyle { this: Button =>
  setStyleName("small")
}

trait LightStyle { this: Panel =>
  setStyleName(Panel.STYLE_LIGHT)
}

trait Immediate { this: AbstractComponent =>
  setImmediate(true)
}

// Tree and Table
trait Selectable { this: {def setSelectable(selectable: Boolean): Unit} =>
  setSelectable(true)
}

trait NotSelectable { this: {def setSelectable(selectable: Boolean): Unit} =>
  setSelectable(false)
}

trait NullSelection extends AbstractSelect {
  setNullSelectionAllowed(true)
}

trait NoNullSelection extends AbstractSelect {
  setNullSelectionAllowed(false)
}


trait SelectionCheck extends AbstractSelect {
  def isSelected: Boolean
}


trait SelectOps[A <: AnyRef] extends AbstractSelect with ItemIdType[A] {
  def addItem(id: A, caption: String): Item = doto(addItem(id)) { _ =>
    setItemCaption(id, caption)
  }
}


trait SingleSelect[A <: AnyRef] extends SelectionCheck with SelectOps[A] with ValueType[A]  {
  setMultiSelect(false)

  def isSelected = value != null

  override def setMultiSelect(multiSelect: Boolean) {
    require(!multiSelect, "must be false")
    super.setMultiSelect(multiSelect)
  }
}

trait MultiSelect[A <: AnyRef] extends SelectionCheck with SelectOps[A] with ValueType[JCollection[A]] {
  setMultiSelect(true)

  override def setMultiSelect(multiSelect: Boolean) {
    require(multiSelect, "must be true")
    super.setMultiSelect(multiSelect)
  }

  def isSelected = value.nonEmpty

  def selection = value.toSeq

  def selection_=(v: Seq[A]) { value = v }

  def selection_=(v: Option[A]) { selection = v.toSeq }

  def first = value.headOption
}




/**
 * Type check <code>value<code> property always returns a collection.
 */
trait MultiSelectBehavior[A <: AnyRef] extends SelectionCheck with SelectOps[A] with ValueType[JCollection[A]] {

  def selection = value.toSeq

  def selection_=(v: Seq[A]) { value = v }

  def selection_=(v: Option[A]) { selection = v.toSeq }

  def first = value.headOption

  def isSelected = value.nonEmpty

  /**
   * @return collection of selected items or empty collection if there is no selected item(s).
   */
  final override def getValue() = super.getValue |> { v => if (isMultiSelect) v else ?(v).toSeq.asJavaCollection }

  final override def setMultiSelect(multiSelect: Boolean) =
    if (value.isEmpty) super.setMultiSelect(multiSelect)
    else throw new IllegalStateException("Multi-select value can not be changed on non-empty select.")


  final override def setValue(v: AnyRef) {
    super.setValue(
      v match {
        case null => if (isMultiSelect) Collections.emptyList[AnyRef] else null
        case coll: JCollection[_] => if (isMultiSelect) coll else coll.headOption.orNull
        case _ => if (isMultiSelect) Collections.singletonList(v) else v
      }
    )
  }
}

trait Now extends DateField {
  setValue(new java.util.Date)
}

trait YearResolution extends DateField {
  setResolution(DateField.RESOLUTION_YEAR)
}

trait MonthResolution extends DateField {
  setResolution(DateField.RESOLUTION_MONTH)
}

trait DayResolution extends DateField {
  setResolution(DateField.RESOLUTION_DAY)
}

trait MinuteResolution extends DateField {
  setResolution(DateField.RESOLUTION_MIN)
}

trait Required extends Field {
  setRequired(true)
}

trait NoTextInput extends ComboBox {
  setTextInputAllowed(false)
}

//trait UndefiedWidth { this: AbstractComponent =>
//  setSizeFull
//  setWid
//}
//
//trait UndefiedHeight { this: Layout.SpacingHandler =>
//  setSpacing(true)
//}


object Checks {
  def assertFixedSize(c: Component) {
    require(c.getWidthUnits != Sizeable.UNITS_PERCENTAGE, "Component width must not be difined in percentage.")
    require(c.getHeightUnits != Sizeable.UNITS_PERCENTAGE, "Component height must not be difined in percentage.")
  }
}

case class FunctionProperty[A](valueFn: () => A)(implicit mf: Manifest[A]) extends Property {

  def setReadOnly(newStatus: Boolean) = throw new UnsupportedOperationException

  val isReadOnly = true

  val getType = mf.erasure

  def setValue(newValue: AnyRef) = throw new UnsupportedOperationException

  def getValue = valueFn().asInstanceOf[AnyRef]

  override def toString = ?(getValue) map { _.toString } getOrElse ""
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

