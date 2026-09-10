(ns ghosthacker-memorize.terminal-test
  "-mainそのものはテストせず、private var経由でparse-pick/pick-cards!/
   play-loop!を直接叩く。実プロセスとしての-main自体は手動検証済み
   （完走/q途中打ち切り/不正入力の再入力要求、いずれも正しく完了し
   プロセスがハングしないことを確認）。"
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker-memorize.core :as core]
            [ghosthacker-memorize.deck :as deck]
            [ghosthacker-memorize.terminal :as terminal]))

(def ^:private parse-pick #'terminal/parse-pick)
(def ^:private pick-cards! #'terminal/pick-cards!)
(def ^:private play-loop! #'terminal/play-loop!)

(defn- silently [thunk]
  (let [result (atom nil)]
    (with-out-str (reset! result (thunk)))
    @result))

(deftest parse-pick-boundary-test
  (testing "\"i j\"形式を[i j](整数)にパースする"
    (is (= [0 5] (parse-pick "0 5")))
    (is (= [1 4] (parse-pick "1  4")))) ; 複数空白も許容
  (testing "不正な入力はnil"
    (is (nil? (parse-pick "0")))
    (is (nil? (parse-pick "0 1 2")))
    (is (nil? (parse-pick "a b")))
    (is (nil? (parse-pick "")))
    (is (nil? (parse-pick nil)))))

(deftest pick-cards-valid-and-quit-test
  (testing "有効な\"i j\"が入力されたら[i j]を返す"
    (is (= [0 5] (silently #(with-in-str "0 5\n"
                              (pick-cards! core/initial-state deck/first-case-file))))))
  (testing "不正な入力は読み飛ばし、次の有効な入力を処理する"
    (is (= [1 4] (silently #(with-in-str "xyz\n1 4\n"
                              (pick-cards! core/initial-state deck/first-case-file))))))
  (testing "qまたはEOFで打ち切り(nil)"
    (is (nil? (silently #(with-in-str "q\n"
                           (pick-cards! core/initial-state deck/first-case-file)))))
    (is (nil? (silently #(with-in-str ""
                           (pick-cards! core/initial-state deck/first-case-file)))))))

(deftest play-loop-full-completion-test
  (testing "全ペアを正しく指定し切れば完走する"
    (let [state (silently
                 #(with-in-str "0 5\n1 4\n2 7\n3 6\n"
                    (play-loop! deck/first-case-file)))]
      (is (core/complete? state deck/first-case-file))
      (is (= 4 (:attempts state)))
      (is (= 4 (core/pairs-collected state))))))

(deftest play-loop-eof-and-quit-boundary-test
  (testing "qで途中打ち切り"
    (let [state (silently #(with-in-str "0 5\nq\n" (play-loop! deck/first-case-file)))]
      (is (not (core/complete? state deck/first-case-file)))
      (is (= 1 (:attempts state)))))
  (testing "EOFでも同様に打ち切り"
    (let [state (silently #(with-in-str "0 5\n" (play-loop! deck/first-case-file)))]
      (is (not (core/complete? state deck/first-case-file)))
      (is (= 1 (:attempts state))))))

(deftest play-loop-invalid-pick-reprompt-test
  (testing "不正な入力(要素数不一致/パース不能)は読み飛ばして再入力を促し、その後の完走を妨げない"
    (let [state (silently #(with-in-str "xyz\n0 5\n1 4\n2 7\n3 6\n"
                             (play-loop! deck/first-case-file)))]
      (is (core/complete? state deck/first-case-file))
      (is (= 4 (:attempts state))))))

(deftest play-loop-no-match-does-not-collect-test
  (testing "外れの手番(0と1)はattemptsだけ進みcollectedは変わらないまま継続する"
    (let [state (silently #(with-in-str "0 1\nq\n" (play-loop! deck/first-case-file)))]
      (is (= 1 (:attempts state)))
      (is (= 0 (core/pairs-collected state))))))
