(ns ghosthacker-memorize.web
  "GHOST HACKER: メモライズ -- browser host adapter (ADR-2607023200,
  portfolio title #7). Plain reagent, no Web Audio -- like ECHOES/TUNING,
  MEMORIZE has no real-time judgment to drive, just clicks.

  The pure core (ghosthacker-memorize.core) only knows about \"flip these
  two indices at once\" (flip-pair); tracking which card was clicked
  *first* within the current turn is a UI-only concern that lives here
  (:first-pick), not in core -- the moment a second, distinct, not-yet-
  collected card is clicked, both indices are handed to core/flip-pair
  together and :first-pick is cleared, whether or not the guess matched."
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [ghosthacker-memorize.core :as core]
            [ghosthacker-memorize.deck :as deck]))

(defn- fresh-state []
  {:phase :playing        ; :playing | :result
   :grid deck/first-case-file
   :game-state core/initial-state
   :first-pick nil
   :last-result nil})     ; nil | :match | :no-match

(defonce state (r/atom (fresh-state)))

(defn- pick-card! [i]
  (let [{:keys [phase grid game-state first-pick]} @state]
    (when (and (= phase :playing)
               (not (contains? (:collected game-state) i)))
      (cond
        (nil? first-pick)
        (swap! state assoc :first-pick i :last-result nil)

        (= first-pick i)
        nil ; re-clicking the same card is a no-op, not a turn

        :else
        (let [next-gs (core/flip-pair game-state grid first-pick i)
              matched? (> (count (:collected next-gs)) (count (:collected game-state)))]
          (swap! state assoc
                 :game-state next-gs
                 :first-pick nil
                 :last-result (if matched? :match :no-match)
                 :phase (if (core/complete? next-gs grid) :result :playing)))))))

(defn- restart! []
  (reset! state (fresh-state)))

(defn- card-label [grid game-state first-pick i]
  (cond
    (contains? (:collected game-state) i) (name (nth grid i))
    (= first-pick i) "?!"
    :else "?"))

(defn- playing-screen []
  (let [{:keys [grid game-state first-pick last-result]} @state]
    [:div.memorize-app
     [:h1 "GHOST HACKER: メモライズ"]
     [:p.memorize-sub "first-case-file"]
     [:div.memorize-grid
      (doall
       (for [i (range (count grid))]
         ^{:key i}
         [:button.memorize-card
          {:class (when (contains? (:collected game-state) i) "collected")
           :disabled (contains? (:collected game-state) i)
           :on-click #(pick-card! i)}
          (card-label grid game-state first-pick i)]))]
     (when last-result
       [:div.memorize-result (case last-result :match "MATCH!" :no-match "no match" "")])
     [:p.memorize-hint (str (core/pairs-collected game-state) "/" (core/total-pairs grid)
                            " pairs collected — attempts " (:attempts game-state))]]))

(defn- result-screen []
  (let [{:keys [grid game-state]} @state
        summary (core/summary game-state grid)]
    [:div.memorize-app
     [:h1 "GHOST HACKER: メモライズ"]
     [:h2 (str "grade: " (name (:grade summary)))]
     [:p (str "attempts " (:attempts summary)
              " / pairs " (:pairs-collected summary) " of " (:total-pairs summary))]
     [:button.memorize-restart {:on-click restart!} "もう一度"]]))

(defn app []
  (case (:phase @state)
    :result [result-screen]
    [playing-screen]))

(defn ^:export mount []
  (when-let [el (.getElementById js/document "app")]
    (rdom/render [app] el)))

(defn ^:export init [] (mount))
