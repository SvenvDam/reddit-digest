(ns reddit-digest.core
  (:require [clj-http.client :as client]
            [cheshire.core]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [clojure.string :as str])
  (:import (java.time LocalDate)
           (com.sendgrid SendGrid Request Method))
  (:gen-class
    :methods [^:static [handler [] Void]]))

(defn env-vec [key]
  "Utility to parse comma-separated env vars"
  (str/split (env key) #","))

;; constants
(def user-agent "digest:v0.1 (by /u/SvenvDam)")
(def reddit-base-url "https://old.reddit.com")
(def subreddit-path "/r/")
(defn reddit-query [n] (str "/top/.json?t=week&limit=" n))
(def sender "reddit-digest@svenvandam.com")
(def title "Reddit Digest")

(defn fetch-top-posts [subreddit n]
  (let [response (client/get
                   (str reddit-base-url subreddit-path subreddit (reddit-query n))
                   {:as      :json
                    :headers {"User-Agent" user-agent}})
        posts (-> response :body :data :children)]
    (map :data posts)))

(defn clean-post-data [raw-post-data]
  {:title        (:title raw-post-data)
   :score        (:score raw-post-data)
   :comments     (:num_comments raw-post-data)
   :url          (:url raw-post-data)
   :comment-link (str reddit-base-url (:permalink raw-post-data))})

(defn fetch-subreddit-data [subreddit]
  (let [top-posts (fetch-top-posts subreddit 10)
        cleaned-top-posts (map clean-post-data top-posts)]
    {:subreddit subreddit
     :posts     cleaned-top-posts}))

(defn fetch-all-reddit-data [subreddits]
  (let [subreddit-data-futures (map #(future (fetch-subreddit-data %)) subreddits)]
    (map deref subreddit-data-futures)))

(defn get-mail-data [subreddits]
  (let [subreddit-data (fetch-all-reddit-data subreddits)]
    {:title      title
     :date       (str (LocalDate/now))
     :subreddits subreddit-data}))

(defn email [address]
  {:email address})

(defn get-sendgrid-payload [receivers content-data template-id]
  {:from             (email sender)
   :template_id      template-id
   :personalizations [{:to                    (map email receivers)
                       :dynamic_template_data content-data}]})

(defn send-email [payload api-key]
  (let [sendgrid (SendGrid. api-key)
        request (Request.)]
    (.setMethod request Method/POST)
    (.setEndpoint request "mail/send")
    (.setBody request (json/write-str payload))
    (.api sendgrid request)))

(defn -handler []
  (let [sendgrid-api-key (env :sendgrid-api-key)
        sendgrid-template-id (env :sendgrid-template-id)
        subreddits (env-vec :subreddits)
        receivers (env-vec :receivers)
        mail-data (get-mail-data subreddits)
        mail-payload (get-sendgrid-payload receivers mail-data sendgrid-template-id)
        response (send-email mail-payload sendgrid-api-key)]
    (println (.getStatusCode response))
    (println (.getBody response))))
