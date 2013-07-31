package com.imcode
package imcms.dao

import scala.collection.JavaConverters._

import org.springframework.transaction.annotation.Transactional
import java.{util => ju}
import com.imcode.imcms.dao.hibernate.HibernateSupport

/**
 * Native queries - moved from the DocumentMapper.
 * TODO: Rewrite native queries using HQL
 */
@Transactional(rollbackFor = Array(classOf[Throwable]))
class NativeQueriesDao extends HibernateSupport {

  def getAllMimeTypes(): JList[String] = hibernate.listBySqlQuery(
    "SELECT mime FROM mime_types WHERE mime_id > 0 ORDER BY mime_id"
  )


  def getAllMimeTypesWithDescriptions(languageIso639_2: String): JList[Array[String]] = hibernate.listBySqlQuery(
    "SELECT mime, mime_name FROM mime_types WHERE lang_prefix = ? AND mime_id > 0 ORDER BY mime_id", languageIso639_2
  )


  def getParentDocumentAndMenuIdsForDocument(documentId: JInteger): JList[Array[JInteger]] =
    hibernate.listBySqlQuery(
      """SELECT doc_id, no FROM imcms_text_doc_menu_items childs, imcms_text_doc_menus menus
         WHERE menus.id = childs.menu_id AND to_doc_id = ?""", documentId
    )


  def getDocumentsWithPermissionsForRole(roleId: JInteger): JList[JInteger] = hibernate.listBySqlQuery(
    "SELECT meta_id FROM roles_rights WHERE role_id = ? ORDER BY meta_id", roleId
  )


  def getAllDocumentTypeIdsAndNamesInUsersLanguage(languageIso639_2: String): JMap[JInteger, String] =
    hibernate.listBySqlQuery(
      "SELECT doc_type, type FROM doc_types WHERE lang_prefix = ? ORDER BY doc_type", languageIso639_2
    ) |> {
      rows =>
        new ju.TreeMap[JInteger, String] |>> { m =>
          for (Array(typeId: JInteger, name: String) <- rows.asScala) m.put(typeId, name)
        }
    }


  def getDocumentMenuPairsContainingDocument(documentId: JInteger): JList[Array[JInteger]] =
    hibernate.listBySqlQuery(
      """SELECT doc_id, no FROM imcms_text_doc_menus menus, imcms_text_doc_menu_items childs
         WHERE menus.id = childs.menu_id AND childs.to_doc_id = ? ORDER BY doc_id, no""",
      documentId
    )
}