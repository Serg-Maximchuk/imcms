package com.imcode
package imcms
package admin.system.file

import scala.collection.JavaConversions._
import com.vaadin.ui._
import scala.collection.mutable.{Map => MMap}
import com.vaadin.terminal.{Resource}
import com.imcode.imcms.vaadin.{ContainerProperty => CP, _}
import com.vaadin.data.util.FilesystemContainer
import java.io.{FilenameFilter, File}
import java.util.concurrent.atomic.AtomicReference
import com.imcode.util.event.Publisher
import imcode.server.Imcms

/** Hierarchical filesystem (non-hidden dirs) container with a single root. */
class LocationTreeContainer(root: File) extends FilesystemContainer(root) {

  import java.util.Collections._

  setFilter(new FilenameFilter {
    def accept(file: File, name: String) = let(new File(file, name)) { fsNode =>
      fsNode.isDirectory && !fsNode.isHidden
    }
  })

  override def rootItemIds() = root |> singleton[File] |> unmodifiableCollection[File]

  override def addRoot(root: File) = sys.error("Operation is not allowed.")
}


/** Predefined dir items filters. */
object LocationItemsFilter {

  import scala.util.matching.Regex

  /** Creates a compund filter from a sequence of filters. */
  def apply(filter: File => Boolean, filters: File => Boolean*) = (file: File) => filter +: filters forall { _ apply file }

  val notHidden = apply(!_.isHidden)

  //val fileOnly = apply(notHidden, _.isFile)

  def nameRE(re: Regex)(fsNode: File) = re.unapplySeq(fsNode.getName).isDefined

  def fileWithExt(ext: String, exts: String*) = nameRE("""(?i).*\.(%s)""".format(ext +: exts mkString("|")).r)_

  val imageFile = fileWithExt("png", "gif", "jpg", "jpeg")

  val templateFile = fileWithExt("jsp", "jspx", "html")
}


/**
 * File browser location (bookmark) conf.
 */
case class LocationConf(dir: File, relativePath: String = "", itemsFilter: File => Boolean = LocationItemsFilter.notHidden, recursive: Boolean = true)


case class LocationSelection(dir: File, items: Seq[File]) {
  def firstItem = items.headOption
  def hasItems = items.nonEmpty
  def hasSingleItem = items.size == 1
}


object ImcmsFileBrowser {

  def addLocation(captionResourceId: String, conf: LocationConf, image: Option[Resource])(browser: FileBrowser) =
    letret(browser) { _ => browser.addLocation(captionResourceId.i, conf, image) }

  val addHomeLocation = addLocation("file.browser.location.home", LocationConf(Imcms.getPath), ?(Theme.Icons.Browser.TabHome32))_

  val addImagesLocation =
    addLocation("file.browser.location.images", LocationConf(Imcms.getPath, "images"), ?(Theme.Icons.Browser.TabImages32))_

  val addTemplatesLocation =
    addLocation("file.browser.location.templates", LocationConf(Imcms.getPath, "WEB-INF/templates/text"), ?(Theme.Icons.Browser.TabTemplates32))_

  val addLogsLocation =
    addLocation("file.browser.location.logs", LocationConf(Imcms.getPath, "WEB-INF/logs"), ?(Theme.Icons.Browser.TabLogs32))_

  val addConfLocation =
    addLocation("file.browser.location.conf", LocationConf(Imcms.getPath, "WEB-INF/conf"), ?(Theme.Icons.Browser.TabConf32))_

  val addAllLocations =
    Function.chain(Seq(addHomeLocation, addImagesLocation, addTemplatesLocation, addLogsLocation, addConfLocation))
}



/**
 * A file browser can have any number of locations (bookmarks).
 * A location is uniquely identified by its root dir.
 */
class FileBrowser(val isSelectable: Boolean = true, val isMultiSelect: Boolean = false)
    extends Publisher[Option[LocationSelection]] {

  type Location = (LocationTree, LocationItems)
  type LocationRoot = File
  type Tab = Component // location tree ui

  private val locationRootToConf = MMap.empty[LocationRoot, LocationConf]
  private val tabsToLocations = MMap.empty[Tab, Location]

  /** Current (visible) location. */
  private val locationRef = new AtomicReference(Option.empty[Location])

  /** Selection in a current location */
  private val selectionRef = new AtomicReference(Option.empty[LocationSelection])

  val ui = letret(new FileBrowserUI) { ui =>
    ui.accLocationTrees.addListener(new TabSheet.SelectedTabChangeListener {
      def selectedTabChange(e: TabSheet#SelectedTabChangeEvent) {
        val locationOpt = tabsToLocations.get(e.getTabSheet.getSelectedTab)
        locationRef.set(locationOpt)

        for ((locationTree, locationItems) <- locationOpt) {
          ui.spLocation.setSecondComponent(locationItems.ui)
          updateSelection(locationTree, locationItems)
        }
      }
    })
  }

  def addLocation(caption: String, conf: LocationConf, icon: Option[Resource] = None) {
    val locationRoot = new File(conf.dir, conf.relativePath)
    val locationTree = new LocationTree(locationRoot)
    val locationItems = new LocationItems(conf.itemsFilter, isSelectable, isMultiSelect)

    locationTree.ui addValueChangeHandler {
      ?(locationTree.ui.value) match {
        case Some(dir) =>
          locationItems.reload(dir)
          let(Some(LocationSelection(dir, Nil))) { selection =>
            selectionRef.set(selection)
            notifyListeners(selection)
          }

        case _ =>
          locationItems.ui.removeAllItems()

          let(None) { selection =>
            selectionRef.set(selection)
            notifyListeners(selection)
          }
      }
    }

    locationItems.ui addValueChangeHandler { updateSelection(locationTree, locationItems) }

    locationItems.ui.addItemClickListener {
      case e if e.isDoubleClick => e.getItemId match {
        case item: File if item.isDirectory => locationTree.selection = item
        case _ =>
      }

      case _ =>
    }

    locationTree.reload()

    locationRootToConf(locationRoot) = conf
    tabsToLocations(locationTree.ui) = (locationTree, locationItems)
    ui.accLocationTrees.addTab(locationTree.ui, caption, icon.orNull)
  }

  private def updateSelection(locationTree: LocationTree, locationItems: LocationItems) {
    ?(locationTree.ui.value) match {
      case Some(dir) =>
        let(Some(LocationSelection(dir, locationItems.selection))) { selection =>
          selectionRef.set(selection)
          notifyListeners(selection)
        }

      case _ =>
        let(None) { selection =>
          selectionRef.set(selection)
          notifyListeners(selection)
        }
    }
  }

  override def notifyListeners() = notifyListeners(selection)

  /** Returns selection in a current location. */
  def selection = selectionRef.get

  /** Returns current (visible) location */
  def location = locationRef.get

  def locations: Map[File, Location] =
    tabsToLocations.values map {
      case loc @ (locationTree, _) => locationTree.root.getCanonicalFile -> loc
    } toMap

  /** Returns location by its root. */
  def location(root: File): Option[Location] = locations.get(root.getCanonicalFile)

  /** Reloads current location. */
  def reloadLocation(preserveTreeSelection: Boolean = true) =
    for ((locationTree, _) <- location; dir = locationTree.ui.value) {
      locationTree.reload()
      if (preserveTreeSelection && dir.isDirectory) locationTree.selection = dir
    }

  /** Reloads current location's items. */
  def reloadLocationItems() =
    for ((locationTree, locationItems) <- location; dir = locationTree.ui.value)
      locationItems.reload(dir)


  /**
   * Changes current selection.
   * Also changes current location if its root is other than provided locationRoot.
   */
  def select(locationRoot: File, dir: File, items: Seq[File] = Nil): Unit =
    select(locationRoot, new LocationSelection(dir, items))

  /**
   * Changes current selection.
   * Also changes current location if its root is other than provided locationRoot.
   */
  def select(locationRoot: File, locationSelection: LocationSelection) =
    tabsToLocations.find {
      case (_, (locationTree, _)) => locationTree.root.getCanonicalFile == locationRoot.getCanonicalFile
    } foreach {
      case (tab, (locationTree, locationItems)) =>
        ui.accLocationTrees.setSelectedTab(tab)
        locationTree.selection = locationSelection.dir
        if (isSelectable) locationItems.ui.value = locationSelection.items
    }

  // primary constructor
  listen { e =>
    ui.lblSelectionPath.value = {
      val pathOpt =
        for {
          LocationSelection(dir, items) <- e
          (locationTree, _) <- location
          conf <- locationRootToConf.get(locationTree.root)
          confParent = ?(conf.dir.getParentFile).map(_.getCanonicalFile).orNull
          dirs = Iterator.iterate(dir)(_.getParentFile).takeWhile(_.getCanonicalFile != confParent).toList.reverse
          dirPath = dirs.map(_.getName).mkString("", "/", "/")
        } yield {
          dirPath + (items match {
            case Nil => ""
            case Seq(item) => item.getName
            case _ => "..."
          })
        }

      pathOpt getOrElse ""
    }
  }

  notifyListeners()
}


class FileBrowserUI extends VerticalLayout with Spacing with FullSize {
  val spLocation = new HorizontalSplitPanel with FullSize
  val accLocationTrees = new Accordion with FullSize
  val lblSelectionPath = new Label

  spLocation.setFirstComponent(accLocationTrees)
  spLocation.setSplitPosition(15)

  addComponents(this, spLocation, lblSelectionPath)
  setExpandRatio(spLocation, 1.0f)
}


/** Select item can be a dir or a file. */
trait FSItemIcon extends AbstractSelect {
  override def getItemIcon(itemId: AnyRef) =
    if (itemId.asInstanceOf[File].isDirectory) Theme.Icons.Folder16 else Theme.Icons.File16
}


class LocationTree(val root: File) {
  val ui = new Tree with SingleSelect2[File] with Immediate with NoNullSelection with FSItemIcon

  def reload() {
    ui.setContainerDataSource(new LocationTreeContainer(root.getCanonicalFile))
    ui.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_ITEM)
    selection = root
  }

  def selection_=(dir: File) = let(dir.getCanonicalFile) { dir =>
    ui.select(dir)
    ui.expandItem(if (dir == root) dir else dir.getParentFile)
  }

  def selection = ui.value
}


class LocationItems(filter: File => Boolean, selectable: Boolean, multiSelect: Boolean) {

  val ui = new Table with MultiSelectBehavior[File] with FSItemIcon with Immediate with FullSize { ui =>
    ui.setSelectable(selectable)
    ui.setMultiSelect(multiSelect)

    addContainerProperties(ui,
      CP[String]("file.browser.items.col.name".i),
      CP[String]("file.browser.items.col.modified".i),
      CP[String]("file.browser.items.col.size".i),
      CP[String]("file.browser.items.col.kind".i))

    import Table._
    ui.setColumnAlignments(Array(ALIGN_LEFT, ALIGN_RIGHT, ALIGN_RIGHT, ALIGN_RIGHT))
    ui.setRowHeaderMode(ROW_HEADER_MODE_ICON_ONLY);
  }

  /** Populates table with dir items. */
  def reload(dir: File) {
    val base = 1024
    val baseFn = java.lang.Math.pow(1024, _:Int).toInt
    val (dirs, files) = dir.listFiles.partition(_.isDirectory)
    def lastModified(file: File) = "file.browser.items.col.modified.fmt".f(file.lastModified.asInstanceOf[AnyRef])

    ui.removeAllItems()

    dirs.sortWith((d1, d2) => d1.getName.compareToIgnoreCase(d2.getName) < 0) foreach { dir =>
      ui.addItem(Array[AnyRef](dir.getName, lastModified(dir), "--", "file.browser.items.col.kind.dir".i), dir)
    }

    for (file <- files.sortWith((f1, f2) => f1.getName.compareToIgnoreCase(f2.getName) < 0) if filter(file)) {
      val (size, units) = file.length match {
        case size if size < baseFn(1) => (0, "KB")
        case size if size < baseFn(2) => (size / base, "KB")
        case size if size < baseFn(3) => (size / base, "MB")
        case size => (size / base, "GB")
      }

      ui.addItem(Array[AnyRef](file.getName, lastModified(file), "%d %s".format(size, units), "file.browser.items.col.kind.file".i), file)
    }
  }

  def selection = ui.value.toSeq
}