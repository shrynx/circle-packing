(ns circle-packing.core)

;; utils
(defn point-dist
  "given two points find the euclidean distance between them"
  [[x1 y1] [x2 y2]]
  (Math/sqrt (+ (Math/pow (- x1 x2) 2) (Math/pow (- y1 y2) 2))))

(defn random-range
  "given start and end generates a random number in range"
  [start end]
  (+ start (rand (- end start))))

(defn- get-fn-value
  "check if the agrument is a function then call it otherwise return as it is"
  [arg]
  (if (fn? arg) (arg) arg))

(defn make-circle
  "given center as 2d vec and radius generate a circle"
  [center radius]
  {:center center :radius radius})

(defn circles-intersect?
  "given two circles and optionallly padding checks if they intersect"
  ([circle-1 circle-2]
   (circles-intersect? circle-1 circle-2 0))
  ([padding {c1 :center r1 :radius} {c2 :center r2 :radius}]
   (let [actual-padding (get-fn-value padding)]
     (<= (point-dist c1 c2) (+ r1 r2 actual-padding)))))

(defn circle-intersects-any?
  "checks if the given circle intersects with any other given circles"
  ([circle all-circles]
   (some (partial circles-intersect? circle) all-circles))
  ([padding circle all-circles]
   (some (partial circles-intersect? padding circle) all-circles)))

(defn in-bound-rect?
  "given start and end points of a reactangle and a circle as arguments returns true if the provided circle is in bound where a circle has a :center as 2d vec and :radius. meant to be used for :in-bound?"
  [x-start y-start x-end y-end {[x y] :center r :radius}]
  (let [x1 (- x r)
        x2 (+ x r)
        y1 (- y r)
        y2 (+ y r)]
    (and (< x-start x1 x2 x-end) (< y-start y1 y2 y-end))))

(defn in-bound-circle?
  "given center as 2d vec, radius and a circle as arguments returns true if the provided circle is in bound where a circle has a :center as 2d vec and :radius. meant to be used for :in-bound?"
  [c1 r1 {c2 :center r2 :radius}]
  (> r1 (+ r2 (point-dist c1 c2))))

(defn random-in-rect
  "given a random number generator (taking min and max arguments), start and end points of a reactangle returns random points inside"
  [random x-start y-start x-end y-end]
  [(random x-start x-end) (random y-start y-end)])

(defn random-in-circle
  "given a random number generator, center as 2d vec and radius of a circle returns random points inside"
  [random [x y] radius]
  (let [random-angle (random (* Math/PI 2))
        random-radius (random radius)]
    [(+ x (* random-radius (Math/cos random-angle))) (+ y (* random-radius (Math/sin random-angle)))]))

;; core circle packing algorithm
(defn- gen-circle
  "takes a config and a list of existing circles"
  [{:keys [random-sampler min-radius max-radius max-attempts padding radius-step in-bounds?]} added-circles]
  (let [;; get values out of the paramenters
        start-radius (get-fn-value min-radius)
        end-radius (get-fn-value max-radius)
        radius-inc-step (get-fn-value radius-step)
        ;; utitlity to make a random circle with the given random sampler
        gen-random-circle #(do {:circle (make-circle (random-sampler) start-radius) :can-add false})
        ;; check if the circles interest with already added circles or is out of of bounds
        invalid-circle? (fn [circle] (or (circle-intersects-any? padding circle added-circles) (not (in-bounds? circle))))
        ;; generate valid base circle
        base-circle (loop [i max-attempts random-circle (gen-random-circle)]
                      (if (invalid-circle? (random-circle :circle))
                        (if (> i 0)
                          (recur (dec i) (gen-random-circle))
                          random-circle)
                        (update-in random-circle [:can-add] not)))
        ;; try and grow circle
        generated-circle (when (base-circle :can-add)
                           (loop [curr-radius start-radius orig-circle (base-circle :circle)]
                             (let [grown-circle (update-in orig-circle [:radius] + radius-inc-step)]
                               (if (invalid-circle? grown-circle)
                                 orig-circle
                                 (if (< curr-radius end-radius)
                                   (recur (+ curr-radius radius-inc-step) grown-circle)
                                   grown-circle)))))]
    generated-circle))

;; utitlity for returning all packed circles. useful for iterate
(defn- pack-circles [config circles]
  (let [generated-circle (gen-circle config circles)]
    (if (some? generated-circle) (conj circles generated-circle) circles)))

(def base-config {:min-radius 5
                  :max-radius 10
                  :max-circles 20
                  :radius-step 1
                  :max-attempts 100
                  :padding 0
                  :random-sampler #(random-in-rect random-range 0 0 100 100)
                  :in-bounds? (partial in-bound-rect? 0 0 100 100)})

(defn gen-packed-circles
  "returns a list of packed circles, where a circle has a :center as 2d vec and :radius.
   optinally takes a config map containing
   :min-radius a number or a 0-arity function returning a number
   :max-radius a number or a 0-arity function returning a number
   :radius-step a number or a 0-arity function returning a number
   :max-circles number
   :max-attempts number
   :padding a number or a 0-arity function returning a number
   :random-sampler a 0-arity function that return a 2d vec of random points
   :in-bounds? a function that takes a circle as argument and return boolean based on bound checks where a circle has a :center as 2d vec and :radius
   
   optionally also takes intial list of circles where a circle has a :center as 2d vec and :radius
   
   ---
   
   default config
   
   {:min-radius 5
    :max-radius 10
    :max-circles 20
    :radius-step 1
    :max-attempts 100
    :padding 0
    :random-sampler #(random-in-rect random-range 0 0 100 100)
    :in-bounds? (partial in-bound-rect? 0 0 100 100)}
   "
  ([]
   (gen-packed-circles base-config (list)))
  ([config]
   (gen-packed-circles config (list)))
  ([config initial-circles]
   (let [merged-config (merge base-config config)
         count (merged-config :max-circles)]
     (->> initial-circles
          (iterate (partial pack-circles merged-config))
          (take count)
          last))))
