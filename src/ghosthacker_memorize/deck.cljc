(ns ghosthacker-memorize.deck
  "GHOST HACKER: メモライズ -- sample card grid (ADR-2607023200, portfolio
  title #7).

  \"first-case-file\": Neiの手元に残った最初の4件の事件の記憶(Ghost)を、
  8枚(4ペア)の固定配置カードグリッドにしたもの。TUNINGのquiet-static
  同様、シャッフルはこの層の責務ではない(実プレイでランダム化したい
  場合はホストアダプタ側で行う) -- ここでは決定的な配置のままにして
  test/terminalの両方が同じpick列で再現可能に動くようにしてある。"
  (:require [ghosthacker-memorize.core :as core]))

(def first-case-file
  "8 cards / 4 pairs, deterministic layout (no shuffle).
   index: 0 phishing-mail  / 1 stolen-password / 2 dark-job-ad /
          3 account-takeover / 4 stolen-password / 5 phishing-mail /
          6 account-takeover / 7 dark-job-ad
   pairs: (0,5) phishing-mail, (1,4) stolen-password,
          (2,7) dark-job-ad, (3,6) account-takeover."
  [:phishing-mail :stolen-password :dark-job-ad :account-takeover
   :stolen-password :phishing-mail :account-takeover :dark-job-ad])

(defn play-first-case-file
  "first-case-fileをpicksで再生し、summaryを返す(core/play-summaryの
   薄いラッパー)。"
  [picks]
  (core/play-summary first-case-file picks))
