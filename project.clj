(defproject demo-grp/demo-art "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/test.check "1.1.1"]
                 [prismatic/schema "1.3.5"]
                 [tupelo "22.07.25a"]
                 ]
  :plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]
            [lein-ancient "0.7.0"]
            ]

  :profiles {:dev     {:dependencies []}
             :uberjar {:aot :all}}

  :global-vars {*warn-on-reflection* false}

  :main demo.core
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clj"]
  :target-path "target/%s"
  :compile-path "%s/class-files"
  :clean-targets [:target-path]

  :jvm-opts ["-Xms500m" "-Xmx4g"]
  )

;---------------------------------------------------------------------------------------------------
(comment
  (do
    (require '[clojure.java.browse :as cjb])
    (dotest
      (spyx (cjb/browse-url "http://yahoo.com")))))

