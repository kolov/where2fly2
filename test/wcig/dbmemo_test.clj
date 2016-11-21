(ns wcig.dbmemo-test
  (:require [clojure.test :refer :all]
            [wcig.dbmemo :refer :all]
            [wcig.dbbase :as dbbase]
            [wcig.util :refer :all]
            [midje.sweet :refer :all]
            ))

(def random-name (let [s (str (System/currentTimeMillis))] (.substring s (- (count s) 8))))

(fact "wrap function"
      (let [_ (dbbase/init "localhost" 27017)
            r1 (get-or-call random-name 1000 + 1 2)
            r2 (get-or-call random-name 1000 + 1 2)
            ]
        (find-call random-name 1 2) => truthy
        (find-call random-name 1 3) => falsey
        (find-call (str "x" random-name) 1 2) => falsey
        r1 => r2
        r2 => truthy
        (:timestamp (find-call random-name 1 2)) => truthy
        (:timestamp (find-call random-name 1 2)) => (:timestamp (find-call random-name 1 2))
        ))

