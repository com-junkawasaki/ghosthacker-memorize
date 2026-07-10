(ns ghosthacker-memorize.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker-memorize.core :as core]))

(def ^:private grid
  ;; index: 0 a / 1 b / 2 a / 3 c / 4 b / 5 c
  ;; pairs: (0,2) a, (1,4) b, (3,5) c
  [:a :b :a :c :b :c])

(deftest flip-pair-match-collects-both
  (testing "同じGhostの2枚をflipするとattemptsが増え、両indexがcollectedになる"
    (let [s1 (core/flip-pair core/initial-state grid 0 2)]
      (is (= 1 (:attempts s1)))
      (is (= #{0 2} (:collected s1))))))

(deftest flip-pair-no-match-only-bumps-attempts
  (testing "違うGhostの2枚はattemptsだけ増え、collectedは変わらない"
    (let [s1 (core/flip-pair core/initial-state grid 0 1)]
      (is (= 1 (:attempts s1)))
      (is (= #{} (:collected s1))))))

(deftest flip-pair-same-index-is-noop
  (testing "同じindexを2回指定した手番はstateを変えない(attemptsも消費しない)"
    (is (= core/initial-state (core/flip-pair core/initial-state grid 0 0)))))

(deftest flip-pair-already-collected-is-noop
  (testing "既にcollected済みのindexを含む手番はstateを変えない"
    (let [s1 (core/flip-pair core/initial-state grid 0 2)
          s2 (core/flip-pair s1 grid 0 1)]
      (is (= s1 s2)))))

(deftest flip-pair-noop-when-complete
  (testing "全ペア収集済みなら以降のflip-pairはstateをそのまま返す"
    (let [done (core/play grid [[0 2] [1 4] [3 5]])]
      (is (core/complete? done grid))
      (is (= done (core/flip-pair done grid 0 2))))))

(deftest total-pairs-and-pairs-collected
  (testing "total-pairsはgridの半分、pairs-collectedはcollectedの半分"
    (is (= 3 (core/total-pairs grid)))
    (let [s1 (core/flip-pair core/initial-state grid 0 2)]
      (is (= 1 (core/pairs-collected s1)))
      (is (= 0 (core/pairs-collected core/initial-state))))))

(deftest complete?-tracks-full-collection
  (testing "全ペア揃うまでcomplete?はfalse、揃うとtrue"
    (is (not (core/complete? core/initial-state grid)))
    (let [s (-> core/initial-state
                (core/flip-pair grid 0 2)
                (core/flip-pair grid 1 4))]
      (is (not (core/complete? s grid)))
      (let [done (core/flip-pair s grid 3 5)]
        (is (core/complete? done grid))))))

(deftest efficiency-and-grade
  (testing "全部一発でマッチ(attempts=total-pairs)ならefficiency=1.0でperfect-memory"
    (let [state (core/play grid [[0 2] [1 4] [3 5]])]
      (is (= 1.0 (core/efficiency state grid)))
      (is (= :perfect-memory (core/grade state grid)))))
  (testing "未プレイ(attempts=0)はefficiency=1.0(未プレイを非効率扱いにしない)"
    (is (= 1.0 (core/efficiency core/initial-state grid))))
  (testing "外れを挟むとefficiencyが下がりgradeも下がる"
    (let [state (core/play grid [[0 1] [0 2] [1 4] [3 5]])] ; 1 miss + 3 matches = 4 attempts / 3 pairs
      (is (core/complete? state grid))
      (is (< (core/efficiency state grid) 1.0))
      (is (not= :perfect-memory (core/grade state grid))))))

(deftest summary-shape
  (testing "summaryはホストアダプタが必要とする全フィールドを返す"
    (let [summary (core/summary (core/play grid [[0 2] [1 4] [3 5]]) grid)]
      (is (= 3 (:attempts summary)))
      (is (= 3 (:pairs-collected summary)))
      (is (= 3 (:total-pairs summary)))
      (is (true? (:complete? summary)))
      (is (= 1.0 (:efficiency summary)))
      (is (= :perfect-memory (:grade summary))))))

(deftest play-and-play-summary-full-completion
  (testing "playは全ペア揃うと終了する"
    (let [state (core/play grid [[0 2] [1 4] [3 5]])]
      (is (core/complete? state grid))
      (is (= #{0 1 2 3 4 5} (:collected state)))))
  (testing "picksが尽きても未完走ならそこまでのstateを返す"
    (let [state (core/play grid [[0 2]])]
      (is (not (core/complete? state grid)))
      (is (= 1 (core/pairs-collected state)))))
  (testing "play-summaryはplay+summaryの合成"
    (let [summary (core/play-summary grid [[0 2] [1 4] [3 5]])]
      (is (:complete? summary))
      (is (= 3 (:attempts summary)))
      (is (= 3 (:pairs-collected summary)))
      (is (= 3 (:total-pairs summary)))
      (is (= :perfect-memory (:grade summary))))))
