package imcode.server.document.index.solr

import _root_.com.imcode._
import _root_.imcode.server.user.UserDomainObject
import _root_.imcode.server.document.DocumentDomainObject
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import org.apache.solr.common.params.SolrParams

/**
 * Delegates all invocations to the ManagedSolrDocumentIndexService instance.
 * In case of an indexing error replaces managed instance with new one and re-indexes documents.
 */
class EmbeddedSolrDocumentIndexService(solrHome: String, serviceOps: SolrDocumentIndexServiceOps)
    extends SolrDocumentIndexService {

  private val lock = new AnyRef
  private val shutdownRef: AtomicBoolean = new AtomicBoolean(false)
  private val serviceRef: AtomicReference[SolrDocumentIndexService] = new AtomicReference(NoOpSolrDocumentIndexService)
  private val serviceErrorHandler: ManagedSolrDocumentIndexService.ServiceError => Unit = {
    case indexError if indexError.error.isInstanceOf[SolrInputDocumentCreateException] =>
      // ignore
    case indexError =>
      // replace service
      indexError.service |> { service => lock.synchronized {
        if (serviceRef.compareAndSet(service, NoOpSolrDocumentIndexService)) {
          logger.info("Index error has occuerd. Managed service instance have to be replaced.", indexError.error)
          service.shutdown()

          if (shutdownRef.get()) {
            logger.info("New managed service instance will not be created - service has been shout down.")
          } else {
            logger.info("Creating new instance of managed service. Data directory will be recreated.")
            newManagedService(recreateDataDir = true) |> { newService =>
              serviceRef.set(newService)
              newService.requestIndexRebuild()
            }

            logger.info("New managed service instance has been created.")
          }
        }
      }
    }
  }

  newManagedService(recreateDataDir = false) |> serviceRef.set


  def search(solrParams: SolrParams, searchingUser: UserDomainObject): JList[DocumentDomainObject] =
    serviceRef.get().search(solrParams, searchingUser)


  def requestIndexUpdate(request: SolrDocumentIndexService.IndexUpdateRequest) {
    serviceRef.get().requestIndexUpdate(request)
  }


  def requestIndexRebuild(): Option[SolrDocumentIndexService.IndexRebuildTask] = serviceRef.get().requestIndexRebuild()


  def indexRebuildTask(): Option[SolrDocumentIndexService.IndexRebuildTask] = serviceRef.get().indexRebuildTask()


  def shutdown(): Unit = lock.synchronized {
    if (shutdownRef.compareAndSet(false, true)) {
      logger.info("Attempting to shtting down the service.")
      serviceRef.getAndSet(NoOpSolrDocumentIndexService).shutdown()
      logger.info("Service has been shut down.")
    }
  }


  private def newManagedService(recreateDataDir: Boolean): ManagedSolrDocumentIndexService = {
    val solrServer = SolrServerFactory.createEmbeddedSolrServer(solrHome, recreateDataDir)

    new ManagedSolrDocumentIndexService(solrServer, solrServer, serviceOps, serviceErrorHandler)
  }
}