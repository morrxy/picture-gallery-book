(ns picture-gallery.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [picture-gallery.layout :refer [error-page]]
            [picture-gallery.routes.home :refer [home-routes]]
            [picture-gallery.routes.services :refer [service-routes restricted-service-routes]]
            [picture-gallery.middleware :as middleware]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [config.core :refer [env]]
            [picture-gallery.config :refer [defaults]]
            [mount.core :as mount]
            [luminus.logger :as logger]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (logger/init env)
  (doseq [component (:started (mount/start))]
    (log/info component "started"))
  ((:init defaults)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "picture-gallery is shutting down...")
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (log/info "shutdown complete!"))

(def app-routes
  (routes
    #'service-routes
    (wrap-routes #'restricted-service-routes middleware/wrap-auth)
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
