package com.imcode
package imcms.dao

import org.springframework.transaction.annotation.Transactional
import com.imcode.imcms.api.{DocumentLanguage, SystemProperty, IPAccess}


@Transactional(rollbackFor = Array(classOf[Throwable]))
class SystemDao extends HibernateSupport {

  def getProperties(): JList[SystemProperty] = hibernate.listAll()

  def getProperty(name: String): SystemProperty = hibernate.getByQuery(
    "SELECT p FROM SystemProperty p WHERE p.name = ?1", 1 -> name)

  def saveProperty(property: SystemProperty) = hibernate.saveOrUpdate(property)
}


@Transactional(rollbackFor = Array(classOf[Throwable]))
class IPAccessDao extends HibernateSupport {

    def getAll(): JList[IPAccess] = hibernate.listAll()

    def delete(id: JInteger) = hibernate.bulkUpdateByNamedParams(
        "DELETE FROM IPAccess i WHERE i.id = :id", "id" -> id
    )

    def save(ipAccess: IPAccess ) = hibernate.saveOrUpdate(ipAccess)

    def get(id: JInteger) = hibernate.get[IPAccess](id)
}


@Transactional(rollbackFor = Array(classOf[Throwable]))
class LanguageDao extends HibernateSupport {


  def getAllLanguages(): JList[DocumentLanguage] = hibernate.listAll()


  def getById(id: JInteger): DocumentLanguage = hibernate.getByNamedQueryAndNamedParams("I18nLanguage.getById", "id" -> id)


  def getByCode(code: String): DocumentLanguage =
    hibernate.getByNamedQueryAndNamedParams("I18nLanguage.getByCode", "code" -> code)


  def saveLanguage(language: DocumentLanguage): DocumentLanguage = language.clone() |> hibernate.saveOrUpdate


  def deleteLanguage(id: JInteger) =
    hibernate.bulkUpdateByNamedParams("DELETE FROM I18nLanguage l WHERE l.id = :id", "id" -> id)
}