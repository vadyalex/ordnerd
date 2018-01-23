(ns ordnerd.markdown)

(defn wrap
  [^CharSequence s ^CharSequence padding]
  (if (or (clojure.string/blank? s)
          (clojure.string/blank? padding))
    s
    (str padding s padding)))

(defn italic
  [^CharSequence s]
  (wrap s "_"))

(defn bold
  [^CharSequence s]
  (wrap s "*"))

(defn fixed
  [^CharSequence s]
  (wrap s "`"))
