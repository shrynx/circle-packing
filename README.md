# circle-packing

[![Build Status](https://travis-ci.org/shrynx/circle-packing.svg?branch=master)](https://travis-ci.org/shrynx/circle-packing)
[![codecov](https://codecov.io/gh/shrynx/circle-packing/branch/master/graph/badge.svg)](https://codecov.io/gh/shrynx/circle-packing)
[![Clojars Project](https://img.shields.io/clojars/v/circle-packing.svg)](https://clojars.org/circle-packing)

![tarbell-packing](https://user-images.githubusercontent.com/4706261/95109777-5a0af880-073d-11eb-8de8-9de6575e521d.png)

A Clojure (script) library designed for circle packing.

```clj
[circle-packing "0.0.0"]
```

## Usage

exposes `gen-packed-circles` which optionally takes a config map and returns list of circles having `:center` as 2d vec and `:radius`

## Params

`:min-radius` a number or a 0-arity function returning a number

`:max-radius` a number or a 0-arity function returning a number

`:radius-step` a number or a 0-arity function returning a number

`:max-circles` number

`:max-attempts` number

`:padding` a number or a 0-arity function returning a number

`:random-sampler` a 0-arity function that return a 2d vec of random points

`:in-bounds?` a function that takes a circle as argument and return boolean based on bound checks where a circle has a `:center` as 2d vec and `:radius`

## Default

```clj
{:min-radius 5
 :max-radius 10
 :max-circles 20
 :radius-step 1
 :max-attempts 100
 :padding 0
 :random-sampler #(random-in-rect random-range 0 0 100 100)
 :in-bounds? (partial in-bound-rect? 0 0 100 100)}
```

## Utilities

A few handy utitlies functions are also provided

`in-bound-rect?` given start and end points of a reactangle returns true if the provided circle is in bound

`in-bound-circle?` given center as 2d vec and radius of circle checks if the provided circle is in bound

`random-in-rect` given a random number generator (taking min and max arguments), start and end points of a reactangle returns random points inside

`random-in-circle` given a random number generator, center as 2d vec and radius of a circle returns random points inside

## Examples

Usage with random generator from [quil](http://www.quil.info/) 

```clj
{:min-radius 5
 :max-radius #(q/random 20 30)
 :max-circles 100
 :padding 4
 :random-sampler #(random-in-rect q/random 0 0 150 100)
 :in-bounds? (partial in-bound-rect? 0 0 150 100)}
```

![circles-in-rect](https://user-images.githubusercontent.com/4706261/95065595-8d2f9680-0701-11eb-8a9a-4778598b0e1f.png)

```clj
{:min-radius 5
 :max-radius #(q/random 20 30)
 :max-circles 100
 :padding 4
 :random-sampler #(random-in-circle q/random [50 50] 50)
 :in-bounds? (partial in-bound-circle? [50 50] 50)}
```

![circles-in-circle](https://user-images.githubusercontent.com/4706261/95065339-32963a80-0701-11eb-81fc-919e16d1da06.png)

## License

Copyright © 2020 shrynx

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
