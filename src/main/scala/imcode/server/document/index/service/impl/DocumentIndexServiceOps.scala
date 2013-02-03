package imcode.server.document.index.service.impl

import com.imcode._
import com.imcode.imcms.api.ContentLanguage
import com.imcode.imcms.mapping.DocumentMapper
import _root_.imcode.server.document.DocumentDomainObject
import _root_.imcode.server.document.index.DocumentIndex
import _root_.imcode.server.user.UserDomainObject
import _root_.imcode.server.document.index.service.{IndexRebuildProgress}
import scala.collection.SeqView
import scala.collection.JavaConverters._
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.util.DateUtil
import java.lang.{InterruptedException, Thread}
import org.apache.solr.client.solrj.{SolrQuery, SolrServer}
import java.util.Date
import org.apache.solr.client.solrj.response.QueryResponse
import java.net.URLDecoder

/**
 * Document index service low level operations.
 *
 * The instance of this class is thread save.
 */
class DocumentIndexServiceOps(documentMapper: DocumentMapper, documentIndexer: DocumentIndexer) extends Log4jLoggerSupport {
  type DocId = Int

  @throws(classOf[SolrInputDocumentCreateException])
  def withExceptionWrapper[A](body: => A): A = {
    try {
      body
    } catch {
      case e: Throwable => throw new SolrInputDocumentCreateException(e)
    }
  }

  @throws(classOf[SolrInputDocumentCreateException])
  def mkSolrInputDocs(docId: Int): Seq[SolrInputDocument] = withExceptionWrapper {
    mkSolrInputDocs(docId, documentMapper.getImcmsServices.getI18nContentSupport.getLanguages.asScala)
  }


  @throws(classOf[SolrInputDocumentCreateException])
  def mkSolrInputDocs(docId: Int, languages: Seq[ContentLanguage]): Seq[SolrInputDocument] = withExceptionWrapper {
    val solrInputDocs = for {
      language <- languages
      doc <- documentMapper.getDefaultDocument(docId, language).asOption
    } yield documentIndexer.index(doc)


    if (logger.isTraceEnabled) {
      logger.trace("created %d solrInputDoc(s) with docId: %d and language(s): %s.".format(
        solrInputDocs.length, docId, languages.mkString(", "))
      )
    }

    solrInputDocs
  }


  @throws(classOf[SolrInputDocumentCreateException])
  def mkSolrInputDocsView(): SeqView[(DocId, Seq[SolrInputDocument]), Seq[_]] = {
    documentMapper.getImcmsServices.getI18nContentSupport.getLanguages.asScala |> { languages =>
      documentMapper.getAllDocumentIds.asScala.view.map(docId => docId.toInt -> mkSolrInputDocs(docId, languages))
    }
  }


  def mkSolrDocsDeleteQuery(docId: Int): String = "%s:%d".format(DocumentIndex.FIELD__META_ID, docId)


  def search(solrServer: SolrServer, solrQuery: SolrQuery, searchingUser: UserDomainObject): Iterator[DocumentDomainObject] = {
    if (logger.isDebugEnabled) {
      logger.debug("Searching using solrQuery: %s, searchingUser: %s.".format(URLDecoder.decode(solrQuery.toString, "UTF-8"), searchingUser))
    }

    val solrDocs = solrServer.query(solrQuery).getResults

    for {
      solrDoc <- solrDocs.iterator.asScala
      //docId = solrDoc.getFieldValue(DocumentIndex.FIELD__META_ID).asInstanceOf[Int]
      //languageCode = solrDoc.getFieldValue(DocumentIndex.FIELD__LANGUAGE_CODE).asInstanceOf[String]
      docId = solrDoc.getFieldValue(DocumentIndex.FIELD__META_ID).toString.toInt
      languageCode = solrDoc.getFieldValue(DocumentIndex.FIELD__LANGUAGE_CODE).toString
      doc = documentMapper.getDefaultDocument(docId, languageCode)
      if doc != null && searchingUser.canSearchFor(doc)
    } yield doc
  }


  def query(solrServer: SolrServer, solrQuery: SolrQuery): QueryResponse = {
    if (logger.isDebugEnabled) {
      logger.debug("Searching using solrQuery: %s.".format(solrQuery))
    }

    solrServer.query(solrQuery)
  }


  @throws(classOf[SolrInputDocumentCreateException])
  def addDocsToIndex(solrServer: SolrServer, docId: Int) {
    mkSolrInputDocs(docId) |> { solrInputDocs =>
      if (solrInputDocs.nonEmpty) {
        solrServer.add(solrInputDocs.asJava)
        solrServer.commit()
        logger.trace("added %d solrInputDoc(s) with docId %d into the index.".format(solrInputDocs.length, docId))
      }
    }
  }


  def deleteDocsFromIndex(solrServer: SolrServer, docId: Int): Unit = mkSolrDocsDeleteQuery(docId) |> { deleteQuery =>
    solrServer.deleteByQuery(deleteQuery)
    solrServer.commit()
  }


  @throws(classOf[InterruptedException])
  @throws(classOf[SolrInputDocumentCreateException])
  def rebuildIndex(solrServer: SolrServer)(progressCallback: IndexRebuildProgress => Unit) {
    logger.trace("Rebuilding index.")
    val docsView = mkSolrInputDocsView()
    val docsCount = docsView.length
    val rebuildStartDt = new Date
    val rebuildStartMills = rebuildStartDt.getTime

    progressCallback(IndexRebuildProgress(rebuildStartMills, rebuildStartMills, docsCount, 0))

    for { ((docId, solrInputDocs), docNo) <- docsView.zip(Stream.from(1)); if solrInputDocs.nonEmpty } {
      if (Thread.currentThread().isInterrupted) {
        solrServer.rollback()
        throw new InterruptedException()
      }
      solrServer.add(solrInputDocs.asJava)
      if (logger.isTraceEnabled) {
        logger.trace("Added input docs [%s] to index.".format(solrInputDocs))
      }

      progressCallback(IndexRebuildProgress(rebuildStartMills, new Date().getTime, docsCount, docNo))
    }

    logger.trace("Deleting old documents from the index.")
    solrServer.deleteByQuery("timestamp:{* TO %s}".format(DateUtil.getThreadLocalDateFormat.format(rebuildStartDt)))
    solrServer.commit()
    logger.trace("Reinfexing is complete.")
  }
}