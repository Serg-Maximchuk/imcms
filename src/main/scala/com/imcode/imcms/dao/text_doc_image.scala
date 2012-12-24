package com.imcode
package imcms.dao

import scala.collection.JavaConverters._

import org.springframework.transaction.annotation.Transactional
import imcode.server.document.textdocument._
import com.imcode.imcms.api.{DocRef, I18nLanguage, ImageHistory}


object ImageUtil {

  /** Inits text docs images sources. */
  def initImagesSources(images: JList[ImageDomainObject]) = images |>> { _.asScala.foreach(initImageSource) }

  /** Inits text doc's image source. */
  def initImageSource(image: ImageDomainObject) = image |>> { _ =>
    for (url <- Option(image).map(_.getImageUrl).map(_.trim)) {
      image.setSource(
        image.getType.intValue match {
          case ImageSource.IMAGE_TYPE_ID__FILE_DOCUMENT =>
            // This type is used in file docs ONLY
            sys.error("Illegal image source type - IMAGE_TYPE_ID__FILE_DOCUMENT. Id: %s, no: %s. url: %s.".format(image.getId, image.getNo, url))

          case ImageSource.IMAGE_TYPE_ID__IMAGES_PATH_RELATIVE_PATH => new ImagesPathRelativePathImageSource(url)
          case ImageSource.IMAGE_TYPE_ID__IMAGE_ARCHIVE => new ImageArchiveImageSource(url)
          case _ => new NullImageSource
        }
      )
    }
  }
}


@Transactional(rollbackFor = Array(classOf[Throwable]))
class ImageDao extends HibernateSupport {

  @scala.reflect.BeanProperty
  var languageDao: LanguageDao = _

  /**
   * Please note that createIfNotExists merely creates an instance of ImageDomainObject not a database entry.
   */
  def getImages(docRef: DocRef, no: Int, contentRefOpt: Option[ContentRef],
                createIfNotExists: Boolean): JList[ImageDomainObject] = {
    for {
      language <- languageDao.getAllLanguages.asScala
      image <- PartialFunction.condOpt(getImage(docRef, no, language, contentRefOpt)) {
        case image if image != null => image
        case _ if createIfNotExists => new ImageDomainObject |>> { img =>
          img.setDocRef(docRef)
          img.setNo(no)
          img.setLanguage(language)
          img.setContentRef(contentRefOpt.orNull)
        }
      }
    } yield image
  } |> { _.asJava }



  def getImage(docRef: DocRef, no: Int, language: I18nLanguage, contentRefOpt: Option[ContentRef]) = {
    val queryStr =
      if (contentRefOpt.isDefined)
        """select i from Image i where i.docRef = :docRef and i.no = :no
           and i.language = :language AND i.contentRef = :contentRef"""
      else
        """select i from Image i where i.docRef = :docRef and i.no = :no
           and i.language = :language AND i.contentRef IS NULL"""

    hibernate.withCurrentSession { session =>
      session.createQuery(queryStr) |> { query =>
        query.setParameter("docRef", docRef)
          .setParameter("no", no)
          .setParameter("language", language)

        if (contentRefOpt.isDefined) {
          query.setParameter("contentRef", contentRefOpt.get)
        }

        ImageUtil.initImageSource(query.uniqueResult.asInstanceOf[ImageDomainObject])
      }
    }
  }


  def saveImage(image: ImageDomainObject) = hibernate.saveOrUpdate(image)


  def saveImageHistory(imageHistory: ImageHistory) = hibernate.save(imageHistory)


  def getImages(docRef: DocRef): JList[ImageDomainObject] =
    hibernate.listByNamedQueryAndNamedParams[ImageDomainObject](
      "Image.getByDocRef", "docRef" -> docRef
    ) |> ImageUtil.initImagesSources


  def getImages(docRef: DocRef, language: I18nLanguage): JList[ImageDomainObject] =
     hibernate.listByNamedQueryAndNamedParams[ImageDomainObject](
       "Image.getByDocRefAndLanguage",
       "docRef" -> docRef, "language" -> language
     ) |> ImageUtil.initImagesSources



  def deleteImages(docRef: DocRef, language: I18nLanguage): Int =
    hibernate.bulkUpdateByNamedQueryAndNamedParams(
      "Image.deleteImagesByDocRefAndLanguage", "docRef" -> docRef, "language" -> language
    )
}