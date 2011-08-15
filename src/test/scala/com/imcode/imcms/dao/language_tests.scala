package com.imcode
package imcms.dao

import imcms.api.{SystemProperty, I18nLanguage}
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite, BeforeAndAfterAll}
import imcms.test.Base.{db}

@RunWith(classOf[JUnitRunner])
class LanguageDaoSuite extends FunSuite with MustMatchers with BeforeAndAfterAll with BeforeAndAfterEach {

  var systemDao: SystemDao = _
  var languageDao: LanguageDao = _

  override def beforeAll() = db.recreate()

  override def beforeEach() {
    val sf = db.createHibernateSessionFactory(Seq(classOf[SystemProperty], classOf[I18nLanguage]),
               "src/main/resources/com/imcode/imcms/hbm/I18nLanguage.hbm.xml")

    db.runScripts("src/test/resources/sql/language_dao.sql")

    systemDao = new SystemDao
    languageDao = new LanguageDao
    systemDao.setSessionFactory(sf)
    languageDao.setSessionFactory(sf)
  }

  test("get all [2] languages") {
    val languages = languageDao.getAllLanguages
    assertTrue("DB contains 2 languages.", languages.size == 2)
  }

  test("save new language") {
    val id = 3
    val code: String = "ee"

    assertNull("Language with id %d does not exists." format id, languageDao.getById(3))
    assertNull("Language with code %s does not exists." format code, languageDao.getByCode(code))

    var language = new I18nLanguage

    language.setCode(code)
    language.setName("Estonain")
    language.setNativeName("Eesti")
    language.setEnabled(true)
    languageDao.saveLanguage(language)
    assertNotNull("Language with id %d exists." format id, languageDao.getById(3))
    assertNotNull("Language with code %s exists." format code, languageDao.getByCode(code))
  }

  test("update existing language") {
    var language = languageDao.getById(1)
    assertTrue("Language is enabled.", language.isEnabled.booleanValue)
    language.setEnabled(false)
    languageDao.saveLanguage(language)
    language = languageDao.getById(1)
    assertFalse("Language is disabled.", language.isEnabled.booleanValue)
  }

  test("get defailt language") {
    val property = systemDao.getProperty("DefaultLanguageId")
    assertNotNull("DefaultLanguageId system property exists.", property)
    assertEquals("DefaultLanguageId system property is set to %d." format 1, new JInteger(1), property.getValueAsInteger)
    val language = languageDao.getById(property.getValueAsInteger)
    assertNotNull("Default language exists.", language)
  }

  test("get existing language by code") {
    for (code <- Array("en", "sv"); language = languageDao.getByCode(code)) {
      assertNotNull("Language with code %s is exists." format code, language)
      assertEquals("Language code is correct.", code, language.getCode)
    }
    let("xx") { undefinedCode =>
      assertNull("Language with code %s does not exists." format undefinedCode, languageDao.getByCode(undefinedCode))
    }
  }

  test("get existing language by id") {
    for (id <- Array(1, 2); language = languageDao.getById(id)) {
      assertNotNull("Language with id %d is exists." format id, language)
      assertEquals("Language id is correct.", id, language.getId)
    }
    assertNull("Language with id %d does not exists." format 3, languageDao.getById(3))
  }

  test("change default language") {
    val property = systemDao.getProperty("DefaultLanguageId")
    property.setValue("2")
    systemDao.saveProperty(property)
    val language = languageDao.getById(systemDao.getProperty("DefaultLanguageId").getValueAsInteger)
    assertEquals("Language id is correct.", language.getId, 2)
  }
}