(ns reddit-digest.core-test
  (:require [clojure.test :refer :all]
            [reddit-digest.core :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(deftest test-clean-post-data
  (let [response-data (json/read-str (slurp (io/file (io/resource "response.json"))) :key-fn keyword)
        posts-data (map :data (-> response-data :data :children))
        cleaned-posts-data (map clean-post-data posts-data)
        top-post (first cleaned-posts-data)
        expected {:title        "Rust Design Patterns now also as a book"
                  :score        765
                  :comments     59
                  :url          "https://old.reddit.com/r/rust/comments/kowtqn/rust_design_patterns_now_also_as_a_book/"
                  :comment-link "https://old.reddit.com/r/rust/comments/kowtqn/rust_design_patterns_now_also_as_a_book/"}]
    (testing "clean-post-data extracts and formats data from API response"
      (is (= top-post expected)))))

