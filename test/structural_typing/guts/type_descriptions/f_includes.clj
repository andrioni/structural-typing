(ns structural-typing.guts.type-descriptions.f-includes
  (:require [structural-typing.guts.type-descriptions.includes :as subject])
  (:use midje.sweet
        structural-typing.assist.testutil
        structural-typing.assist.special-words))


(fact includes
  (let [point {[:x] [integer?] [:y] [integer?]}
        type-map {:Point point}]
    (subject/substitute type-map (includes :Point)) => point
    (subject/substitute type-map [(includes :Point)]) => [point]
    (subject/substitute type-map {:a (includes :Point)}) => {:a point}
    (subject/substitute type-map {[:a :points ALL] [required-path (includes :Point)]})
    => {[:a :points ALL] [required-path point]}
    (subject/substitute type-map {:a [required-path pos?]}) => {:a [required-path pos?]}))
