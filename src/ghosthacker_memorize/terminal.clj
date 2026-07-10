(ns ghosthacker-memorize.terminal
  "GHOST HACKER: メモライズ -- minimal terminal host adapter (playable
  prototype).

  Like TUNING, there is no real-time pressure to track here -- no
  `future`/agent thread pool, no wall-clock judging. Just a plain
  read-line-driven loop: each turn reads two card indices at once
  (space-separated, e.g. \"0 5\"), applies core/flip-pair once, and
  prints the grid with collected cards shown face-up (their Ghost name)
  and everything else hidden behind \"?\". `q` or EOF cuts the run off
  with the state gathered so far, same posture as TUNING's play-loop!.

  Run: clojure -M -m ghosthacker-memorize.terminal"
  (:require [clojure.string :as str]
            [ghosthacker-memorize.core :as core]
            [ghosthacker-memorize.deck :as deck]))

(defn- card-glyph
  "collected済みならGhost名、そうでなければ伏せ札の記号\"?\"を返す。"
  [state grid i]
  (if (contains? (:collected state) i)
    (name (nth grid i))
    "?"))

(defn- print-grid! [state grid]
  (println)
  (dotimes [i (count grid)]
    (println (format "  [%d] %s" i (card-glyph state grid i))))
  (println))

(defn- read-pick! []
  (print "flip two indices \"i j\" (q to quit) > ") (flush)
  (some-> (read-line) str/trim))

(defn- parse-pick
  "\"i j\" 形式の入力を[i j](両方とも整数)にパースする。要素数不一致や
   パース不能ならnil。"
  [line]
  (when line
    (let [parts (remove str/blank? (str/split line #"\s+"))]
      (when (= 2 (count parts))
        (try
          (mapv #(Integer/parseInt %) parts)
          (catch NumberFormatException _ nil))))))

(defn- pick-cards!
  "1手番ぶんの入力ループ。有効な2インデックスが得られたら[i j]を返す。
   q/EOFで打ち切り(nilを返す)。不正な入力は読み飛ばして再入力を促す。"
  [state grid]
  (loop []
    (print-grid! state grid)
    (let [line (read-pick!)]
      (cond
        (nil? line) nil
        (= (str/lower-case line) "q") nil
        :else
        (let [pick (parse-pick line)]
          (if (nil? pick)
            (do (println "\"i j\" の形式で2つのインデックスを空白区切りで入力してください。")
                (recur))
            pick))))))

(defn- play-loop!
  "全ペアを収集するまでpick-cards!→flip-pairを繰り返す。途中でnil(打ち切り)
   が返ったら、そこまでのstateで終える。"
  [grid]
  (loop [state core/initial-state]
    (if (core/complete? state grid)
      state
      (let [pick (pick-cards! state grid)]
        (if (nil? pick)
          state
          (let [[i j] pick
                next-state (core/flip-pair state grid i j)]
            (println (cond
                       (= (:attempts next-state) (:attempts state))
                       "-> invalid pick (same card / already collected), try again"

                       (> (count (:collected next-state)) (count (:collected state)))
                       "-> MATCH!"

                       :else "-> no match"))
            (recur next-state)))))))

(defn -main
  "Entry point for `clojure -M -m ghosthacker-memorize.terminal`."
  [& _args]
  (println "GHOST HACKER: メモライズ — first-case-file")
  (println "2枚のインデックスを空白区切りで入力し、同じGhostのペアを揃えてください。")
  (let [state (play-loop! deck/first-case-file)
        result (core/summary state deck/first-case-file)]
    (println)
    (println "=== RESULT ===")
    (println (format "grade=%s attempts=%d pairs=%d/%d complete=%s"
                      (name (:grade result))
                      (:attempts result)
                      (:pairs-collected result)
                      (:total-pairs result)
                      (:complete? result)))))
