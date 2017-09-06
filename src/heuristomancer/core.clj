(ns heuristomancer.core
  (:gen-class)
  (:use [clojure.java.io :only [reader writer input-stream as-file]]
        [clojure.tools.cli :only [parse-opts]]
        [heuristomancer.loader :only [load-parsers]])
  (:require [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.data.csv :as csv])
  (:import [java.util.zip GZIPInputStream]
           [java.io ByteArrayInputStream]))

(def ^:private default-sample-size
  "The default sample size to use."
  1000)

(defmulti bytes->string
  "Converts a byte array to a string depending on the type of parser."
  (fn [converter sample] converter))

(defmethod bytes->string :gzip
  [converter bytes]
  (try
    (let [bis   (ByteArrayInputStream. bytes)
          gis   (GZIPInputStream. bis)
          limit (count bytes)
          buf   (byte-array limit)
          len   (.read gis buf 0 limit)]
      (if (= len -1)
        ""
        (string/join " " (map str buf))))
    (catch Exception e "")))

(defmethod bytes->string :hex
  [converter bytes]
  (reduce (fn [st b] (string/join [st (format "%02x" b)])) "" bytes))

(defmethod bytes->string nil
  [converter bytes]
  (String. bytes))

(defn sip
  "Reads in 'limit' number of bytes from input-stream. Returns a byte-array."
  [input-stream limit]
  (with-open [r input-stream]
    (let [buf (byte-array limit)
          len (.read r buf 0 limit)]
      (if (= len -1)  ; EOF
        ""
        (byte-array (take len buf))))))

(defn format-matches
  "Determines whether a format matches a sample from a file."
  [sample [_ identifier-fn converter]]
  (let [sample-str    (bytes->string converter sample)]
    (not (insta/failure? (identifier-fn sample-str)))))

(def formats (load-parsers))

(defn identify-sample
  "Attempts to identify the type of a sample."
  [sample]
  (ffirst (filter (partial format-matches sample) formats)))

(defn identify
  "Attempts to identify the type of a sample obtained from anything that clojure.java.io/reader
   can convert to a reader."
  ([in]
     (identify in default-sample-size))
  ([in sample-size]
     (identify-sample (sip in sample-size))))

(defn parse-args
  "Parses the command-line arguments."
  [args]
  (parse-opts args
       [["-l" "--list" "List recognized file types." :id :list :default false]
        ["-s" "--sample-size N" "Specify the size of the sample." :id :sample-size :parse-fn #(Integer. %)
        :default 1000]
        ["-c" "--csv FILE" "Specify a CSV file to use as input. The output file will be the columns of this file plus one at the end for the identified type, or 'unknown' if unknown." :id :csv :default "" :validate [#(.exists (as-file %)) "Input CSV file must exist"]]
        ["-n" "--column-number N" "Which column of the CSV to use as the input path." :id :column-number :default 4 :parse-fn #(Integer. %)]
        ["-o" "--output FILE" "Specify an output file (for use with CSV mode). Defaults to 'heuristomancer.csv', and errors if the file already exists, unless the default is used. (Does NOT validate the existence or nonexistence of the default output file, use caution.)" :id :output :default "heuristomancer.csv" :validate [#(not (.exists (as-file %))) "Output file must not already exist"]]
        ["-h" "-?" "--help" "Show help." :id :help :default false]]))

(defn list-formats
  "Lists all of the formats currently recognized by this utility."
  []
  (dorun (map (comp println name first) formats)))

(defn supported-formats
  "Returns a list of the formats currently recognized by heuristomancer. Returns the list
   in the order of evaluation when iterating over the parsers when attempting to id a file."
  []
  (mapv (comp name first) formats))

(defn- print-type
  [path t]
  (if (nil? t)
      (println path "- UNRECOGNIZED")
      (println path "-" (name t))))

(defn show-file-type
  "Shows the type of a single file or 'UNRECOGNIZED' if the file type can't be identified."
  [sample-size path]
  (let [type (identify (input-stream path) sample-size)]
    (print-type path type)))

(defn show-file-types
  "Shows the types of a sequence of files."
  [sample-size paths]
  (dorun (map (partial show-file-type sample-size) paths)))

(defn identify-one-row
  "Pulls the path out of `column-number` of `csv-data`, and then identifies
  with `sample-size` and returns the CSV data with the type added as an
  additional column (using 'unknown' if no type was detected). Returns nil if
  the file does not exist."
  [column-number sample-size csv-data]
  (let [path (nth csv-data (- column-number 1))]
    (if (.exists (as-file path))
      (let [type (identify (input-stream path) sample-size)]
        (print-type path type)
        (conj csv-data (if (nil? type) "unknown" (name type))))
      (do
        (println path "- DOES NOT EXIST")
        nil))))

(defn process-csv
  [sample-size column-number csv-path output-path]
  (println "Processing csv" csv-path "to" output-path)
  (with-open [csv-reader (reader csv-path)
              csv-writer (writer output-path)]
    (->> (csv/read-csv csv-reader)
         (map #(identify-one-row column-number sample-size %))
         (remove nil?)
         (csv/write-csv csv-writer))))

(defn -main
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-args args)]
    (if errors (println (string/join "\n" errors))
     (cond
      (:help options)                      (println summary)
      (:list options)                      (list-formats)
      (not (string/blank? (:csv options))) (process-csv (:sample-size options) (:column-number options) (:csv options) (:output options))
      :else                                (show-file-types (:sample-size options) arguments)))))
