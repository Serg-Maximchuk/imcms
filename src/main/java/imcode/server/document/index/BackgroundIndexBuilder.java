package imcode.server.document.index;

import imcode.util.ShouldNotBeThrownException;
import imcode.util.Utility;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.solr.client.solrj.SolrServer;

public class BackgroundIndexBuilder {
    private final static Logger log = Logger.getLogger(BackgroundIndexBuilder.class);

    private final RebuildingDirectoryIndex rebuildingDirectoryIndex;

    private SolrServer solrServer;
    private IndexBuildingThread indexBuildingThread;
    private SolrIndexDocumentFactory indexDocumentFactory;

    public BackgroundIndexBuilder(SolrServer solrServer, RebuildingDirectoryIndex rebuildingDirectoryIndex,
                                  SolrIndexDocumentFactory indexDocumentFactory) {
        this.solrServer = solrServer;
        this.rebuildingDirectoryIndex = rebuildingDirectoryIndex;
        this.indexDocumentFactory = indexDocumentFactory;
    }

    public synchronized void start() {
        try {
            NDC.push(ClassUtils.getShortClassName(getClass())+"-"+Utility.numberToAlphaNumerics(System.identityHashCode(this)));

            if ( null != indexBuildingThread && indexBuildingThread.isAlive() ) {
                log.debug("Ignoring request to build new index. Already in progress.");
                return;
            }

            try {
                log.debug("Starting index rebuild thread.") ;
                indexBuildingThread = new IndexBuildingThread(this, solrServer, indexDocumentFactory);
                indexBuildingThread.start();
            } catch ( IllegalThreadStateException itse ) {
                throw new ShouldNotBeThrownException(itse);
            }
        } finally {
            NDC.pop();
        }
    }

    public synchronized void addDocument(Integer docId) {
        if ( null != indexBuildingThread ) {
            indexBuildingThread.addDocument(docId);
        }
    }

    public synchronized void removeDocument(Integer docId) {
        if ( null != indexBuildingThread ) {
            indexBuildingThread.removeDocument(docId);
        }
    }

    public synchronized void notifyRebuildComplete(DirectoryIndex newIndex) {
        rebuildingDirectoryIndex.notifyRebuildComplete(newIndex) ;
    }
}
