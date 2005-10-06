package imcode.server.document.index;

import imcode.server.document.DocumentDomainObject;
import imcode.util.CounterStringFactory;
import imcode.util.ShouldNotBeThrownException;
import imcode.util.Utility;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import java.io.File;

public class BackgroundIndexBuilder {
    private final static Logger log = Logger.getLogger(BackgroundIndexBuilder.class);

    private final File indexParentDirectory;
    private final AutorebuildingDirectoryIndex autorebuildingDirectoryIndex;

    private IndexBuildingThread indexBuildingThread;
    private long previousIndexParentDirectoryLastModified;

    public BackgroundIndexBuilder(File indexParentDirectory, AutorebuildingDirectoryIndex autorebuildingDirectoryIndex) {
        this.indexParentDirectory = indexParentDirectory;
        this.autorebuildingDirectoryIndex = autorebuildingDirectoryIndex;
        indexParentDirectory.setLastModified(System.currentTimeMillis()) ;
        previousIndexParentDirectoryLastModified = indexParentDirectory.lastModified() ;
    }

    public synchronized void start() {
        try {
            NDC.push(ClassUtils.getShortClassName(getClass())+"-"+Utility.numberToAlphaNumerics(System.identityHashCode(this)));

            log.info("Starting index rebuild.");
            
            if (previousIndexParentDirectoryLastModified != 0 ) {
                long lastModified = indexParentDirectory.lastModified();
                if (lastModified > previousIndexParentDirectoryLastModified ) {
                    log.trace("Expected last modified "+previousIndexParentDirectoryLastModified+" but got "+lastModified);
                    log.debug("Another process modified index directory. Aborting.") ;
                    rememberParentDirectoryLastModified();
                    throw new AlreadyRebuildingIndexException() ;
                }
            }

            touchIndexParentDirectory();

            if ( null != indexBuildingThread && indexBuildingThread.isAlive() ) {
                log.debug("Ignoring request to build new index. Already in progress.");
                return;
            }

            File indexDirectory = getNewIndexDirectory(indexParentDirectory);
            if ( !indexDirectory.mkdirs() ) {
                log.warn("Failed to create new index directory. Will try again next time.");
                return;
            }

            log.debug("Created directory "+indexDirectory);
            
            rememberParentDirectoryLastModified();

            try {
                log.debug("Starting index rebuild thread.") ;
                indexBuildingThread = new IndexBuildingThread(this, indexDirectory);
                indexBuildingThread.start();
            } catch ( IllegalThreadStateException itse ) {
                throw new ShouldNotBeThrownException(itse);
            }
        } finally {
            NDC.pop();
        }
    }

    private void touchIndexParentDirectory() {
        indexParentDirectory.setLastModified(System.currentTimeMillis()) ;
    }

    public synchronized void addDocument(DocumentDomainObject document) {
        if ( null != indexBuildingThread ) {
            indexBuildingThread.addDocument(document);
        }
    }

    public synchronized void removeDocument(DocumentDomainObject document) {
        if ( null != indexBuildingThread ) {
            indexBuildingThread.removeDocument(document);
        }
    }

    private static File getNewIndexDirectory(File indexParentDirectory) {
        return (File) Utility.findMatch(new CounterFileFactory(indexParentDirectory), new UniqueFilePredicate()) ;
    }

    public synchronized void notifyRebuildComplete(DefaultDirectoryIndex newIndex) {
        autorebuildingDirectoryIndex.notifyRebuildComplete(newIndex) ;
        rememberParentDirectoryLastModified();
    }

    private void rememberParentDirectoryLastModified() {
        previousIndexParentDirectoryLastModified = indexParentDirectory.lastModified() ;
    }

    private static class CounterFileFactory extends CounterStringFactory {

        private File parentDirectory;

        CounterFileFactory(File parentDirectory) {
            super(1) ;
            this.parentDirectory = parentDirectory;
        }

        public Object create() {
            return new File(parentDirectory, (String) super.create());
        }

    }

    private static class UniqueFilePredicate implements Predicate {

        public boolean evaluate(Object object) {
            File file = (File) object;
            return !file.exists();
        }
    }
}
