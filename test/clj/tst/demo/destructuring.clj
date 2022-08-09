(ns tst.demo.destructuring
  (:use tupelo.core tupelo.test))

;-----------------------------------------------------------------------------
; normal params as you would expect
(defn foo
  [a b m]
  (is= [a b] [1 2])
  (is= m {:x 3 :y 4}))

; the use of rest args first places the map in a list, but then rest-destructuring
; unwraps the map from the list before pulling out the values x, y, & m
(defn bar
  [a b & {:keys [x y] :as m}]
  (is= [a b] [1 2])
  (is= [x y] [3 4])
  (is= m {:x 3 :y 4}))

(verify
  (foo 1 2 {:x 3 :y 4})
  (bar 1 2 {:x 3 :y 4}))

;-----------------------------------------------------------------------------
(defn bazz
  [a b & extras]
  (is= [a b] [1 2]) ; regular scalar args
  (is= extras (list {:x 3 :y 4})) ; a map wrapped in a seq as expected

  ; destructuring as normal
  (let [
        {:keys [x y] :as m} {:x 3 :y 4}]
    (is= [x y] [3 4])
    (is= m {:x 3 :y 4}))

  ; Even plain destructuring SILENTLY UNWRAPS the map from inside the list.
  ; Note that this IS NOT rest-destructuring
  (let [
        {:keys [x y] :as m} (list {:x 3 :y 4})]
    (is= [x y] [3 4])
    (is= m {:x 3 :y 4}))

  ; a vector IS NOT silently unwrapped
  (let [
        {:keys [x y] :as m} (vector {:x 3 :y 4})]
    (is= [x y] [nil nil])
    (is= m [{:x 3 :y 4}])) ; the map is still wrapped in a vector

  ; a seq is SILENTLY UNWRAPPED just like a list
  (let [
        {:keys [x y] :as m} (seq (vector {:x 3 :y 4}))]
    (is= [x y] [3 4])
    (is= m {:x 3 :y 4})))

(defn bass
  [a b & extras]
  (is= [a b] [1 2]) ; regular scalar args
  (is= extras (list {:x 3 :y 4})) ; a map wrapped in a seq as expected

  (let [[e1 e2] extras ; sequence destructuring works as expected
        {:keys [x y] :as m} e1] ; regular destructuring
    (is= e1 {:x 3 :y 4})
    (is= e2 nil)
    (is= [x y] [3 4])
    (is= m {:x 3 :y 4})))

(verify
  (bazz 1 2 {:x 3 :y 4})
  (bass 1 2 {:x 3 :y 4}))

;-----------------------------------------------------------------------------
(verify
  ; normal Clojure destructuring
  (let [{:keys [a b]} {:a 1 :b 2}]
    (is= [a b] [1 2]))

  ; easier ways to create & destructure a keyword map
  (let [a 1
        b 2
        m (vals->map a b)] ; create a keyword map
    (is= m {:a 1 :b 2})
    (with-map-vals m [a b] ; destructure a keyword map
      (is= [a b] [1 2]))))

;-----------------------------------------------------------------------------
(verify   ; rest-destructuring
  (let [m  {:a 1 :b 2}
        mv (list {:a 1 :b 2}) ; a map in a list
        ms [:a 1 :b 2]] ; a sequence

    ; normal destructuring as you would expect
    (let [
          {:keys [a b] :as m1} m]
      (is= [a b] [1 2])
      (is= m1 m))

    ; rest-destructuring UNWRAPS THE MAP FROM INSIDE THE LIST
    (let [
          [& {:keys [a b] :as m2}] mv]
      (is= [a b] [1 2])
      (is= m2 m))

    ; rest-destructuring CONVERTS THE SEQUENCE BACK INTO A MAP
    (let [
          [& {:keys [a b] :as m3}] ms]
      (is= [a b] [1 2])
      (is= m3 m))))


