package imcode.server.document.index;

import imcode.server.document.DocumentDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.DateConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AutorebuildingDirectoryIndex implements DocumentIndex {
    private int indexingSchedulePeriodInMilliseconds;

    private final static Logger log = Logger.getLogger(AutorebuildingDirectoryIndex.class.getName());

    private DirectoryIndex index;
    private Set documentsToAddToNewIndex = Collections.synchronizedSet(new HashSet());
    private Set documentsToRemoveFromNewIndex = Collections.synchronizedSet(new HashSet());
    private boolean buildingNewIndex;

    private final Thread indexBuildingThread = createIndexBuildingThread();

    static {
        // FIXME: Set to something lower, like imcmsDocumentCount to prevent slow queries?
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
    }

    public AutorebuildingDirectoryIndex(File indexDirectory, int indexingSchedulePeriodInMinutes) {
        this.indexingSchedulePeriodInMilliseconds = indexingSchedulePeriodInMinutes * DateUtils.MILLIS_IN_MINUTE ;
        this.index = new DirectoryIndex(indexDirectory);
        Timer scheduledIndexBuildingTimer = new Timer(true);
        long scheduledIndexDelay = 0 ;
        if ( IndexReader.indexExists(indexDirectory) ) {
            try {
                long indexModifiedTime = IndexReader.lastModified(indexDirectory);
                long time = System.currentTimeMillis();
                long nextTime = indexModifiedTime + indexingSchedulePeriodInMilliseconds;
                if (nextTime > time) {
                    log.info("First indexing scheduled at " + formatDatetime(new Date(nextTime)));
                    scheduledIndexDelay = nextTime - time;
                }
            } catch ( IOException e ) {
                log.warn("Failed to get last modified time of index.", e) ;
            }
        }
        scheduledIndexBuildingTimer.scheduleAtFixedRate(new ScheduledIndexingTimerTask(), scheduledIndexDelay, indexingSchedulePeriodInMilliseconds);
    }

    private String formatDatetime(Date nextExecutionTime) {
        return new SimpleDateFormat(DateConstants.DATETIME_FORMAT_STRING).format(nextExecutionTime);
    }

    public void indexDocument(DocumentDomainObject document) {
        if ( buildingNewIndex ) {
            documentsToAddToNewIndex.add(document);
        }
        try {
            index.indexDocument(document);
        } catch ( IndexException e ) {
            rebuildBecauseOfError("Failed to add document " + document.getId() + " to index.", e);
        }
    }

    public void removeDocument(DocumentDomainObject document) {
        if ( buildingNewIndex ) {
            documentsToRemoveFromNewIndex.add(document);
        }
        try {
            index.removeDocument(document);
        } catch ( IndexException e ) {
            rebuildBecauseOfError("Failed to remove document " + document.getId() + " from index.", e);
        }
    }

    public DocumentDomainObject[] search(Query query,
                                                      UserDomainObject searchingUser) throws IndexException {
        try {
            DocumentDomainObject[] documents = index.search(query, searchingUser);
            if (index.isInconsistent()) {
                rebuildBecauseOfError("Index is inconsistent.", null);
            }
            return documents;
        } catch ( IndexException ex ) {
            rebuildBecauseOfError("Search failed.", ex);
            throw ex;
        }
    }

    private void rebuildBecauseOfError(String message, IndexException ex) {
        log.error(message+" Rebuilding index...", ex);
        rebuild();
    }

    public void rebuild() {
        rebuildInBackground();
    }

    private void rebuildInBackground() {
        if ( buildingNewIndex ) {
            log.debug("Ignoring request to build new index. Already in progress.") ;
        } else {
            indexBuildingThread.start();
        }
    }

    private Thread createIndexBuildingThread() {
        Thread newIndexBuildingThread = new Thread("Background indexing thread") {
            public void run() {
                rebuildInForeground();
            }
        };
        newIndexBuildingThread.setPriority(Thread.MIN_PRIORITY);
        newIndexBuildingThread.setDaemon(true);
        return newIndexBuildingThread;
    }

    private void rebuildInForeground() {
        if ( buildingNewIndex ) {
            log.debug("Ignoring request to build new index. Already in progress.") ;
            return;
        }
        buildingNewIndex = true;
        try {
            File indexDirectoryFile = this.index.getDirectory();
            synchronized ( this ) {
                File newIndexDirectoryFile = indexDirectoryFile ;
                if (newIndexDirectoryFile.exists()) {
                    newIndexDirectoryFile = new File(indexDirectoryFile.getParentFile(), indexDirectoryFile.getName() + ".new");
                }
                DirectoryIndex newIndexDirectory = new DirectoryIndex(newIndexDirectoryFile);
                newIndexDirectory.indexAllDocuments();
                replaceIndexWithNewIndex(indexDirectoryFile, newIndexDirectory);
                considerDocumentsForNewIndex();
            }
        } catch ( IOException e ) {
            log.fatal("Failed to index all documents.", e);
        } finally {
            buildingNewIndex = false;
        }
    }

    private void considerDocumentsForNewIndex() throws IndexException {
        for (Iterator iterator = documentsToAddToNewIndex.iterator(); iterator.hasNext(); ) {
            DocumentDomainObject document = (DocumentDomainObject)iterator.next();
            index.indexDocument(document);
            iterator.remove();
        }
        for (Iterator iterator = documentsToRemoveFromNewIndex.iterator(); iterator.hasNext(); ) {
            DocumentDomainObject document = (DocumentDomainObject)iterator.next();
            index.removeDocument(document);
            iterator.remove();
        }
    }

    private void replaceIndexWithNewIndex(File indexDirectoryFile, DirectoryIndex newIndex) throws IOException {
        File oldIndex = new File(indexDirectoryFile.getParentFile(), indexDirectoryFile.getName()
                                                                 + ".old");
        if ( oldIndex.exists() ) {
            FileUtils.forceDelete(oldIndex);
        }
        if ( indexDirectoryFile.exists() && !indexDirectoryFile.renameTo(oldIndex) ) {
            log.error("Failed to rename \"" + indexDirectoryFile + "\" to \"" + oldIndex + "\".");
        }
        File newIndexDirectory = newIndex.getDirectory();
        if ( !newIndexDirectory.renameTo(indexDirectoryFile) ) {
            throw new IOException("Failed to rename \"" + newIndexDirectory + "\" to \""
                                  + indexDirectoryFile
                                  + "\".");
        }
        FileUtils.deleteDirectory(oldIndex);
    }

    private class ScheduledIndexingTimerTask extends TimerTask {
        public void run() {
            Date nextExecutionTime = new Date(this.scheduledExecutionTime() + indexingSchedulePeriodInMilliseconds);
            log.info("Starting scheduled index rebuild. Next rebuild at " + formatDatetime(nextExecutionTime));
            rebuildInForeground();
        }
    }

}
