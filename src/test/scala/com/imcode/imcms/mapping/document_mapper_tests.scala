package com.imcode
package imcms.dao

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import imcode.server.user.UserDomainObject
import imcode.server.Imcms
import java.io.ByteArrayInputStream
import imcode.util.io.InputStreamSource
import org.apache.commons.io.FileUtils
import imcode.server.document.textdocument.{NoPermissionToAddDocumentToMenuException, MenuItemDomainObject, MenuDomainObject, TextDocumentDomainObject}
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite, BeforeAndAfterAll}
import imcms.test._
import fixtures.LanguagesFX
import imcms.test.Base.{project, db}
import imcode.server.document._
import java.util.EnumSet
import imcms.mapping.{DocumentSaver, DocumentStoringVisitor, DocumentMapper}
import imcms.api.{I18nMeta, ContentLoop, I18nSupport}

@RunWith(classOf[JUnitRunner])
class DocumentMapperSuite extends FunSuite with MustMatchers with BeforeAndAfterAll with BeforeAndAfterEach {

  var docMapper: DocumentMapper = _
  var admin: UserDomainObject = _
  var user: UserDomainObject = _
  var i18nSupport: I18nSupport = _

  override def beforeAll() = withLogFailure {
    db.recreate()
    project.initImcms(true, true)

    i18nSupport = Imcms.getI18nSupport
    docMapper = Imcms.getServices().getDocumentMapper
    admin = Imcms.getServices().verifyUser("admin", "admin")
    user = Imcms.getServices().verifyUser("user", "user")
  }

  override def afterAll() = Imcms.stop()


  test("save new empty text doc") {
    saveNewTextDocumentFn()
  }

  test("save new text doc with EMPTY save params") {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.TEXT_ID, parentDoc, admin)
      .asInstanceOf[TextDocumentDomainObject]

    val headlinePrefix = "headline_"
    val menuTextPrefix = "menu_text_"

    val i18nMetas = i18nSupport.getLanguages.map { language =>
      val i18nMeta = new I18nMeta

      i18nMeta.setLanguage(language)
      i18nMeta.setHeadline(headlinePrefix + language.getCode)
      i18nMeta.setMenuText(menuTextPrefix + language.getCode)

      language -> i18nMeta
    }.toMap.asJava

    val id = docMapper.saveNewDocument(
      newDoc,
      i18nMetas,
      EnumSet.noneOf(classOf[DocumentSaver.SaveParameter]),
      admin).getMeta.getId

    i18nSupport.getLanguages.map { language =>
      val doc = docMapper.getDefaultDocument(id, language).asInstanceOf[TextDocumentDomainObject]

      assertEquals(headlinePrefix + language.getCode, doc.getHeadline)
      assertEquals(menuTextPrefix + language.getCode, doc.getMenuText)

      expect(0, "texts in a doc") {
        doc.getTexts.size
      }
    }
  }


  test("save new text doc with [CopyI18nMetaTextsIntoTextFields] save params") {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.TEXT_ID, parentDoc, admin)
      .asInstanceOf[TextDocumentDomainObject]

    val headlinePrefix = "headline_"
    val menuTextPrefix = "menu_text_"

    val i18nMetas = i18nSupport.getLanguages.map { language =>
      val i18nMeta = new I18nMeta

      i18nMeta.setLanguage(language)
      i18nMeta.setHeadline(headlinePrefix + language.getCode)
      i18nMeta.setMenuText(menuTextPrefix + language.getCode)

      language -> i18nMeta
    }.toMap.asJava

    val id = docMapper.saveNewDocument(
      newDoc,
      i18nMetas,
      EnumSet.of(DocumentSaver.SaveParameter.CopyI18nMetaTextsIntoTextFields),
      admin).getMeta.getId

    i18nSupport.getLanguages.map { language =>
      val doc = docMapper.getDefaultDocument(id, language).asInstanceOf[TextDocumentDomainObject]

      assertEquals(headlinePrefix + language.getCode, doc.getHeadline)
      assertEquals(menuTextPrefix + language.getCode, doc.getMenuText)

      expect(2, "texts in a doc") {
        doc.getTexts.size
      }

      val text1 = doc.getText(1)
      val text2 = doc.getText(2)

      assertNotNull(text1)
      assertNotNull(text2)

      assertEquals(headlinePrefix + language.getCode, text1.getText)
      assertEquals(menuTextPrefix + language.getCode, text2.getText)
    }
  }


  test("save new url doc") {
    saveNewUrlDocumentFn()
  }


  test("save new html doc") {
    saveNewHtmlDocumentFn()
  }


  test("save new file doc") {
    saveNewFileDocumentFn()
  }


  def saveNewTextDocumentFn() = {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.TEXT_ID, parentDoc, admin)
      .asInstanceOf[TextDocumentDomainObject]

    docMapper.saveNewDocument(newDoc, admin)
  }


  def saveNewUrlDocumentFn() = {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.URL_ID, parentDoc, admin)
      .asInstanceOf[UrlDocumentDomainObject]

    docMapper.saveNewDocument(newDoc, admin)
  }


  def saveNewHtmlDocumentFn() = {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.HTML_ID, parentDoc, admin)
      .asInstanceOf[HtmlDocumentDomainObject]

    docMapper.saveNewDocument(newDoc, admin)
  }

  /**
   * Saves new file document file containing 3 files.
   *
   * @return
   * @throws Exception
   */
  def saveNewFileDocumentFn() = {
    class Source(data: String) extends InputStreamSource {
      val bin = new ByteArrayInputStream(data.getBytes())

      def getInputStream() = bin

      def getSize() = bin.available()
    }

    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val newDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.FILE_ID, parentDoc, admin)
      .asInstanceOf[FileDocumentDomainObject]


    for (i <- 0 to 2) {
        val fdf = new FileDocumentDomainObject.FileDocumentFile

        fdf.setFilename("test_file_%d.txt" format i)
        fdf.setMimeType("text")
        fdf.setCreatedAsImage(false)
        fdf.setInputStreamSource(new Source("test content " + i))

        newDoc.addFile("file_id_" + i, fdf)
    }

    assertSavedFiles(docMapper.saveNewDocument(newDoc, admin))
  }


  def assertSavedFiles(doc: FileDocumentDomainObject) = {
    val defaultFileId = doc.getDefaultFileId
    val defaultFile = doc.getDefaultFile
    val docFiles = doc.getFiles

    assertEquals(defaultFileId, "file_id_0")
    assertEquals(docFiles.size(), 3)

    for (i <- 0 to 2) {
      val fdfId = "file_id_" + i
      val fdf = docFiles.get(fdfId)

      assertNotNull(fdf)
      assertEquals(fdf.getFilename, "test_file_%d.txt" format i)
      assertEquals(fdf.getMimeType(), "text")

      val file = DocumentStoringVisitor.getFileForFileDocumentFile(doc.getId, doc.getVersionNo.intValue, fdfId)
      assertTrue(file.exists)

      val content = FileUtils.readFileToString(file)
      assertEquals(content, "test content " + i)
    }

    doc
  }


  test("update existing text document") {
    val doc = saveNewTextDocumentFn()

    docMapper.saveDocument(doc, admin)
  }


//  test("try save new document without required permissions") {
//    val doc = saveNewTextDocumentFn()
//
//    intercept[NoPermissionToEditDocumentException] {
//      docMapper.saveDocument(doc, user)
//    }
//  }


  test("update existing html document") {
    val doc = saveNewHtmlDocumentFn()

    docMapper.saveDocument(doc, admin)
  }

  test("update existing url document") {
    val doc = saveNewUrlDocumentFn()

    docMapper.saveDocument(doc, admin)
  }


  test("update existing file document") {
    val doc = saveNewFileDocumentFn()

    docMapper.saveDocument(doc, admin)
  }


  test("add menu to text doc") {
    val textDoc = saveNewTextDocumentFn()
    val menuDoc = saveNewTextDocumentFn()

    val menu = new MenuDomainObject
    val item = new MenuItemDomainObject(docMapper.getDocumentReference(menuDoc))
    menu.addMenuItem(item)


    textDoc.setMenu(0, menu)

    docMapper.saveDocument(textDoc, admin)

    val savedTextDoc = docMapper.getCustomDocument(textDoc.getId, textDoc.getVersionNo, textDoc.getLanguage)
      .asInstanceOf[TextDocumentDomainObject]

    val savedMenu = savedTextDoc.getMenus.get(0)

    assertNotNull(savedMenu)

    assertEquals(savedMenu.getMenuItems.length, 1)

    val savedMenuDoc = savedMenu.getMenuItems()(0).getDocument

    assertEquals(savedMenuDoc.getId, menuDoc.getId)

  }

//    @Test//(dependsOnMethods = {"createDocumentOfTypeFromParent"})
//    public void addMenu() throws Exception {
//        TextDocumentDomainObject parentDoc = (TextDocumentDomainObject)getMainWorkingDocumentInDefaultLanguage(true);
//        DocumentDomainObject menuItemDoc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.TEXT_ID, parentDoc, admin);
//        List<I18nMeta> labels = new LinkedList<I18nMeta>();
//
//        for (I18nLanguage lang: i18nSupport.getAllLanguages()) {
//            I18nMeta l = new I18nMeta();
//
//            l.setHeadline(":headline in:" + lang.getCode());
//            l.setMenuImageURL(":url in:" + lang.getCode());
//            l.setMenuText(":menuText in:" + lang.getCode());
//        }
//
//        Integer menuItemDocId =  docMapper.saveNewDocument(menuItemDoc, labels, admin, true);
//        DocumentReference docRef = docMapper.getDocumentReference(menuItemDoc);
//
//        MenuDomainObject menu = Factory.createNextMenu(parentDoc, docRef);
//        Integer menuNo = menu.getNo();
//
//        docMapper.saveTextDocMenu(parentDoc, menu, admin);
//
//        parentDoc = (TextDocumentDomainObject)getMainWorkingDocumentInDefaultLanguage(true);
//
//
//        DocGetterCallback docRequestInfo = new DocGetterCallback.WorkingDocRequestHandler(admin);
//        docRequestInfo.setLanguage(i18nSupport.getDefaultLanguage());
//        Imcms.setGetDocumentCallback(docRequestInfo);
//
//        menu = parentDoc.getMenu(menuNo);
//
//        assertNotNull(menu);
//        MenuItemDomainObject[] menuItems = menu.getMenuItems();
//
//        assertEquals(1, menuItems.length);
//        assertEquals(menuItemDocId.intValue(), menuItems[0].getDocumentReference().getDocumentId());
//    }


//  test("try copy existing text doc without required permissions") {
//    val doc = saveNewTextDocumentFn()
//
//    intercept[NoPermissionToAddDocumentToMenuException] {
//      val docCopy = docMapper.copyDocument(doc, user)
//    }
//  }


  test("copy text doc") {
    //TextDocumentDomainObject doc = saveNewTextDocumentFn();
    for (l <- Imcms.getI18nSupport.getLanguages) {
      val doc = docMapper.getDocument(1001).asInstanceOf[TextDocumentDomainObject]
      assertNotNull(doc)
    }

    val doc = docMapper.getDocument(1001).asInstanceOf[TextDocumentDomainObject]

    val docCopy = docMapper.copyDocument(doc, admin)
    val docCopyId = docCopy.getId

    assertNotSame(doc.getId, docCopyId)

    for (l <- Imcms.getI18nSupport.getLanguages.toList) {
      val d = docMapper.getDocument(docCopyId)
      assertNotNull(doc)
    }
  }



  test("copy HTML doc") {
    val doc = saveNewHtmlDocumentFn()
    val docCopy = docMapper.copyDocument(doc, admin)
  }


  test("copy URL doc") {
    val doc = saveNewUrlDocumentFn()
    val docCopy = docMapper.copyDocument(doc, admin)
  }


  test("copy File doc")  {
    val doc = saveNewFileDocumentFn()
    val docCopy = docMapper.copyDocument(doc, admin)
  }


//    @Test(enabled = true, dataProvider = "contentInfo")
//    public void insertTextDocumentText(Integer contentLoopNo, Integer contentIndex) throws Exception {
//        TextDocumentDomainObject doc = (TextDocumentDomainObject)getMainWorkingDocumentInDefaultLanguage(true);
//        TextDomainObject text = Factory.createNextText(doc);
//
//        text.setContentLoopNo(contentLoopNo);
//        text.setContentNo(contentIndex);
//
//        if (contentLoopNo != null) {
//            ContentLoop loop = doc.getContentLoop(contentLoopNo);
//
//            if (loop == null) {
//                loop = Factory.createContentLoop(doc.getId(), doc.getVersion().getNo(), contentLoopNo);
//                Content content = loop.addFirstContent();
//
//                text.setContentNo(content.getNo());
//                doc.setContentLoop(contentLoopNo, loop);
//            }
//
//            doc.setText(text.getNo(), text);
//        }
//
//        docMapper.saveTextDocText(doc, text, admin);
//    }


  test("change doc default version") {
    val parentDoc = getMainWorkingDocumentInDefaultLanguage(true)
    var doc = docMapper.createDocumentOfTypeFromParent(DocumentTypeDomainObject.TEXT_ID, parentDoc, admin)

    val docId = docMapper.saveNewDocument(doc, admin).getId
    val vi = docMapper.getDocumentVersionInfo(docId)

    doc = docMapper.getDefaultDocument(docId, i18nSupport.getDefaultLanguage)

    assertNotNull("New document exists",  doc)
    assertEquals("Default version of a new document is 0.", doc.getVersion.getNo, new JInteger(0))

    val version = docMapper.makeDocumentVersion(docId, admin)

    assertEquals("New doc version no is 1.", version.getNo, new JInteger(1))

    docMapper.changeDocumentDefaultVersion(docId, 1, admin)

    doc = docMapper.getDefaultDocument(docId, i18nSupport.getDefaultLanguage)

    assertEquals("Default version of a document is 1.", doc.getVersion.getNo, new JInteger(1))
  }


//    @Test(enabled = true, dataProvider = "contentInfo")
//    public void insertTextDocumentImage(Integer contentLoopNo, Integer contentIndex) throws Exception {
//        TextDocumentDomainObject doc = (TextDocumentDomainObject)getMainWorkingDocumentInDefaultLanguage(true);
//        ImageDomainObject image = Factory.createNextImage(doc);
//
//        image.setSource(new NullImageSource());
//        image.setContentLoopNo(contentLoopNo);
//        image.setContentNo(contentIndex);
//
//        docMapper.saveTextDocImage(doc, image, admin);
//    }


  test("get all documents") {
    val ids = docMapper.getAllDocumentIds()
    val docs = docMapper.getDocuments(ids)

    assertEquals(ids.size(), docs.size)
  }


  test("get working version of a doc") {
    getMainWorkingDocumentInDefaultLanguage(true)
  }

  test("get File doc") (pending)

  test("get HTML doc") (pending)

  test("get URL doc") (pending)

  test("make text doc version") {
    val workingVersionDoc = getMainWorkingDocumentInDefaultLanguage(true)
    val info = docMapper.getDocumentVersionInfo(workingVersionDoc.getId)

    docMapper.makeDocumentVersion(workingVersionDoc.getId, admin)

    val newInfo = docMapper.getDocumentVersionInfo(workingVersionDoc.getId)
    val expectedNewVersionNo = info.getLatestVersion.getNo.intValue + 1

    assertEquals(info.getVersionsCount + 1, newInfo.getVersionsCount)
    assertEquals(newInfo.getLatestVersion.getNo, expectedNewVersionNo)

    val newVersionDoc = docMapper.getCustomDocument(workingVersionDoc.getId, expectedNewVersionNo)
    // instance of TextDocumentDomainObject ???

    assertNotNull(newVersionDoc)
  }


  /**
   * Saves document's content (all expect meta).
   */
  test("save doc content") (pending)


  test("make HTML doc version") {
    val doc = saveNewHtmlDocumentFn();

    val info = docMapper.getDocumentVersionInfo(doc.getId)

    docMapper.makeDocumentVersion(doc.getId(), admin)

    val newInfo = docMapper.getDocumentVersionInfo(doc.getId)
    val expectedNewVersionNo = info.getLatestVersion.getNo.intValue + 1

    assertEquals(info.getVersionsCount + 1, newInfo.getVersionsCount)
    assertEquals(newInfo.getLatestVersion.getNo, expectedNewVersionNo)

    val newVersionDoc = docMapper.getCustomDocument(doc.getId, expectedNewVersionNo)
    // instance of HtmlDocumentDomainObject

    assertNotNull(newVersionDoc)
  }


  test("make URL doc version") {
    val doc = saveNewUrlDocumentFn();
    val info = docMapper.getDocumentVersionInfo(doc.getId)

    docMapper.makeDocumentVersion(doc.getId, admin)

    val newInfo = docMapper.getDocumentVersionInfo(doc.getId)
    val expectedNewVersionNo = info.getLatestVersion.getNo.intValue + 1

    assertEquals(info.getVersionsCount + 1, newInfo.getVersionsCount)
    assertEquals(newInfo.getLatestVersion.getNo, expectedNewVersionNo)

    val newVersionDoc = docMapper.getCustomDocument(doc.getId, expectedNewVersionNo)
    // instanceOf UrlDocumentDomainObject

    assertNotNull(newVersionDoc);
  }


  test("make file doc version") {
    val doc = saveNewFileDocumentFn()
    val info = docMapper.getDocumentVersionInfo(doc.getId)
    val docVersionNew = docMapper.makeDocumentVersion(doc.getId, admin)
    val infoNew = docMapper.getDocumentVersionInfo(doc.getId)
    val expectedNewVersionNo = info.getLatestVersion.getNo.intValue + 1

    assertEquals(info.getVersionsCount + 1, infoNew.getVersionsCount)
    assertEquals(infoNew.getLatestVersion.getNo, expectedNewVersionNo)

    val docNew = docMapper.getCustomDocument(doc.getId, expectedNewVersionNo)
    // instance of FileDocumentDomainObject
    assertNotNull(docNew)
    assertEquals(doc.getId, docNew.getId)

    assertSavedFiles(docNew.asInstanceOf[FileDocumentDomainObject])
  }


  test("get doc version info") (pending)

  test("save text doc text") (pending)

  test("save text doc image") (pending)

  test("save text doc content loop") {
    val doc = saveNewTextDocumentFn()
    val loop = new ContentLoop
    loop.addFirstContent

    doc.setContentLoop(0, loop)

    docMapper.saveDocument(doc, admin)
  }


  test("invalidate doc") {
    val doc = saveNewTextDocumentFn()

    docMapper.invalidateDocument(doc)
  }

  test("get working version of a doc in default language") {
    val doc = getMainWorkingDocumentInDefaultLanguage(true)
  }


  test("get default document") {
    val doc = docMapper.getDefaultDocument(1001)

    assertNotNull(doc)
  }


  test("get custom doc") (pending)


  test("delete text doc") {
    val doc = saveNewTextDocumentFn()
    docMapper.deleteDocument(doc, admin)
  }


  test("delete HTML doc") {
    val doc = saveNewHtmlDocumentFn()
    docMapper.deleteDocument(doc, admin)
  }


  test("delete URL doc") {
    val doc = saveNewUrlDocumentFn()
    docMapper.deleteDocument(doc, admin)
  }

  test("delete File doc") {
    val doc = saveNewFileDocumentFn()

    for (i <- 0 to 2) {
      val fdfId = "file_id_" + i
      val file = DocumentStoringVisitor.getFileForFileDocumentFile(doc.getId, doc.getVersionNo.intValue, fdfId)

      assertTrue(file.exists)
    }


    docMapper.deleteDocument(doc, admin)

    for (i <- 0 to 2) {
      val fdfId = "file_id_" + i
      val file = DocumentStoringVisitor.getFileForFileDocumentFile(doc.getId, doc.getVersionNo.intValue, fdfId)

      assertTrue(!file.exists)
    }
  }

  test("update doc permissions") (pending)


  def getMainWorkingDocumentInDefaultLanguage(assertDocExists: Boolean) = {
    val doc = docMapper.getCustomDocument(1001, 0, i18nSupport.getDefaultLanguage)

    if (assertDocExists) {
      assertNotNull(doc)
    }

    doc.asInstanceOf[TextDocumentDomainObject]
  }


//    /**
//     * Return content loop no and content index:
//     */
//    @DataProvider
//    public Object[][] contentInfo() {
//        TextDocumentDomainObject doc = getMainWorkingDocumentInDefaultLanguage(true);
//        ContentLoop existingContentLoop = doc.getContentLoops().values().iterator().next();
//        ContentLoop unsavedContentLoop = Factory.createNextContentLoop(doc);
//
//        unsavedContentLoop.addFirstContent();
//
//        Integer noContentLoopNo = null;
//        Integer noContentNo = null;
//
//        Integer existingContentLoopNo = existingContentLoop.getNo();
//        Integer existingContentNo = existingContentLoop.getContents().get(0).getNo();
//
//        Integer unsavedContentLoopNo = unsavedContentLoop.getNo();
//        Integer unsavedContentNo = unsavedContentLoop.getContents().get(0).getNo();
//
//        return new Object [][] {
//                {noContentLoopNo, noContentNo},
//                {existingContentLoopNo, existingContentNo},
//                {unsavedContentLoopNo, unsavedContentNo}
//        };
//    }
}