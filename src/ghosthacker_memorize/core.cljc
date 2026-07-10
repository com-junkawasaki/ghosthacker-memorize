(ns ghosthacker-memorize.core
  "GHOST HACKER: メモライズ / MEMORIZE -- memory-match card core
  (ADR-2607023200, portfolio title #7, Nei-led).

  Pure, host-free judgment/state engine: past cases (the Ghosts Nei and
  Ren have observed/handled) are turned into cards, and Nei collects them
  by flipping two face-down cards per turn. Unlike FLOW/HARMONY's
  real-time beat judgment or TUNING's single-track dial progression, the
  state here is a fixed grid checked off as pairs are found -- but the
  loop is kept as small as the rest of the portfolio: exactly one
  state-transition function per turn (`flip-pair`), matching TUNING's
  `lock-in` / ECHOES's `choose`. There is no shuffle, animation, or
  timing concern in this namespace -- a host adapter that wants a
  randomized layout shuffles the grid itself before handing it to this
  core; this core only ever sees a `grid` (a vector of Ghost keywords,
  one per card position) and a `state` (attempts taken so far + which
  indices are already collected).

  Invariant: `:collected` only ever grows (once a pair is found it stays
  face-up for the rest of the run) and a turn either grows it by exactly
  two indices (a match) or leaves it untouched (a miss) -- there is no
  path that removes an index once added. `flip-pair` is intentionally
  defensive rather than throwing on a malformed turn (same index twice,
  or an index that's already collected): it returns `state` unchanged
  and does not consume an attempt, mirroring TUNING's
  `lock-in`-no-op-when-complete guard. A caller (terminal/web host) is
  expected to avoid offering already-collected cards as pick targets,
  but core doesn't rely on that being enforced upstream."
  )

(def initial-state
  "既定の初期state。attemptsは0、collectedは空(全カード伏せた状態)。"
  {:attempts 0
   :collected #{}})

(defn total-pairs
  "gridに含まれるペア総数(カード枚数の半分)。"
  [grid]
  (quot (count grid) 2))

(defn pairs-collected
  "stateがこれまでに収集したペア数(collectedの半分)。"
  [state]
  (quot (count (:collected state)) 2))

(defn complete?
  "state(gridに対する)が全ペアを収集し終えたか。"
  [state grid]
  (>= (count (:collected state)) (count grid)))

(defn- match?
  "gridのindex i/jが異なる位置かつ同じGhostを指しているか。"
  [grid i j]
  (and (not= i j) (= (nth grid i) (nth grid j))))

(defn flip-pair
  "index i と j のカードを1手番として同時にflipする。

  無効な手番(i=j、またはiかjのいずれかが既にcollected済み)、あるいは
  既に全ペア収集済みの場合は、stateをそのまま返す -- attemptsは消費
  されない(呼び出し側の責任で無効な手を弾く運用でも良いが、coreは
  それを前提にしない)。

  それ以外は常にattemptsを1消費する。iとjが同じGhostなら両indexを
  :collectedに加える(以後ずっと表向き)。一致しなければattemptsだけ
  進め、:collectedは変わらない -- 『裏返して伏せ札に戻す』という演出は
  ここでは何もしないことと等価: このstateはどちらのカードが何だった
  かを覚えていない(そのペアがまだcollectedでない、という事実だけが
  残る)。"
  [state grid i j]
  (cond
    (complete? state grid) state
    (= i j) state
    (contains? (:collected state) i) state
    (contains? (:collected state) j) state
    (match? grid i j) (-> state
                          (update :attempts inc)
                          (update :collected conj i j))
    :else (update state :attempts inc)))

(defn efficiency
  "attemptsに対するtotal-pairsの比率(0.0〜1.0)。1手番=1ペアが理論上の
   最良(全部一発で当てた場合)なので、この比率が1.0に近いほど無駄が
   少ない。attempts=0(未プレイ)なら1.0(未プレイを非効率扱いにしない)。"
  [state grid]
  (let [attempts (:attempts state)]
    (if (zero? attempts)
      1.0
      (/ (double (total-pairs grid)) attempts))))

(defn grade
  "efficiencyから最終評価を返す。"
  [state grid]
  (let [eff (efficiency state grid)]
    (cond
      (>= eff 0.95) :perfect-memory
      (>= eff 0.7) :a
      (>= eff 0.5) :b
      (>= eff 0.3) :c
      :else :d)))

(defn summary
  "runの結果サマリ。ホストアダプタ側のリザルト画面にそのまま渡せる形。"
  [state grid]
  {:attempts (:attempts state)
   :pairs-collected (pairs-collected state)
   :total-pairs (total-pairs grid)
   :complete? (complete? state grid)
   :efficiency (efficiency state grid)
   :grade (grade state grid)})

(defn play
  "gridに対し、picks(各要素[i j])を順にflip-pairで適用する。picksが
   尽きても全ペア終わっていなければ、そこまでのstateで打ち切る(ホスト
   アダプタが途中で中断した場合に相当)。"
  [grid picks]
  (loop [state initial-state
         ps (seq picks)]
    (if (or (complete? state grid) (nil? ps))
      state
      (let [[i j] (first ps)]
        (recur (flip-pair state grid i j) (next ps))))))

(defn play-summary
  "play + summaryの合成。ホストアダプタが1run分のpick列を録り終えた後に
   呼ぶ最短経路。"
  [grid picks]
  (summary (play grid picks) grid))
