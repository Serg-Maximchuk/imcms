(ns com.imcode.imcms.schema.upgrade-test
  (:require
    [clojure.contrib.sql :as sql])
  
  (:use
    clojure.contrib.test-is
    [com.imcode.imcms.project-utils :only [db-spec]])
  
  (:import
    (com.imcode.imcms.schema SchemaUpgrade Vendor)
    (java.io File)))


(def confXMLFile (File. "src/test/resources/schema-upgrade.xml"))
(def confXSDFile (File. "src/test/resources/schema-upgrade.xsd"))
(def scriptsDir (File. "src/main/sql"))
(def schema-name "x");


(deftest test-validate-xml
  (SchemaUpgrade/validateAndGetContent confXMLFile confXSDFile))


(deftest test-upgrade
  (sql/with-connection (db-spec)
    (sql/transaction
      (sql/do-commands
        (format "drop database if exists %s" schema-name)
        (format "create database %s" schema-name)
        (format "use %s" schema-name))


      (doto (SchemaUpgrade. (SchemaUpgrade/validateAndGetContent confXMLFile confXSDFile) scriptsDir)
        (.upgrade (sql/connection))))))