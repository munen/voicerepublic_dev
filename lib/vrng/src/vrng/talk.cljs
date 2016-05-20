(ns vrng.talk
  (:require
   [vrng.util :as u :refer [t state]]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :include-macros true]
   goog.string.format
   [clojure.string :as str]
   [ajax.core :refer [POST]]
   cljsjs.moment
   cljsjs.moment.locale.de))

(.locale js/moment "en") ;; or "de", depending on user settings


(defonce page-state (atom {}))

(defn starts-at []
  (:starts_at (:talk @state)))

;; -------------------------
;; Utils

(defn time-to-start []
  (- (u/millis (starts-at)) (* 1000 (:now @state))))

;; ---------------
;; todo: change this to "state message??"
;; so that it can display the countdown and then the state (live, processing, etc)?
(defn format-countdown [millis]
  "Returns a textual respresentation of the countdown."
  (let [[days hours minutes seconds] (u/millis-to-dhms millis)]
    (cond
      (< millis 0) (t "any_time_now")
      (> hours 48) (str days " " (t "days"))
      :else (goog.string.format "in %02dh %02dm %02ds" hours minutes seconds))))

;; -------------------------
;; Helpers

(defn channel []
  (:channel (:talk @state)))

(defn started-at []
  (:started_at (:talk @state)))

(defn now []
  (:now @state))

(defn stream-url []
  (:stream_url (:venue (:talk @state))))

(defn title []
  (:title (:talk @state)))

(defn media-links []
  (:media_links (:talk @state)))

(defn talk-state []
  (:state (:talk @state)))

(defn create-comment-url []
  (:create_message_url (:talk @state)))

(defn talk-speakers []
  (:speakers (:talk @state)))

(defn teaser []
  (:teaser (:talk @state)))

(defn description []
  (:description (:talk @state)))

(defn play-count []
  (:play_count (:talk @state)))

(defn series-title []
  (:title (:series (:talk @state))))

(defn series-url []
  (:url (:series (:talk @state))))

(defn user-name []
  (:name (:user (:venue (:talk @state)))))

(defn user-url []
  (:url (:user (:venue (:talk @state)))))

(defn user-image-url []
  (:image_url (:user (:venue (:talk @state)))))

(defn image-url []
  (:image_url (:talk @state)))

(defn comments []
  (sort-by :created_at > (:messages (:talk @state))))

(defn pin-id []
  (:pin-id (:talk @state)))

(defn talk-state-label []
  (cond
    (= (talk-state) "prelive") (str "Starting " (format-countdown (time-to-start)))
    :else (t (str "state." (talk-state)))))


;; -------------------------
;; CSRF Token

(defn content-by-name [name]
  (aget (.getElementsByName js/document name) 0 "content"))

(defn value-by-name [name]
  (aget (.getElementsByName js/document name) 0 "value"))

(defn set-value-by-name [name value]
  (let [element (aget (.getElementsByName js/document name) 0)]
    (set! (.-value element) value)))

(defn csrf-token []
  (content-by-name "csrf-token"))

;; -------------------------
;; Actions

(defn post-comment-action []
  (let [url (create-comment-url)
        content (value-by-name "content")]
    (POST url {:headers {"X-CSRF-Token" (csrf-token)}
               :format :json
               :params {:message {:content content}}})
    (set-value-by-name "content" "")))

(defn cancel-comment-action []
  (set-value-by-name "content" ""))

;; -------------------------
;; Player
;;
;; http://jplayer.org/latest/developer-guide/

(defn player []
  (js/jQuery "#player"))

(defn player-data []
  (.data (player) "jPlayer"))

(defn player-paused? []
  (.. (player-data) -status -paused))

(defn load-stream [media]
  (if-not (= (:media @page-state) media)
    (do
      (swap! page-state assoc :media media)
      (print "start playing" media)
      (.jPlayer (player) "setMedia" (clj->js media)))))

(defn start-stream []
  (.jPlayer (player) "play"))

(defn stop-stream []
  (if-not (player-paused?)
    (do
      (print "stop playing" (stream-url))
      (.jPlayer (player) "stop"))))

(defn control-stream []
  (cond
    (= (talk-state) "live") (do
                              (load-stream {:mp3 (stream-url)})
                              (start-stream))
    (= (talk-state) "archived") (load-stream (media-links))
    :else (stop-stream)))

(defn player-ready []
  (swap! page-state assoc :ready true)
  (control-stream))

(defn jplayer-config []
  #js {;; --- configuration
       :cssSelectorAncestor ".player-container"
       :verticalVolume true
       ;; --- event callbacks
       :ready player-ready
       :error #(print "ERROR" (.. % -jPlayer -error -message))
       :warning #(print "WARNING" (.. % -jPlayer -warning -message))
       :suspend #(print "suspend")
       :play #(print "play")
       :pause #(print "pause")
       :loadeddata #(print "loadeddata")
       :waiting #(print "waiting")
       :playing #(print "playing")
       :stalled #(print "stalled")})

(defn setup-jplayer []
  (let [element (.getElementById js/document "player")]
    (.jPlayer (js/jQuery element) (jplayer-config))))

;; -------------------------
;; Components

(defn countdown-comp []
  [:p.label.state-msg (format-countdown (time-to-start))])

(defn audio-comp []
  [:audio { :auto-play "autoplay" }
   [:source { :src (stream-url) :data-x 1 }]])

(defn debug-comp []
  [:table
   [:tr
    [:td "channel"]
    [:td (channel)]]
   [:tr
    [:td "starts-at"]
    [:td (starts-at)]]
   [:tr
    [:td "started-at"]
    [:td (started-at)]]
   [:tr
    [:td "now"]
    [:td (now)]]
   [:tr
    [:td "stream-url"]
    [:td (stream-url)]]
   [:tr
    [:td "series-title"]
    [:td (series-title)]]
   [:tr
    [:td "series-url"]
    [:td (series-url)]]
   [:tr
    [:td "pin-id"]
    [:td (pin-id)]]
   [:tr
    [:td "user-name"]
    [:td (user-name)]]
   [:tr
    [:td "user-url"]
    [:td (user-url)]]
   [:tr
    [:td "image-url"]
    [:td (image-url)]]
   [:tr
    [:td "title"]
    [:td (title)]]
   [:tr
    [:td "comments"]
    [:td (str (comments))]]
   ])


(defn title-block-comp []
  [:div.row.collapse.talk-title
   [:div
    [:p.label.state-msg {:class (talk-state)} (talk-state-label)]]
   [:h1 (title)]])

(defn talk-info-comp []
  [:div {:class "row collapse"}
   [:div {:class "medium-7 columns"}
    [:p {:class "lead teaser"} (teaser)
      (if-not (str/blank? (talk-speakers))
      [:p {:class "speakers"} "Speakers: "
       [:span.names (talk-speakers)]])]]
   [:div {:class "medium-5 columns"}
    [:div {:class "media-object float-right"}
     [:div {:class "media-object-section pub-meta text-right middle"}
      [:p {:class "meta-publisher"}
       [:a {:href (user-url)} (user-name)]]
      [:p {:class "meta-series"} "From the Series: "
       [:a {:href (series-url)} (series-title)]]
      [:p {:class "meta-info"}
       [:span (.fromNow (js/moment (starts-at)))]
       (if-not (= (talk-state) "prelive") [:span " • " (play-count) " Plays"])]]
     [:div {:class "media-object-section"}
      [:div {:class "img-circle"}
       [:a {:href (user-url)}
        [:img.avatar-image {:alt "(user-name)" :height "60",
                            :src (user-image-url) :width "60"}]]]]]]])

(defn player-media-comp []
  [:div#player {:style {:width "0px" :height "0px"}}
   [:img {:id "jp_poster_0", :style { :width "0px" :height "0px" :display "none" }}]
   [:audio {:id "jp_audio_0", :preload "none"}]])

(defn player-controls-comp []
  [:div {:class "player-container"}
   [:div {:class "row collapse", :id "talk-playbar"}
    [:button {:class "button button-playbar button-play-pause state-play"
              :on-click start-stream}
     [:span {:class "jp-play"}
      [:svg {:dangerouslySetInnerHTML {:__html (str "<use xlink:href='#icon-play' />")}}]]
     [:span {:class "jp-pause icon-uniE6B8 hide", :style { :display "none"}}]]
    [:div {:class "jp-volume-bar", :id "meter"}
     [:div {:class "jp-volume-bar-value", :id "indicator", :style { :height "80%"}}]]
    [:span {:class "timecode jp-current-time"} "00:00"]
    [:div {:class "timeline jp-seek-bar", :style {:width  "0%" }}
     [:div {:class "track-playing jp-play-bar", :style { :width "0%"}}]]
    [:span {:class "timecode right jp-duration"} "00:00"]
    [:button {:class "button button-playbar button-volume right hide-for-small-only"}
     [:span {:class "icon-uniE6BE"}]
     [:span {:class "icon-uniE6C1 hide"}]]
    [:div {:class "playbar-divider"}]
    [:button {:data-selector "tooltip-imm27bsv0", :title "", :data-yeti-box "tooltip-imm27bsv0", :data-resize "tooltip-imm27bsv0", :data-options "disable_for_touch:true", :id "view-slides2", :class "has-tip tip-top tip-center button button-playbar button-volume right hide-for-small-only", :data-tooltip "nz95zk-tooltip", :aria-describedby "tooltip-imm27bsv0", :aria-haspopup "true", :data-toggle "tooltip-imm27bsv0"} ]]])


(defn talk-image-comp []
  [:div {:class "row collapse"}
   [:div {:class "talk-picture"}
    [:img {:class "image" :src (image-url) :alt "" }]
    [:button {:class "button button-playbar button-volume right show-for-small-only", :id "view-slides", :title "View Slides"}
     [:svg
      [:use {:xmlns:xlink "http://www.w3.org/1999/xlink", :xlink:href "#icon-slides"}]]]]])

(defn open-modal-action [action-name]
  #(swap! page-state assoc action-name true))

(defn close-modal-action [action-name]
  #(swap! page-state assoc action-name false))

(defn action-button-comp [action-name title classes]
  [:li { :class classes }; --- pin
   [:a {:class "button small" :aria-haspopup "true", :tabindex "0"
        :on-click (open-modal-action action-name)}
    [:svg
     [:use {:xmlns:xlink "http://www.w3.org/1999/xlink",
            :xlink:href (str "#icon-" action-name)}]]
    [:span {:class "action-name"} title]]])

(defn talk-actions-comp []
  [:div {:class "float-right medium-4 large-3 columns", :id "talk-actions"}
   [:ul {:class "button-group float-right"}
    [action-button-comp :pin "Pin" ""]
    [action-button-comp :share "Share" ""]
    [:li ; --- comments
     [:a {:class "button small", :href "#comment-container"}
      [:svg
       [:use {:xmlns:xlink "http://www.w3.org/1999/xlink", :xlink:href "#icon-chat"}]]
      [:span {:class "comment-count show-for-small-only"} "3"]
      [:span {:class "action-name"} "Comments"]]]
    [action-button-comp :embed "Embed" "hide-for-small-only"]
    [action-button-comp :podcast "Podcast" "hide-for-small-only"]]])

(defn talk-description-comp []
  [:div {:class "left medium-8 large-9 columns", :id "talk-description"}
   [:p (description)]])

(defn comment-comp [comment]
  ^{:key (:id comment)}
  [:div {:class "row collapse comment-block"}
   [:div {:class "comment-meta small-3 columns"}
    [:p
     [:img.comment-avatar {:src (:user_image_url comment) :alt "Dude"}]]
    [:p {:class "post-author"} (:user_name comment)]
    [:p {:class "post-time" :title (:created_at comment)}
     (.fromNow (js/moment (:created_at comment)))]]
   [:div {:class "comment-content small-8 small-offset-1 medium-9 medium-offset-0 columns"}
    [:p {:class "meta-info"} (:content comment)]]]
  )

(defn comments-login-comp []
  [:div.clearfix
    [:p "Please "
      [:a {:href js/signInPath} "sign in"]
      " to make a comment."]]
  )

(defn comments-form-comp []
  [:div
    [:textarea.panel {:name "content"
                      :placeholder "Share your thoughts here."}]
    [:button {:class "button float-right small secondary btn-hover-blue" :on-click post-comment-action } "Post"]
    [:button {:class "button float-right cancel small hollow btn-hover-red btn-gray" :on-click cancel-comment-action } "Cancel"]]
  )

(defn comments-comp []
  [:div.clearfix {:id "comment-container"}
   [:div {:class "comment small-12 columns float-center"}
    [:h3
     [:svg {:dangerouslySetInnerHTML {:__html (str "<use xlink:href='#icon-chat' />")}}]
     " Comments"]
     (if js/signedIn [comments-form-comp] [comments-login-comp])]
   [:div {:class "small-12 columns previous-comments"}
    (doall (map comment-comp (comments)))]])

(defn modal-comp [action-name title content-comp]
  [:div
   [:div {:id "share-modal" :role "dialog"}
    [:div {:class "row collapse"}
     [:h2 {:id "modalTitle"} title]]
    [content-comp]
    [:button {:aria-label "Close modal", :class "close-button", :type "button"  :on-click (close-modal-action action-name) }
     [:span {:aria-hidden "true"} "×"]]]])

;; TODO fix embed code
(defn modal-embed-comp []
  [:div
   [:p {:class "lead"} "Copy and paste the following code into your web site:"]
   [:textarea {:rows "3"} "&lt;iframe width=&quot;445&quot; height=&quot;140&quot; src=&quot;http://localhost:3000/embed/talks/games-net-presents-howard-phillips&quot; frameborder=&quot;0&quot; scrolling=&quot;no&quot; allowfullscreen&gt;&lt;/iframe&gt;"]])

;; TODO make pin work
(defn modal-pin-comp []
  [:div
   [:button {:class "button small"} "Sign In"]])

(defn modal-podcast-comp []
  [:div
   [:p {:class "lead"}
    [:button {:class "button hollow btn-hover-yellow"}
     [:span {:class "translation_missing", :title "translation missing: en.talks.show.launch"} "Launch"]]]])

;; TODO fix link to flyer
(defn modal-share-comp []
  [:div
   [:div {:class "small-6 columns"}
    [:ul {:class "menu vertical"}
     [:li
      [:a {:class "button small btn-hover-yellow"}
       [:svg
        [:use {:xmlns:xlink "http://www.w3.org/1999/xlink", :xlink:href "#icon-facebook"}]]
       [:span {:class "translation_missing", :title "translation missing: en.talks.show.facebook"} "Facebook"]]]
     [:li
      [:a {:class "button small btn-hover-yellow"}
       [:svg
        [:use {:xmlns:xlink "http://www.w3.org/1999/xlink", :xlink:href "#icon-twitter"}]]
       [:span {:class "translation_missing", :title "translation missing: en.talks.show.twitter"} "Twitter"]]]
     [:li
      [:a {:class "button small btn-hover-yellow"}
       [:svg
        [:use {:xmlns:xlink "http://www.w3.org/1999/xlink", :xlink:href "#icon-email"}]]
       [:span {:class "translation_missing", :title "translation missing: en.talks.show.mail"} "Mail"]]]]]
   [:div {:class "small-6 columns"}
    [:img {:class "flyer", :src "/system/flyer/3378.png", :alt "3378"}]]])

(defn modals-comp []
  [:div
   (if (true? (:pin @page-state))
     [modal-comp :pin "Pin this Talk" modal-pin-comp])
   (if (true? (:share @page-state))
     [modal-comp :share "Share this Talk" modal-share-comp])
   (if (true? (:embed @page-state))
     [modal-comp :embed "Embed this Talk" modal-embed-comp])
   (if (true? (:podcast @page-state))
     [modal-comp :podcast "Podcast this Talk" modal-podcast-comp])])

(defn root-comp []
  ;;[debug-comp])
  (.onTalkPageReady js/window)
  (fn [] [:div.left-side.medium-9.columns
   [:div.top-container
    [title-block-comp]
    [talk-info-comp]
    [player-media-comp]
    [player-controls-comp]
    [talk-image-comp]
    [:div.row.collapse
     [talk-actions-comp]
     [talk-description-comp]]]
   [:div.bottom-container.row.collapse.clearfix
    [comments-comp]]
   [modals-comp]]))


;; -------------------------
;; Initialize & Messageing

(defn add-new-comment [message]
  (let [comment (:message message)]
    (swap! state assoc-in [:talk :messages]
           (concat (comments) [comment]))
    ;; FIXME does not trigger update, what's wrong?
    (print "new comment" comment)
    (print "comments now:" (comments))))

(defn message-handler [message]
  (condp = (message :event)
    "message" (add-new-comment message)
    "snapshot" (do
                 (u/update-snapshot state message)
                 (print "State now: " (talk-state))
                 (control-stream))))

(defn mount-root []
  (reagent/render [root-comp] (.getElementById js/document "app")))

(defn init! []
  (js/setTimeout setup-jplayer 1000)
  (u/start-timer state)
  (u/setup-faye (channel) message-handler)
  ;;(js/setTimeout #(start-stream) 1000) ;; move start-stream to message-handler
  (mount-root))
