(ns structural-typing.docs.f-wiki-condensed-type-descriptions
 (:require [structural-typing.type :refer :all]
           [structural-typing.global-type :refer :all]
           [clojure.string :as str])
 (:use midje.sweet structural-typing.assist.testutil))

(start-over!)

(type! :V1
       {[:x] [required-key]}
       {[:x] [even?]})
(type! :V2
       {[:x] [required-key even?]})

(tabular
  (fact 
    (with-out-str (checked ?version {}))
    => #":x must exist"
    
    (with-out-str (checked ?version {:x 1}))
    => #":x should be `even\?`")
  ?version
  :V1
  :V2)

(type! :V1 {[:x] [even?]})
(type! :V2 { :x   even? })

(tabular
  (fact 
    (checked ?version {:x 2}) => {:x 2}
    
    (with-out-str (checked ?version {:x 1}))
    => #":x should be `even\?`")
  ?version
  :V1
  :V2)

(type! :V1 {[:refpoint :x] [integer?]
            [:refpoint :y] [integer?]})
(type! :V2 {:refpoint {:x integer? 
            :y integer?}})

(tabular
  (fact 
    (with-out-str (checked ?version {:refpoint {:y "2"}}))
    => #"\[:refpoint :y\] should be `integer\?`")
  ?version
  :V1
  :V2)


(type! :Point {:x integer? :y integer?})
(type! :X {:refpoint (includes :Point)}
          {:refpoint {:color integer?}})

(fact 
  (with-out-str (checked :X {:refpoint {:y "2"}}))
  => #"\[:refpoint :y\] should be `integer\?`")


(type! :X {[:refpoint (each-of :x :y)] integer?})
(fact 
  (with-out-str (checked :X {:refpoint {:y "2"}}))
  => #"\[:refpoint :y\] should be `integer\?`")


(type! :Point (requires :x :y) {:x integer? :y integer?, :color string?})
(fact 
  (with-out-str (checked :Point {:y 1}))
  => #":x must exist"
  (with-out-str (checked :Point {:x "1" :y 1}))
  => #":x should be `integer\?`"
  (with-out-str (checked :Point {:x 1 :y 1, :color 1}))
  => #":color should be `string\?`"
  )

(type! :A-has-evens {[:a ALL] even?})
(fact 
  (with-out-str (checked :A-has-evens {:a [1 2]}))
  => #"\[:a 0\] should be `even")

(type! :Terminal {[:a ALL] [required-key even?]})
(type! :Middle {[:a ALL :b] [even? required-key]}) ; doesn't matter where `required-key` is.
(type! :Double {[:a ALL :b ALL] [required-key even?]})

(fact 
  (with-out-str (checked :Terminal {})) => #":a must exist"

  (with-out-str (checked :Middle {})) => #":a must exist"
  (checked :Middle {:a []}) => {:a []}
  (with-out-str (checked :Middle {:a [{:c 1}]})) => #"\[:a 0 :b\] must exist"

  (with-out-str (checked :Double {})) => #":a must exist"
  (checked :Double {:a []}) => {:a []}
  (with-out-str (checked :Double {:a [{:c 1}]})) => #"\[:a 0 :b\] must exist"
  (checked :Double {:a [{:b []}]}) => {:a [{:b []}]}
  (with-out-str (checked :Double {:a [{:b [1]}]})) => #"\[:a 0 :b 0\] should be `even"
  (checked :Double {:a [{:b [2 4]}]}) => {:a [{:b [2 4]}]})


(type! :DoubleNested {[:a ALL :b ALL] even?})
(fact 
  (with-out-str (checked :DoubleNested {:a [{:b [4 8]} {:b [0 2]} {:b [1 2 4]}]}))
  => #"\[:a 2 :b 0\] should be `even\?")


(type! :Figure {[:points ALL (each-of :x :y)] [required-key integer?]})
(fact 
  (let [result (with-out-str (checked :Figure {:points [{:x "1"}]}))]
    result => #"\[:points 0 :y\] must exist"
    result => #"\[:points 0 :x\] should be `integer"))


(type! :Point {:x integer? :y integer?})
(type! :V1 {[:points ALL] (includes :Point)})
(type! :V2 {[:points ALL] {:x integer? :y integer?}})

(tabular
  (fact 
    (checked ?version {:x 2}) => {:x 2}
    
    (with-out-str (checked ?version {:points [{:x "1" :y 1}]}))
    => #"\[:points 0 :x\] should be `integer")
  ?version
  :V1
  :V2)

(type! :V1 (requires :x [:y :z]))

(type! :V2 {[:x] [required-key]
            [:y :z] [required-key]})
(type! :V3 (requires :x [:y :z]))

(tabular
  (fact 
    (checked ?version {:x 2
                       :y {:z 1}}) => {:x 2
                                       :y {:z 1}}
    
    (with-out-str (checked ?version {:x 2 :y 3}))
    => #"\[:y :z\] must exist")
  ?version
  :V1
  :V2
  :V3)

(type! :Point
       (requires :x :y)
       {:x integer? :y integer?})

(type! :Line1
       (requires :start :end :color)
       {:color string?
        :start (includes :Point)
        :end (includes :Point)})

(type! :Line2
       (requires [:start (paths-of :Point)]
                 [:end (paths-of :Point)]
                 :color)
       {:color string?
        :start (includes :Point)
        :end (includes :Point)})

(future-fact "There should be an error if there's an instance of `includes` in a path")

(tabular
  (fact 
    (let [result (check-for-explanations ?version {:start {:x 1 :y "2"}})]
      result => (contains (err:required :color)
                          (err:required [:end :x])
                          (err:required [:end :y])
                          (err:shouldbe [:start :y] "integer?" "\"2\"")
                          :in-any-order :gaps-ok)
      ))
  ?version
  :Line1
  :Line2)


(type! :Point1
       (requires :x :y :color)
       {[(through-each :x :y)] integer?})

(type! :Point2
       {[:x] [required-key integer?]
        [:y] [required-key integer?]
        [:color] [required-key]})

(tabular
  (fact 
    (let [result (with-out-str (checked ?version {:x 3.0}))]
      result => #":color must exist"
      result => #":x should be `integer"
      result => #":y must exist"))
  ?version
  :Point1
  :Point2)


(start-over!)
