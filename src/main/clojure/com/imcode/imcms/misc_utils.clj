(ns com.imcode.imcms.misc-utils)

(defn to-keyword-key-map
  "Converts keys of a map into keywords."
  [a-map]
  (into {}
    (for [[k v] a-map] [(keyword k) v])))