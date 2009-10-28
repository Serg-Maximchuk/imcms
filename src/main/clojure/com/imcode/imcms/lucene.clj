(ns com.imcode.imcms.lucene
  (:import
    (java.io File)
    (org.apache.lucene.store RAMDirectory)
    (org.apache.lucene.document Document Field Field$Store Field$Index)
    (org.apache.lucene.index IndexReader IndexWriter Term)
    (org.apache.lucene.analysis SimpleAnalyzer)
    (org.apache.lucene.queryParser QueryParser MultiFieldQueryParser)
    (org.apache.lucene.search IndexSearcher TermQuery Hits BooleanClause BooleanClause$Occur)

    (com.imcode.imcms.api DocumentService ContentManagementSystem))
  (:use clojure.contrib.duck-streams))


(def cms (atom nil))

(defn get-cms [username, password]
  (ContentManagementSystem/getContentManagementSystem username password))


(defn login [username password]
  (reset! cms (get-cms username password)))


(defn login-admin []
  (login "admin" "admin"))


(defn search [query]
  (let [docService (.getDocumentService @cms)
        parsedQuery (.parseLuceneSearchQuery docService query)]
    (seq (.search docService parsedQuery))))


(defn get-info [document]
  (let [internal (.getInternal document)
        meta (.getMeta internal)
        version (.getVersion meta)]
    {:id (.getId meta)
     :version_nr (.getNumber version)
     :version_tag (.. version getTag name)}))


(defn search-info [query]
  (map get-info (search query)))