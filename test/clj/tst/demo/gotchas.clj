;   Copyright (c) Alan Thompson. All rights reserved.
;   The use and distribution terms for this software are covered by the Eclipse Public License 1.0
;   (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at
;   the root of this distribution.  By using this software in any fashion, you are agreeing to be
;   bound by the terms of this license.  You must not remove this notice, or any other, from this
;   software.
(ns tst.demo.gotchas
  (:use tupelo.core tupelo.test)
  (:require
    [clojure.set :as set]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [tupelo.core :as t]
    [tupelo.string :as ts]))

; #todo add example for duplicates in clojure.core.combo
; #todo make work for clj/cljs

(verify
  ; the less-than and greater-than operators work for numbers in different categories
  (is= (< 1 2.0) true)
  (is= (< 1 1.0) false)
  (is= (> 1 1.0) false)
  (is= (> 1 0.2) true)

  ; single-equals `=` doesn't work for integer and floating point values (never!)
  (is= (= 1 1.0) false)

  ; double-equals `==` works for numbers (only) in different categories
  (is= (== 1 1.0) true))


(verify
  ; Expected, intuitive behavior
  (throws? (seq 5))
  (= [5] (vector 5))
  (throws? (vec 5))
  (= [5] (list 5))
  (throws? (apply list 5))
  (throws? (first 5))
  (throws? (second 5))
  (throws? (rest 5))
  (throws? (next 5)))

(verify
  ; Unexpected, non-intuitive behavior
  (is= nil (seq nil)) ; should throw
  (is= [nil] (vector nil))
  (is= [] (vec nil)) ; should throw
  (is= [nil] (list nil))
  (is= [] (apply list nil))
  (is= nil (first nil)) ; should throw
  (is= nil (second nil)) ; should throw
  (is= [] (rest nil)) ; should throw
  (is= nil (next nil))) ; should throw

(verify
  ; Unexpected, non-intuitive behavior
  (is= nil (seq [])) ; should be []
  (is= [] (vec []))
  (is= [[]] (list []))
  (is= [] (apply list []))
  (is= nil (first [])) ; should throw
  (is= nil (second [])) ; should throw
  (is= [] (rest [])) ; should throw
  (is= nil (next []))) ; should throw

(verify
  (is= [5] (seq [5]))
  (is= [5] (vec [5]))
  (is= [5 6] (vec [5 6]))
  (is= [[5]] (list [5]))
  (is= [5] (apply list [5]))
  (is= [5 6] (apply list [5 6]))
  (is= [6 5] (into (list) [5 6])) ; accidentally reversed
  (is= [6 5] (into nil [5 6])) ; accidentally reversed
  (is= 5 (first [5]))
  (is= nil (second [5])) ; should throw
  (is= [] (rest [5]))
  (is= nil (next [5]))) ; should be []

(verify   ; duality between `nil` and empty list
  (is= [1] (cons 1 nil))
  (is= [5] (conj nil 5))
  (is= [3 2 1] (into nil [1 2 3]))

  (is= [5] (take 1 [5 6 7]))
  (is= [] (take 1 [])) ; should be error

  (is= [6 7] (drop 1 [5 6 7]))
  (is= [] (drop 1 [])) ; should be error

  ; Predictable bahavior
  (throws? (t/xtake 1 []))
  (throws? (t/xdrop 1 []))
  )

(verify
  ; Predictable bahavior
  (throws? (t/xfirst nil))
  (throws? (t/xsecond nil))
  (throws? (t/xrest nil)) ; drop first item or throw if not more

  (throws? (t/xfirst []))
  (throws? (t/xsecond []))
  (throws? (t/xrest [])) ; drop first item or throw if not more

  (is= 5 (t/xfirst [5]))
  (throws? (t/xsecond [5]))
  (is= [] (t/xrest [5])) ; drop first item or throw if not more
  (is= [5] (t/xrest [4 5])))

; vec & (apply list ...) too loose
(verify
  (is= [] (vec nil)) ; should throw
  (is= [] (apply list nil)) ; should throw

  (is= [] (vec []))
  (is= [] (apply list []))

  (is= [5] (vec [5]))
  (is= [5] (apply list [5])))

(verify
  (is= [1 2 3] (conj [1] 2 3))
  (is= [1 2 3] (conj [1 2] 3))

  (is= [3 2 1] (conj (list) 1 2 3))
  (is= [3 2 1] (conj (list 1) 2 3))
  (is= [3 1 2] (conj (list 1 2) 3))

  (is= [1 2 3] (into (vector) [1 2 3]))
  (is= [1 2 3] (into (vector 1) [2 3]))
  (is= [1 2 3] (into (vector 1 2) [3]))

  (is= [3 2 1] (into (list) [1 2 3]))
  (is= [3 2 1] (into (list 1) [2 3]))
  (is= [3 1 2] (into (list 1 2) [3])))

; Clojure is consistent & symmetric for if/if-not, when/when-not, every?/not-every?
; Clojure is inconsistent & broken for
;      empty vs     empty?
;  not-empty vs not-empty?
;  any?
;  some vs some? (truthy vs not-nil?)

; Clojure has `empty?` but no `not-empty?`.  However, it does have `empty` and `not-empty`.  Confusing!
; empty / not-empty vs empty? (not-empty? missing)
; not-empty? is missing for no good reason
; empty/not-empty are not mirror images of each other; (not (empty coll)) != (not-empty coll)
(verify
  (is= (empty [1 2 3]) [])
  (is= (not-empty [1 2 3]) [1 2 3]
    (t/validate t/not-empty? [1 2 3])) ; explicit validation of non-empty collection
  (is= (not (empty [1 2 3])) false)

  (is= (empty? [1 2 3]) false)
  ;(not-empty?  [1 2 3])  => Unable to resolve symbol: not-empty?
  (is= (t/not-empty? [1 2 3]) true) ; explicit test for non-empty collection

  ; empty? / count too loose:
  (is= true (empty? nil))
  (is= 0 (count nil)))

(verify
  (is= nil (some #{false} [false true]))
  (is= true (some #(= false %) [false true]))
  (is= true (some #{false true} [false true]))
  (is= true (some #{false true} [false true]))
  (is= true (some? false))
  (is= true (some? true)))

(verify
  (is= false (contains? [1 2 3 4] 4))
  (is= false (contains? [:a :b :c :d] :a)))

; map oddities
(verify
  (is= {:a 1 :b 2} (conj {:a 1} [:b 2])) ; MapEntry as 2-vec
  (is= {:a 1} (conj {:a 1} nil)) ; this is ok => noop
  (throws? (conj {:a 1} [])) ; illegal
  (is= {:a 1 :b 2} (conj {:a 1} {:b 2})) ; this works, but shouldn't
  (let [me (first {:a 1})]
    (is= clojure.lang.MapEntry (type me))
    (is= [:a 1] me)) ; MapEntry is "equal" to a 2-vec

  (is= {:a 1} (into {:a 1} nil))
  (is= {:a 1} (into {:a 1} []))
  (throws? (into {:a 1} [:b 2]))
  (is= {:a 1 :b 2} (into {:a 1} {:b 2}))

  ; nil same as {} (empty map)
  (is= {:a 1} (assoc nil :a 1))
  (is= {:a {:b 1}} (assoc-in nil [:a :b] 1))
  (is= {} (into {} nil))

  (is= nil (get {:a 1} [:x :y])) ; doesn't fail when should have been `get-in`

  ; clojure.lang.MapEntry passes sequential?  (argh!!!)
  (let [m    {:a 1 :b 2}
        me   (first m)
        avec [1 2 3]
        alst (list 1 2 3)]
    (is= clojure.lang.MapEntry (type me))
    (is (sequential? me))
    (isnt (t/xsequential? me))
    (is (t/xsequential? avec))
    (is (t/xsequential? alst))))

(verify   ; regarding get & nil
  (is= nil (get nil :a))
  (is= nil (get-in nil [:a :b]))
  (is= nil (:a nil))
  (is= nil (:a {:b 2}))
  (is= nil (get {:b 2} nil))
  (is= nil (get nil nil)))

;-----------------------------------------------------------------------------
; Incorporate all from:
;    https://stackoverflow.com/questions/64237949/value-of-a-list-with-quoted-first-argument-in-clojure-is-the-last-argument


(verify   ; conj inconsistencies
  (is= [1 2 nil] (conj [1 2] nil))
  (is= [nil 1 2] (conj '(1 2) nil))
  (is= {:a 1} (conj {:a 1} nil))
  (is= #{1 2 nil} (conj #{1 2} nil))
  (throws? (conj "ab" nil)))

(verify
  (is= [] (flatten #{1 2 3}))
  )

(verify
  (is= "abc" (str "ab" \c))
  (is= "ab" (str "ab" nil))
  (is= "abc" (str "ab" nil \c))

  (is= "abc" (str "abc"))
  (is= "{:s 'abc'}" (ts/quotes->single (str {:s "abc"}))) ; calls pr-str automatically

  (is= "123" (ts/quotes->single (pr-str 123)))
  (is= "'abc'" (ts/quotes->single (pr-str "abc")))
  (is= "nil" (ts/quotes->single (pr-str nil)))
  )


; "generic" indexing is a problem; always be explicit with first, nth, get, etc
(verify
  (let [vv [1 2 3]
        ll (list 1 2 3)
        cc (cons 1 [2 3])]
    (is= 1 (vv 0)) ; works fine
    (throws? (ll 0)) ; clojure.lang.PersistentList cannot be cast to clojure.lang.IFn
    (throws? (cc 0)) ; clojure.lang.Cons cannot be cast to clojure.lang.IFn

    ; best solution
    (is= 1 (first vv))
    (is= 1 (first ll))
    (is= 1 (first cc))))

; Assume symbol or keyword first means associative lookup
(verify
  ; with keyword
  (is= 1 (:a {:a 1}))
  (is= 1 (:a {:a 1} 9))
  (is= 9 (:b {:a 1} 9))
  (is= 9 (:b "dummy" 9))
  (is= 9 (:b :dummy 9))
  (is= 9 (:b 1234567 9))
  ; with symbols
  (is= 1 ('a {'a 1}))
  (is= 1 ('a {'a 1} 9))
  (is= 9 ('b {'a 1} 9))
  (is= 9 ('b "dummy" 9))
  (is= 9 ('b :dummy 9))
  (is= 9 ('b 1234567 9))

  (is= [0 1 2 3 4]
    (:or "abc" (range 5)) ; keyword function morphs into `get` function call
    (get "abc" :or (range 5))))

; binding operates in parallel, not sequentially
(def ^:dynamic xx :x-old)
(def ^:dynamic yy :b-old)
(verify
  (is= xx :x-old)
  (is= yy :b-old)
  (binding [xx :x-new
            yy xx] ; gets the old `xx` value
    (is= xx :x-new)
    (is= yy :x-old))) ; ***** OLD VALUE *****

; every? not-every? some not-any? + has-some? has-none?
(verify   ; should throw if empty arg
  (is (every? even? []))
  (is (every? odd? [])))


; `case` automatically quotes its argument with misleading results
(verify
  (let [expected    123
        value       123
        case-result (case value
                      expected :success
                      :failure)
        cond-result (cond
                      (= value expected) :success
                      :else :failure)]
    (is= :failure case-result)
    (is= :success cond-result))
  (let [value       2
        result-case (case value
                      (first [2 3 4]) :success
                      :fail)
        result-cond (cond
                      (= value (first [2 3 4])) :pass
                      :else :fail)]
    (is= :fail result-case)
    (is= :pass result-cond)))

; Var surprises
(verify
  (def some-var 5) ; Set a value to a Var (& create if needed)
  (is= 5 some-var)
  (def some-var 6) ; Set a new value in the Var
  (is= 6 some-var)
  (throws? (alter-var-root some-var inc)) ; forgot to deref the Var-Symbol
  (alter-var-root (var some-var) inc) ; Must deref the symbol to get the Var object
  (is= 7 some-var)

  (def ^:dynamic some-dynvar 5) ; Set a value to a Var (& create if needed)
  (is= 5 some-dynvar)
  (def ^:dynamic some-dynvar 6) ; Set a new value in the Var
  (is= 6 some-dynvar)
  (throws? (alter-var-root some-dynvar inc)) ; forgot to deref the Var-Symbol
  (alter-var-root (var some-dynvar) inc) ; Must deref the symbol to get the Var object
  (is= 7 some-dynvar)

  )

; transducer surprises
(verify
  ; when use `comp` with normal functions, data flows leftward (result <-- fn <-- data)
  (let [comp-fn        (comp #(mapv inc %) #(filter even? %))
        result-comp-fn (comp-fn (range 10))]
    (is= [1 3 5 7 9] result-comp-fn))

  ; when use `comp` with transducers, data flows rightward (data --> txd --> result )
  (let [xform            (comp (map inc) (filter even?))
        result-comp-txd  (into [] xform (range 10))
        result-transduce (transduce xform
                           conj [] (range 10)) ; alternate syntax
        ]
    (is= [2 4 6 8 10] result-comp-txd result-transduce))

  ; "dataflow" processing with thread-first macro is top->bottom (i.e. rightward)
  (let [result-thread (->> (range 10)
                        (map inc)
                        (filter even?))]
    (is= [2 4 6 8 10] result-thread)))

; Double and (with-precision ...) doesn't work
(verify
  (let [a6 1.112233
        a4 1.1122
        a2 1.11

        b6 1.667788
        b4 1.6678
        b3 1.67]
    (is= a6 (with-precision 6 a6))
    (is= a6 (with-precision 4 a6))
    (is= a6 (with-precision 2 a6))

    (is= b6 (with-precision 6 b6))
    (is= b6 (with-precision 4 b6))
    (is= b6 (with-precision 2 b6))
    ))

; BigDecimal and (with-precision ...) doesn't work
(verify
  (let [a6 (bigdec 1.112233)
        a4 (bigdec 1.1122)
        a2 (bigdec 1.12)

        b6 (bigdec 1.667788)
        b4 (bigdec 1.6678)
        b3 (bigdec 1.67)]
    (is= a6 (with-precision 6 a6))
    (is= a6 (with-precision 4 a6))
    (is= a6 (with-precision 2 a6))

    (is= b6 (with-precision 6 b6))
    (is= b6 (with-precision 4 b6))
    (is= b6 (with-precision 2 b6))))

(t/when-clojure-1-9-plus
  (verify
    ; `any?` always returns true
    (is= true (any? false))
    (is= true (any? nil))
    (is= true (any? 5))
    (is= true (any? "hello"))

    ; tests a predicate fn on each element
    (is= false (not-any? odd? [1 2 3]))
    (is= true (not-any? odd? [2 4 6]))

    ; explicit & consistent way of testing predicate
    (is (t/has-some? odd? [1 2 3]))
    (is (t/has-none? odd? [2 4 6]))
    ))

; #todo fix this!
;
; samples for dospec & check-isnt
;-----------------------------------------------------------------------------
;(dospec 9
;  (prop/for-all [val (gen/vector gen/any)]
;    (is (= (not (empty? val)) (t/not-empty? val)))
;    (isnt= (empty? val) (empty val))))
;(t/when-clojure-1-9-plus
;  (verify
;    (check-isnt 33
;      (prop/for-all [val (gen/vector gen/int)]
;        (= (any? val) (not-any? odd? val))))))
;

;-----------------------------------------------------------------------------
; quote surprises
(verify
  (is= 'quote (first ''hello)) ; 2 single quotes
  (isnt= '{:a 1 :b [1 2]} '{:a 1 :b '[1 2]})
  (is= '{:a 1 :b [1 2]} `{:a 1 :b [1 2]})
  (is= '{:a 1 :b [1 2]} (quote {:a 1 :b [1 2]})))

;-----------------------------------------------------------------------------
; record-map equality fails
(defrecord SampleRec [a b])
(verify
  (let [sampleRec (->SampleRec 1 2)]
    (isnt (= sampleRec {:a 1 :b 2})) ; fails for clojure.core/= "
    (is (t/val= sampleRec {:a 1 :b 2})))) ; works for tupelo.core/val=

;-----------------------------------------------------------------------------
; clojure.set has no type-checking
(verify
  (is= [:z :y :x 1 2 3] (set/union '(1 2 3) '(:x :y :z)))
  (is= [1 2 3 :x :y :z] (set/union [1 2 3] [:x :y :z]))
  (is= #{1 2 3 :x :y :z} (set/union #{1 2 3} #{:x :y :z})))

;-----------------------------------------------------------------------------
; clojure.string has no type-checking
(verify
  (is= "false" (clojure.string/lower-case false))
  ; (throws? (clojure.string/lower-case false))   ; #todo fix in tupelo.string
  )



;#?(:cljs
;   (do
;     ; assumes nil=0, etc (from JS)
;     (dotest
;       (is= {:a 1} (update {} :a inc))
;       (is= 1 (inc nil))
;       (is= 1 (+ 1 nil)))
;
;     ; from skillsmatter.com ClojureExchange 2018 "Fullstack Clojure in the Movie Business"
;     (let [foo-bar 1
;           foo_bar 2]
;       (is= foo-bar 2)) ; #todo verify or not???
;
;     ))
