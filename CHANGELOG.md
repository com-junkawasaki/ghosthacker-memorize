# Changelog

pure `.cljc` 記憶マッチ核（`ghosthacker-memorize.core`）と、それを使う
プロトタイプ実装の変更履歴（ADR-2607023200 addendum 2、portfolio title #7）。

## Unreleased

- 初期実装: `core.cljc`（`flip-pair`によるindex 2枚同時flip判定、
  attempts/collectedの状態遷移、無効な手番へのno-opガード、
  attempts対total-pairsのefficiencyから決まるgrade/summary）、
  `deck.cljc`（サンプルグリッド`first-case-file`、8枚/4ペア、シャッフル
  なしの決定的配置）、`terminal.clj`（プレイ可能なread-line駆動プロト
  タイプ、futureなし、"i j"形式で2インデックスを一度に受け取る）、
  `web.cljs`（ブラウザhostアダプタ、reagent、Web Audio不要、1枚目
  クリックをUIローカルの`:first-pick`として保持し2枚目で手番を確定）。
  `clojure -M:test`（16 tests / 57 assertions）と`clojure -M:lint`
  （clj-kondo 0 errors/0 warnings）が通ることを確認済み。headless DOM
  上で実クリック操作による通し（マッチしたペアがcollected表示になる
  こと、外れたペアが手番だけ進めて状態をリセットすること、4ペア全収集
  でresult画面に到達すること、もう一度ボタンで初期状態に復帰すること）
  を検証済み。
