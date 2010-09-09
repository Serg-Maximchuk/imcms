package com.imcode.imcms.servlet.superadmin.vaadin;

import java.lang.{Boolean => JBoolean, Integer => JInteger}

import scala.collection.JavaConversions._
import com.imcode._
import com.vaadin.event.ItemClickEvent
import com.vaadin.terminal.gwt.server.WebApplicationContext
import com.vaadin.ui._
import com.vaadin.data.Property
import com.vaadin.data.Property._
import com.imcode.imcms.dao.{MetaDao, SystemDao, LanguageDao, IPAccessDao}
import imcms.servlet.superadmin.AdminSearchTerms
import imcode.server.document.DocumentDomainObject
import com.imcode.imcms.api.Document.PublicationStatus
import com.vaadin.terminal.UserError
import imcode.util.Utility
import imcode.server.user._
import com.imcode.imcms.api.{SystemProperty, IPAccess, Document}
import imcode.server.{SystemData, Imcms}
import java.util.Date

/** Creates root item; root is not displayed */
class MenuItem(val parent: MenuItem = null, val handler: () => Unit = () => {}) {

  import collection.mutable.ListBuffer

  private val itemsBuffer = new ListBuffer[MenuItem]

  def items = itemsBuffer.toList

  override val toString = getClass.getName split '$' last

  val id = {
    def pathToRoot(m: MenuItem): List[MenuItem] = m :: (if (m.parent == null) Nil else pathToRoot(m.parent))

    pathToRoot(this).reverse map (_.toString) map camelCaseToUnderscore mkString "."
  }

  if (parent != null) parent.itemsBuffer += this

  // forces initialization of items declared as inner objects
  for (m <- getClass.getDeclaredMethods if m.getParameterTypes().length == 0)
    m.invoke(this)
}


class TabView extends VerticalLayout {
  val tabSheet = new TabSheet
  addComponent(tabSheet)
  setMargin(true)

  def addTab(c: Component) = tabSheet.addTab(c)
}


class ViewVerticalLayout(caption: String = "") extends VerticalLayout {
  setCaption(caption)
  setMargin(true)
  setSpacing(true)
}


class App extends com.vaadin.Application {

  setTheme("imcms")
  
  object Menu extends MenuItem {
    object About extends MenuItem(this)
    object Settings extends MenuItem(this) {
      object Languages extends MenuItem(this)
      object Properties extends MenuItem(this)
    }
    object Documents extends MenuItem(this) {
      object Categories extends MenuItem(this)
      object Templates extends MenuItem(this)
    }
    object Permissions extends MenuItem(this) {
      object Users extends MenuItem(this)
      object Roles extends MenuItem(this)
      object IP_Access extends MenuItem(this)
    }
    object Statistics extends MenuItem(this) {
      object SearchTerms extends MenuItem(this)
      object SessionCounter extends MenuItem(this)
    }
    object Filesystem extends MenuItem(this)
  }
  
  type ButtonClickHandler = Button#ClickEvent => Unit
  type PropertyValueChangeHandler = ValueChangeEvent => Unit 

  val languageDao = Imcms.getSpringBean("languageDao").asInstanceOf[LanguageDao]
  val systemDao = Imcms.getSpringBean("systemDao").asInstanceOf[SystemDao]
  val ipAccessDao = Imcms.getSpringBean("ipAccessDao").asInstanceOf[IPAccessDao]

  implicit def BlockToButtonClickListener(handler: => Unit): Button.ClickListener =
    new Button.ClickListener {
      def buttonClick(event: Button#ClickEvent) = handler
    }  

//  def addButtonClickListener(button: Button)(handler: ButtonClickHandler) {
//    button addListener new Button.ClickListener {
//      def buttonClick(event: Button#ClickEvent) = handler(event)
//    }
//  }

  implicit def BlockToPropertyValueChangeListener(block: => Unit): Property.ValueChangeListener =
    new Property.ValueChangeListener {
      def valueChange(event: ValueChangeEvent) = block
    }

//  def addValueChangeHandler(target: AbstractField)(handler: ValueChangeEvent => Unit) {
//    target addListener new Property.ValueChangeListener {
//      def valueChange(event: ValueChangeEvent) = handler(event)
//    }
//  }

  def initAndShow[W <: Window](window: W, modal: Boolean = true)(init: W => Unit) {
    init(window)
    window setModal modal
    wndMain addWindow window
  }

  def addComponents(container: AbstractComponentContainer, components: Component*) = {
    components foreach { c => container addComponent c }
    container
  }

  def addContainerProperties(table: Table, properties: (AnyRef, java.lang.Class[_], AnyRef)*) =
    for ((propertyId, propertyType, defaultValue) <- properties)
      table.addContainerProperty(propertyId, propertyType, defaultValue)

  class DialogWindow(caption: String) extends Window(caption) {
    val lytContent = new GridLayout(1, 2)

    lytContent setMargin true
    lytContent setSpacing true
    lytContent setSizeFull

    // auto size
    setContent(new VerticalLayout {
      addComponent(lytContent)
      setSizeUndefined
    })

    def setMainContent(c: Component) {
      lytContent.addComponent(c, 0, 0)
      lytContent.setComponentAlignment(c, Alignment.BOTTOM_CENTER)
    }
    
    def setButtonsContent(c: Component) {
      lytContent.addComponent(c, 0, 1)
      lytContent.setComponentAlignment(c, Alignment.TOP_CENTER)
    }
  }

  /** Message dialog window. */
  class MsgDialog(caption: String, msg: String) extends DialogWindow(caption) {
    val btnOk = new Button("Ok")
    val lblMessage = new Label(msg)

    setMainContent(lblMessage)
    setButtonsContent(btnOk)

    btnOk addListener close
  }

  /** OKCancel dialog window. */
  class OkCancelDialog(caption: String) extends DialogWindow(caption) {
    val btnOk = new Button("Ok")
    val btnCancel = new Button("Cancel")
    val lytButtons = new GridLayout(2, 1) {
      setSpacing(true)
      addComponent(btnOk)
      addComponent(btnCancel)
      setComponentAlignment(btnOk, Alignment.MIDDLE_RIGHT)
      setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT)
    }
    
    setButtonsContent(lytButtons)

    btnCancel addListener close

    def addOkButtonClickListener(block: => Unit) {
      btnOk addListener {
        try {
          block
          close
        } catch {
          case ex: Exception => using(new java.io.StringWriter) { w =>
            ex.printStackTrace(new java.io.PrintWriter(w))
            show(new MsgDialog("ERROR", "%s  ##  ##  ##  ## ## %s" format (ex.getMessage, w.getBuffer)))
            throw ex
          }
        }
      }
    }
  }

  /** Confirmation dialog window. */
  class ConfirmationDialog(caption: String, msg: String) extends OkCancelDialog(caption) {
    val lblMessage = new Label(msg)

    setMainContent(lblMessage)
  }

  abstract class TableViewTemplate extends GridLayout(1,3) {
    val tblItems = new Table {
      setSelectable(true)
      setImmediate(true)
      setPageLength(10)

      setSizeFull

      addListener { resetComponents }

      tableProperties foreach { p => addContainerProperties(this, p) }
    }

    val pnlHeader = new Panel {
      val layout = new HorizontalLayout {
        setSpacing(true)
      }

      setContent(layout)
    }

    val btnReload = new Button("Reload") {
      addListener { reloadTableItems }
    }
    
    val pnlFooter = new Panel {
      val layout = new GridLayout(1,1) {
        addComponent(btnReload)
        setComponentAlignment(btnReload, Alignment.MIDDLE_RIGHT)
        setSizeFull
      }

      setContent(layout)
    }

    addComponents(this, pnlHeader, tblItems, pnlFooter)

    // Investigate: List[(AnyRef, Array[AnyRef])]
    def tableItems(): List[(AnyRef, List[AnyRef])] = List.empty

    def tableProperties: List[(AnyRef, java.lang.Class[_], AnyRef)] = List.empty

    def reloadTableItems {
      tblItems.removeAllItems

      for((id, cells) <- tableItems()) tblItems.addItem(cells.toArray, id)
      //for ((id:, cells:) <- tableItems()) tblItems.addItem(cells, id)
    }

    def resetComponents = {}
    
    reloadTableItems
    resetComponents
  }

  case class Node(nodes: Node*)

  def NA(id: Any) = new ViewVerticalLayout {
    addComponent(new Panel(id.toString) {
      let(getContent.asInstanceOf[VerticalLayout]) { c =>
        c.setMargin(true)
        c.setSpacing(true)
      }

      addComponent(new Label("NOT AVAILABLE"))
    })
  }

  val labelAbout = new ViewVerticalLayout {
    addComponent(new Panel("About") {
      let(getContent.asInstanceOf[VerticalLayout]) { c =>
        c.setMargin(true)
        c.setSpacing(true)
      }

      addComponent(new Label("""|Welcome to the imCMS new admin UI prototype -
                     | please pick a task from the menu. Note that some views are not (yet) available. 
                     |""".stripMargin))
    })
  }

  val wndMain = new Window {
    val content = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL)
    val treeMenu = new Tree {
      setImmediate(true)
    }

    def initMenu(menu: MenuItem) {
      treeMenu addItem menu

      menu.parent match {
        case null => ()
        case parent => treeMenu.setParent(menu, parent)
      }

      menu.items match {
        case Nil => treeMenu setChildrenAllowed (menu, false)
        case items => items foreach initMenu
      }
    }

    treeMenu addListener (new ValueChangeListener {
      def valueChange(e: ValueChangeEvent) {
        content.setSecondComponent(
          e.getProperty.getValue match {
            case null | Menu.About => labelAbout
            
            case Menu.Statistics.SearchTerms => searchTerms
            case Menu.Documents.Categories => categories
            case Menu.Settings.Languages => languagesPanel
            case Menu.Settings.Properties => settingsProperties
            case Menu.Statistics.SessionCounter => settingSessionCounter
            case Menu.Documents => documentsTable
            case Menu.Permissions.Roles => roles
            case Menu.Permissions.Users => users
            case Menu.Permissions.IP_Access => ipAccess

            case other => NA(other)
          })
      }
    })

    content setFirstComponent treeMenu
    this setContent content
  }

  def show(wndChild: Window, modal: Boolean = true) {
    wndChild setModal modal
    wndMain addWindow wndChild
  }

  def init {
    wndMain initMenu Menu
    this setMainWindow wndMain

    wndMain.treeMenu expandItemsRecursively Menu
    wndMain.treeMenu select Menu.About    
  }

  //
  // Languages panel
  // 
  def languagesPanel = {
    class LanguageWindow(caption: String) extends OkCancelDialog(caption) {
      val txtId = new TextField("Id")
      val txtCode = new TextField("Code")
      val txtName = new TextField("Name")
      val txtNativeName = new TextField("Native name")
      val chkEnabled = new CheckBox("Enabled")

      val lytMainContent = new FormLayout

      // lytMainContent setMargin true
      
      addComponents(lytMainContent, txtId, txtCode, txtName, txtNativeName, chkEnabled)

      setMainContent(lytMainContent)
    }

    val pnlLanguages = new Panel

    
    val table = new Table

    table setPageLength 10
    table setSelectable true
    table setImmediate true

    table.addContainerProperty("Id", classOf[JInteger],  null)
    table.addContainerProperty("Code", classOf[String],  null)
    table.addContainerProperty("Name", classOf[String],  null)
    table.addContainerProperty("Native name", classOf[String],  null)
    table.addContainerProperty("Enabled", classOf[JBoolean],  null)
    table.addContainerProperty("Default", classOf[JBoolean],  null)

    def reloadTable {
      table.removeAllItems

      val defaultLanguageId = Int box systemDao.getProperty("DefaultLanguageId").getValue.toInt

      languageDao.getAllLanguages.toList foreach { language =>
        table.addItem(Array(language.getId, language.getCode, language.getName,
                            language.getNativeName, language.isEnabled,
                            Boolean box (language.getId == defaultLanguageId)),
                      language.getId)
      }
    }    

    val pnlControls = new Panel with Button.ClickListener {
      val btnNew = new Button("New")
      val btnEdit = new Button("Edit")
      val btnSetDefault = new Button("Set default")
      val btnDelete = new Button("Delete")

      val layout = new HorizontalLayout
      setContent(layout)
      layout.setSpacing(true)

      List(btnNew, btnEdit, btnSetDefault, btnDelete).foreach { btn =>
        this addComponent btn
        btn addListener this
      }

      def buttonClick(clickEvent: Button#ClickEvent) {
        val defaultLanguageId = Int box systemDao.getProperty("DefaultLanguageId").getValue.toInt

        clickEvent.getButton match {

          case `btnNew` =>
            def isInt(x:Any) = x match {
              case n: Int => true
              case s: String => s.nonEmpty && s.forall(_.isDigit)
              case _ => false
            }

            initAndShow(new LanguageWindow("New language")) { wndEditLanguage =>
              val language = new com.imcode.imcms.api.I18nLanguage

              wndEditLanguage addOkButtonClickListener {
                if (!isInt(wndEditLanguage.txtId.getValue)) {
                  wndEditLanguage.txtId.setComponentError(new UserError("Id must be an Int"))
                } else {
                  language.setId(Int box wndEditLanguage.txtId.getValue.asInstanceOf[String].toInt)
                  language.setCode(wndEditLanguage.txtCode.getValue.asInstanceOf[String])
                  language.setName(wndEditLanguage.txtName.getValue.asInstanceOf[String])
                  language.setNativeName(wndEditLanguage.txtNativeName.getValue.asInstanceOf[String])
                  language.setEnabled(wndEditLanguage.chkEnabled.getValue.asInstanceOf[JBoolean])

                  languageDao.saveLanguage(language)
                  reloadTable
                }
              }
            }

          case `btnEdit` =>
            val languageId = table.getValue.asInstanceOf[JInteger]
            val language = languageDao.getById(languageId)

            initAndShow(new LanguageWindow("Edit language")) { wndEditLanguage =>
              wndEditLanguage.txtId.setValue(language.getId)
              wndEditLanguage.txtId.setEnabled(false)
              wndEditLanguage.txtCode.setValue(language.getCode)
              wndEditLanguage.txtName.setValue(language.getName)
              wndEditLanguage.txtNativeName.setValue(language.getNativeName)
              wndEditLanguage.chkEnabled.setValue(language.isEnabled)

              wndEditLanguage addOkButtonClickListener {
                language.setCode(wndEditLanguage.txtCode.getValue.asInstanceOf[String])
                language.setName(wndEditLanguage.txtName.getValue.asInstanceOf[String])
                language.setNativeName(wndEditLanguage.txtNativeName.getValue.asInstanceOf[String])
                language.setEnabled(wndEditLanguage.chkEnabled.getValue.asInstanceOf[JBoolean])

                languageDao.saveLanguage(language)
                reloadTable
              }
            }

          case `btnSetDefault` =>
            initAndShow(new ConfirmationDialog("Confirmation", "Change default language?")) { wndConfirmation =>
              wndConfirmation addOkButtonClickListener {
                val languageId = table.getValue.asInstanceOf[JInteger]
                val property = systemDao.getProperty("DefaultLanguageId")

                property.setValue(languageId.toString)
                systemDao.saveProperty(property)
                reloadTable
              }
            }

          case `btnDelete` =>
            initAndShow(new ConfirmationDialog("Confirmation", "Delete language from the system?")) { wndConfirmation =>
              wndConfirmation addOkButtonClickListener {
                val languageId = table.getValue.asInstanceOf[JInteger]
                languageDao.deleteLanguage(languageId)
                reloadTable
              }
            }
        }
      }
    }    

    def resetControls = {
      val languageId = table.getValue.asInstanceOf[JInteger]

      if (languageId == null) {
        pnlControls.btnDelete.setEnabled(false)
        pnlControls.btnEdit.setEnabled(false)
        pnlControls.btnSetDefault.setEnabled(false)
      } else {
        val defaultLanguageId = Int box systemDao.getProperty("DefaultLanguageId").getValue.toInt

        pnlControls.btnEdit.setEnabled(true)
        pnlControls.btnDelete.setEnabled(languageId != defaultLanguageId)
        pnlControls.btnSetDefault.setEnabled(languageId != defaultLanguageId)
      }
    }

    table addListener resetControls

    reloadTable

    val pnlReloadBar = new Panel(new GridLayout(1,1))
    val btnReload = new Button("Reload")
    pnlReloadBar.addComponent(btnReload)

    btnReload addListener reloadTable

    pnlReloadBar.getContent.setSizeFull
    pnlReloadBar.getContent.asInstanceOf[GridLayout].setComponentAlignment(btnReload, Alignment.MIDDLE_RIGHT)

    val lytLanguages = new GridLayout(1,3)
    pnlLanguages.setContent(lytLanguages)
    lytLanguages.setMargin(true)

    pnlLanguages addComponent pnlControls
    pnlLanguages addComponent table    
    pnlLanguages addComponent pnlReloadBar

    resetControls
    pnlLanguages
  }
  

  def documentsTable = {
    val content = new VerticalLayout
    content.setMargin(true)

    val table = new Table()
    table.addContainerProperty("Page alias", classOf[String],  null)
    table.addContainerProperty("Status", classOf[String],  null)
    table.addContainerProperty("Type", classOf[JInteger],  null)
    table.addContainerProperty("Admin", classOf[String],  null)
    table.addContainerProperty("Ref.", classOf[String],  null)
    table.addContainerProperty("Child documents", classOf[String],  null)

    val metaDao = Imcms.getSpringBean("metaDao").asInstanceOf[MetaDao]

    metaDao.getAllDocumentIds.toList.foreach { id =>
      val meta = metaDao getMeta id
      val alias = meta.getProperties.get(DocumentDomainObject.DOCUMENT_PROPERTIES__IMCMS_DOCUMENT_ALIAS) match {
        case null => ""
        case value => value
      }

      val status = meta.getPublicationStatus match {
        case Document.PublicationStatus.NEW => "New"
        case Document.PublicationStatus.APPROVED => "Approved"
        case Document.PublicationStatus.DISAPPROVED => "Disapproved"
      }

      table.addItem(Array(alias, status, meta.getDocumentType, id.toString, Int box 0, Int box 0), id)
    }

    val controls = new HorizontalLayout
    
    controls.addComponent(new Label("List between:"))
    controls.addComponent(new TextField)
    controls.addComponent(new Label("-"))
    controls.addComponent(new TextField)
    controls.addComponent(new Button("List"))

    content.addComponent(controls)
    content.addComponent(table)

    content
  }
 
  def ipAccess = {
    def toDDN(internalFormat: String) = Utility.ipLongToString(internalFormat.toLong)
    def fromDDN(humanFormat: String) = Utility.ipStringToLong(humanFormat).toString

    class IPAccessWindow(caption: String) extends OkCancelDialog(caption) {
      val sltUser = new Select("Users")
      val txtFrom = new TextField("From")
      val txtTo = new TextField("To")

      val lytMainContent = new FormLayout

      lytMainContent.addComponent(sltUser)
      lytMainContent.addComponent(txtFrom)
      lytMainContent.addComponent(txtTo)

      setMainContent(lytMainContent)
    }
    
    class IPAccessView extends TableViewTemplate {
      lazy val btnAdd = new Button("Add")
      lazy val btnEdit = new Button("Edit")
      lazy val btnDelete = new Button("Delete")

      addComponents(pnlHeader, btnAdd, btnEdit, btnDelete)

      override def tableProperties = List(
        ("User", classOf[String],  null),
        ("IP range from", classOf[String],  null),
        ("IP range to", classOf[String],  null))

      override def tableItems() = ipAccessDao.getAll.toList map { ipAccess =>
        val user = Imcms.getServices.getImcmsAuthenticatorAndUserAndRoleMapper getUser (Int unbox ipAccess.getUserId)
        ipAccess.getUserId -> List(user.getLoginName, toDDN(ipAccess.getStart), toDDN(ipAccess.getEnd))
      }

      override def resetComponents =
        if (tblItems.getValue == null) {
          btnDelete setEnabled false
          btnEdit setEnabled false
        } else {
          btnEdit setEnabled true
          btnDelete setEnabled true
        }

      btnAdd addListener {
        initAndShow(new IPAccessWindow("Add new IP Access")) { w =>
          Imcms.getServices.getImcmsAuthenticatorAndUserAndRoleMapper.getAllUsers foreach { u =>
            w.sltUser addItem u.getId
            w.sltUser setItemCaption (u.getId, u.getLoginName)
          }

          w.addOkButtonClickListener {
            val ipAccess = new IPAccess
            ipAccess setUserId w.sltUser.getValue.asInstanceOf[Integer]
            ipAccess setStart fromDDN(w.txtFrom.getValue.asInstanceOf[String])
            ipAccess setEnd fromDDN(w.txtTo.getValue.asInstanceOf[String])

            ipAccessDao.save(ipAccess)

            reloadTableItems
          }
        }
      }

      btnEdit addListener {
        initAndShow(new IPAccessWindow("Edit IP Access")) { w =>
          Imcms.getServices.getImcmsAuthenticatorAndUserAndRoleMapper.getAllUsers foreach { u =>
            w.sltUser addItem u.getId
            w.sltUser setItemCaption (u.getId, u.getLoginName)
          }

          val ipAccessId = tblItems.getValue.asInstanceOf[JInteger]
          val ipAccess = ipAccessDao get ipAccessId

          w.sltUser select ipAccess.getUserId
          w.txtFrom setValue toDDN(ipAccess.getStart)
          w.txtTo setValue toDDN(ipAccess.getEnd)

          w.addOkButtonClickListener {
            val ipAccess = new IPAccess
            ipAccess setUserId w.sltUser.getValue.asInstanceOf[Integer]
            ipAccess setStart fromDDN(w.txtFrom.getValue.asInstanceOf[String])
            ipAccess setEnd fromDDN(w.txtTo.getValue.asInstanceOf[String])

            ipAccessDao.save(ipAccess)

            reloadTableItems
          }
        }
      }

      btnDelete addListener {
        initAndShow(new ConfirmationDialog("Confirmation", "Delete IP Access?")) { w =>
          w.addOkButtonClickListener {
            ipAccessDao delete tblItems.getValue.asInstanceOf[JInteger]
            reloadTableItems
          }
        }
      }
    }

    val lytContent = new VerticalLayout
    lytContent setMargin true
    lytContent setSpacing true
    
    addComponents(lytContent,
      new Label("Users from a specific IP number or an intervall of numbers are given direct access to the system (so that the user does not have to log in)."),
      new IPAccessView)
  }


  def roles = {
    def roleMapper = Imcms.getServices.getImcmsAuthenticatorAndUserAndRoleMapper

    class RoleDataWindow(caption: String) extends OkCancelDialog(caption) {
      val txtName = new TextField("Name")
      val chkPermGetPasswordByEmail = new CheckBox("Permission to get password by email")
      val chkPermAccessMyPages = new CheckBox("""Permission to access "My pages" """)
      val chkPermUseImagesFromArchive = new CheckBox("Permission to use images from image archive")
      val chkPermChangeImagesInArchive = new CheckBox("Permission to change images in image archive")

      val lytForm = new FormLayout {
        addComponents(this, txtName, chkPermGetPasswordByEmail, chkPermAccessMyPages, chkPermUseImagesFromArchive,
          chkPermChangeImagesInArchive)
      }

      val permsToChkBoxes = Map(
        RoleDomainObject.CHANGE_IMAGES_IN_ARCHIVE_PERMISSION -> chkPermChangeImagesInArchive,
        RoleDomainObject.USE_IMAGES_IN_ARCHIVE_PERMISSION -> chkPermUseImagesFromArchive,
        RoleDomainObject.PASSWORD_MAIL_PERMISSION -> chkPermGetPasswordByEmail,
        RoleDomainObject.ADMIN_PAGES_PERMISSION -> chkPermAccessMyPages 
      )
      
      def checkedPermissions =
        for ((permission, chkBox) <- permsToChkBoxes if chkBox.getValue.asInstanceOf[Boolean]) yield permission

      def checkPermissions(permissions: Set[RolePermissionDomainObject]) =
        permissions foreach { p => permsToChkBoxes(p).setValue(true) }

      setMainContent(lytForm)
    }

    class RolesView extends TableViewTemplate {
      lazy val btnAdd = new Button("Add")
      lazy val btnEdit = new Button("Edit")
      lazy val btnDelete = new Button("Delete")

      addComponents(pnlHeader, btnAdd, btnEdit, btnDelete)

      override def tableProperties = List(
        ("Id", classOf[JInteger],  null),
        ("Name", classOf[String],  null))

      override def tableItems() =
        roleMapper.getAllRoles.toList map { role =>
          role.getId -> List(Int box role.getId.intValue, role.getName)
        }

      btnAdd addListener {
        initAndShow(new RoleDataWindow("New role")) { w =>
          w.addOkButtonClickListener {
            val role = new RoleDomainObject(w.txtName.getValue.asInstanceOf[String])
            w.checkedPermissions foreach { p => role.addPermission(p) }

            roleMapper saveRole role

            reloadTableItems
          }
        }
      }

      btnEdit addListener {
        initAndShow(new RoleDataWindow("Edit role")) { w =>
          val roleId = tblItems.getValue.asInstanceOf[RoleId]
          val role = roleMapper.getRole(roleId)

          w.txtName setValue role.getName
          w checkPermissions role.getPermissions.toSet

          w.addOkButtonClickListener {
            role.removeAllPermissions
            w.checkedPermissions foreach { p => role.addPermission(p) }
            roleMapper saveRole role
            reloadTableItems
          }
        }        
      }

      btnDelete addListener {
        initAndShow(new ConfirmationDialog("Confirmation", "Delete role?")) { w =>
          val roleId = tblItems.getValue.asInstanceOf[RoleId]
          val role = roleMapper.getRole(roleId)
          
          w.addOkButtonClickListener {
            roleMapper deleteRole role
            reloadTableItems
          }
        }
      }

      override def resetComponents =
        if (tblItems.getValue == null) {
          btnDelete setEnabled false
          btnEdit setEnabled false
        } else {
          btnEdit setEnabled true
          btnDelete setEnabled true
        }
    }

    val lytContent = new VerticalLayout
    lytContent setMargin true
    lytContent setSpacing true

    addComponents(lytContent,
      new Label("Roles and their permissions."),
      new RolesView)    
  }

  //
  //
  //
  def users = {
    def roleMapper = Imcms.getServices.getImcmsAuthenticatorAndUserAndRoleMapper

    class UsersView extends TableViewTemplate {
      override def tableProperties = List(
        ("Id", classOf[JInteger],  null),
        ("Login name", classOf[String],  null),
        ("Password", classOf[String],  null),
        ("Default user?", classOf[JBoolean],  null),
        ("Superadmin?", classOf[JBoolean],  null),
        ("Useradmin?", classOf[JBoolean],  null))

      override def tableItems() =
        roleMapper.getAllUsers.toList map { user =>
          val userId = Int box user.getId
          
          userId -> List(userId,
                         user.getLoginName,
                         user.getPassword,
                         Boolean box user.isDefaultUser,
                         Boolean box user.isSuperAdmin,
                         Boolean box user.isUserAdmin)          
        }

      val frmFilter = new Form {
        setCaption("Filter")
        val layout = new VerticalLayout
        setLayout(layout)        

        val txtFilter = new TextField("Login, first name, last name, title, email, company")
        val sltRoles = new ListSelect("Role(s)")
        val chkInactive = new CheckBox("Include inactive users")
        val btnClear = new Button("Clear")
        val lytFooter = new GridLayout(2, 1)

        setFooter(lytFooter)

        lytFooter addComponent chkInactive
        lytFooter addComponent btnClear

        lytFooter.setComponentAlignment(chkInactive, Alignment.MIDDLE_LEFT)
        lytFooter.setComponentAlignment(btnClear, Alignment.MIDDLE_RIGHT)

        layout addComponent txtFilter
        layout addComponent sltRoles
      }

      //val 

      //pnlHeader setContent lytFilter
      pnlFooter setContent new VerticalLayout { addComponent(frmFilter) }
    }

    val lytContent = new VerticalLayout
    lytContent setMargin true
    lytContent setSpacing true

    addComponents(lytContent,
      new Label("Users and their permissions."),
      new UsersView)    
  }

  def settingsProperties = {
    val pnlStartPage = new Panel("Start page") {
      val txtNumber = new TextField("Number")

      addComponent(txtNumber)
    }

    val pnlSystemMessage = new Panel("System message") {
      val txtMessage = new TextField("Text")

      txtMessage.setRows(5)

      addComponent(txtMessage)
    }

    val pnlServerMaster = new Panel("Server master") {
      val txtName = new TextField("Name")
      val txtEmail = new TextField("Email")

      addComponents(this, txtName, txtEmail)
    }

    val pnlWebMaster = new Panel("Web master") {
      val txtName = new TextField("Name")
      val txtEmail = new TextField("Email")

      addComponents(this, txtName, txtEmail)
    }

    val lytButtons = new HorizontalLayout {
      val btnRevert = new Button("Revert")
      val btnSave = new Button("Save")

      setSpacing(true)

      addComponents(this, btnRevert, btnSave)
    }
    
    val lytContent = new VerticalLayout {
      setSpacing(true)
      setMargin(true)
    }

    addComponents(lytContent, pnlStartPage, pnlSystemMessage, pnlServerMaster, pnlWebMaster, lytButtons)

    def reload() {
      let(Imcms.getServices.getSystemData) { d =>
        pnlStartPage.txtNumber setValue d.getStartDocument.toString
        pnlSystemMessage.txtMessage setValue d.getSystemMessage
        pnlWebMaster.txtName setValue d.getWebMaster
        pnlWebMaster.txtEmail setValue d.getWebMasterAddress
        pnlServerMaster.txtName setValue d.getServerMaster
        pnlServerMaster.txtEmail setValue d.getServerMasterAddress        
      }
    }

    lytButtons.btnRevert addListener {
      reload() 
    }

    lytButtons.btnSave addListener {
      let(new SystemData) { d =>
        d setStartDocument pnlStartPage.txtNumber.getValue.asInstanceOf[String].toInt
        d setSystemMessage pnlSystemMessage.txtMessage.getValue.asInstanceOf[String]
        d setServerMaster pnlServerMaster.txtName.getValue.asInstanceOf[String]
        d setServerMasterAddress pnlServerMaster.txtEmail.getValue.asInstanceOf[String]
        d setWebMaster pnlWebMaster.txtName.getValue.asInstanceOf[String]
        d setWebMasterAddress pnlWebMaster.txtEmail.getValue.asInstanceOf[String]

        Imcms.getServices.setSystemData(d)
      }
    }

    reload()

    lytContent
  }

  def settingSessionCounter = new TabView {
    addTab(new ViewVerticalLayout("Session counter") { self =>
      setSpacing(false)
      
      val lytData = new FormLayout {
        val txtValue = new TextField("Value:")
        val calStart = new DateField("Start date:")
        calStart.setResolution(DateField.RESOLUTION_DAY)

        txtValue.setReadOnly(true)
        calStart.setReadOnly(true)

        addComponents(this, txtValue, calStart)
      }

      val lytButtons = new HorizontalLayout {
        val btnReload = new Button("Reload")
        val btnClear = new Button ("Clear")
        val btnEdit = new Button("Edit")
        setSpacing(true)

        addComponents(this, btnEdit, btnClear, btnReload)
      }

      addComponents(this, lytData, lytButtons)

      def reload() {
        // ?!?! when read only throws exception ?!?!
        lytData.txtValue setReadOnly false
        lytData.calStart setReadOnly false

        lytData.txtValue setValue Imcms.getServices.getSessionCounter.toString
        lytData.calStart setValue Imcms.getServices.getSessionCounterDate

        lytData.txtValue setReadOnly true
        lytData.calStart setReadOnly true         
      }

      lytButtons.btnReload addListener reload()
      lytButtons.btnClear addListener {
        initAndShow(new ConfirmationDialog("Confirmation", "Clear counter statistics?")) { w =>
          w.addOkButtonClickListener {
            Imcms.getServices setSessionCounter 0
            Imcms.getServices setSessionCounterDate new Date

            reload()
          }          
        }
      }

      lytButtons.btnEdit addListener {
        initAndShow(new OkCancelDialog("Edit session counter")) { w =>
          val txtValue = new TextField("Value")
          val calStart = new DateField("Start date")

          calStart.setResolution(DateField.RESOLUTION_DAY)
          txtValue setValue Imcms.getServices.getSessionCounter.toString
          calStart setValue Imcms.getServices.getSessionCounterDate

          w.setMainContent(new FormLayout {
            addComponents(this, txtValue, calStart)
          })

          w.addOkButtonClickListener {
            Imcms.getServices setSessionCounter txtValue.getValue.asInstanceOf[String].toInt
            Imcms.getServices setSessionCounterDate calStart.getValue.asInstanceOf[Date]

            reload()
          }

          reload() // enshure form and dialog values are same
        }
      }

      reload()
    })
  }

  def categories = {
    new VerticalLayout {
      setMargin(true)      
      addComponent(new TabSheet {
        addTab(new TableViewTemplate {
          setCaption("Category")

          override def tableProperties =
            ("Id", classOf[JInteger],  null) ::
            ("Name", classOf[String],  null) ::
            ("Description", classOf[String],  null) ::
            ("Icon", classOf[String],  null) ::
            ("Type", classOf[String],  null) ::
            Nil
        })
        
        addTab(new TableViewTemplate {
          setCaption("Category type")
          override def tableProperties = List(
            ("Id", classOf[JInteger],  null),
            ("Name", classOf[String],  null),
            ("Multi select?", classOf[JBoolean],  null),
            ("Inherited to new documents?", classOf[JBoolean],  null),
            ("Used by image archive?", classOf[JBoolean],  null))

//            ("Id", classOf[JInteger],  null) ::
//            ("Name", classOf[String],  null) ::
//            ("Multi select?", classOf[JBoolean],  null) ::
//            ("Inherited to new documents?", classOf[JBoolean],  null) ::
//            ("Used by image archive?", classOf[JBoolean],  null) ::
//            Nil
//
//          override def tableProperties =
//            ("Id", classOf[JInteger],  null) ::
//            ("Name", classOf[String],  null) ::
//            ("Multi select?", classOf[JBoolean],  null) ::
//            ("Inherited to new documents?", classOf[JBoolean],  null) ::
//            ("Used by image archive?", classOf[JBoolean],  null) ::
//            Nil
        })
      })
    }
  }

  lazy val filesystem = new ViewVerticalLayout {
    addComponent(new Panel("File manager"))
  }
  
  lazy val searchTerms = new TabView {
    addTab(new ViewVerticalLayout("Popular search terms") {
      val tblTerms = new Table {
        addContainerProperties(this, ("Term", classOf[String], null), ("Count", classOf[String], null))
        setPageLength(10)
      }

      val lytBar = new HorizontalLayout {
        setSpacing(true)
        setCaption("Date range")
        val calFrom = new DateField()
        val calTo = new DateField()
        val btnReload = new Button("Reload")

        calFrom.setValue(new Date)
        //calFrom.setStyle("calendar")
        calFrom.setResolution(DateField.RESOLUTION_DAY)

        calTo.setValue(new Date)
        //calTo.setStyle("calendar")
        calTo.setResolution(DateField.RESOLUTION_DAY)

        addComponents(this, calFrom, calTo, btnReload)
      }

      addComponents(this, tblTerms, lytBar)

      def reload() {
        val terms = AdminSearchTerms.getTermCounts(lytBar.calFrom.getValue.asInstanceOf[Date],
          lytBar.calTo.getValue.asInstanceOf[Date])

        tblTerms.removeAllItems
        terms foreach { t =>
          val item = Array[AnyRef](t.getTerm, t.getCount.toString)
          tblTerms.addItem(item, item)
        }
      }

      lytBar.btnReload addListener reload()

      reload()
    })
  }
}


