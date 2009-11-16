(ns com.imcode.imcms.compojure

  (:gen-class :extends javax.servlet.http.HttpServlet))


(use 'compojure 'clojure.contrib.def)

(defn html-doc
  [title & body]
  (html
    (doctype :html4)
    [:html
      [:head
        [:title title]]
      [:body
        [:div
          [:h2
            [:a {:href "/"} "Main (welcome page)"]
          [:h2
            [:a {:href "/doc"} "Document"]]
          [:h2
            [:a {:href "/user"} "Users"]]
          [:h2
            [:a {:href "/role"} "Roles"]]]]
        body]]))

(defn handle-doc
  [action]
  (let []
    (html-doc "Result"
      "Processing: " action)))

(defn undefined [request]
  (html-doc "Undefined"
    request))

(defn welcome-page
  []
  (html-doc "imCMS admin" ">> Please pick a task <<"))

(defroutes admin-services
  (GET "/"
    (welcome-page))

  (GET "/doc"
    (html-doc "Document"
      (form-to [:post "/doc"]
        (drop-down "action" [:new :delete :search])
        (submit-button "Go"))))

  (POST "/doc"
    (handle-doc (params :action)))

  (GET "/user"
    (undefined request))

  (GET "/role"
     (undefined request))

  (GET "/image"
    (java.io.File. "/Users/ajosua/Pictures/ant.jpg"))  

  (ANY "*"
    [404 "Page Not Found"]))   







(defvar srv (atom nil))

(defn srv-start
  ([]
    (srv-start 8080))

  ([port]
    (reset! srv
      (run-server
        {:port port}
        "/*"
        (servlet admin-services)))))

(defn srv-stop []
  (stop @srv))
