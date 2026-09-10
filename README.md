# GHOST HACKER: メモライズ

![test](https://github.com/com-junkawasaki/ghosthacker-memorize/actions/workflows/test.yml/badge.svg)

Ghost Hacker ゲームポートフォリオ第7弾。設計は
[ADR-2607023200](../../../90-docs/adr/2607023200-ghosthacker-game-portfolio-flow.md)
（superproject `com-junkawasaki/root`、addendum 2）を参照。

[Ghost Hacker](https://github.com/com-junkawasaki/ghosthacker)（既存カノン: Ren/Nei、
「情報は物理だ」、情報場、Ghost Battle / Daemon Battle）を土台に、FreeTEMPOの
『Life』（2010）収録曲 "メモライズ" に由来する、10ジャンル展開の第7弾。

## コンセプト

- **ジャンル**: カードゲーム（記憶デッキビルダー）
- **主人公**: Nei主導
- **コアループ**: 事件の記憶=Ghostをカード化した固定グリッド（3〜4ペア規模、
  他タイトルと同じ小さなサンプルサイズ）を、2枚ずつ裏返して同じGhostの
  ペアを探す古典的な神経衰弱。揃えばそのペアは`collected`として表向きの
  まま固定され、揃わなければ手番(attempts)だけが進む — 「裏返して伏せ札に
  戻す」という演出は状態としては何もしないことと等価（このstateはどちらの
  カードが何だったかを覚えていない）。全ペア収集で1run終了。

  本タイトルは**フルのデッキビルダーではなく、意図的に最小の神経衰弱scaffold**
  にしてある（FLOW/HARMONY/ECHOES/TUNINGが単一の小さな判定/状態ループに
  留めてきたポートフォリオ全体の慎ましさに合わせた設計判断）。

## 実装範囲

`src/ghosthacker_memorize/core.kotoba` — pure、host-free。判定/state核:

- `flip-pair` — index i/jの2枚を1手番としてflipし、一致すれば両方を
  `:collected`に加え、不一致なら`:attempts`だけ進める。同一index指定・
  既にcollected済みのindexを含む手番・全ペア収集済み後の呼び出しは
  無効な手番としてstateを変えない（attemptsも消費しない）——TUNINGの
  `lock-in`-no-op-when-completeガードと同じ防御姿勢
- `complete?`/`total-pairs`/`pairs-collected` — 収集状況の判定
- `efficiency`/`grade`/`summary` — attempts対total-pairsの効率でgradeを
  決めるリザルト集計（少ない手数で全ペア揃えるほど高評価）

`src/ghosthacker_memorize/deck.kotoba` — サンプルの完結したグリッド
（`first-case-file`、8枚/4ペア、シャッフルなしの決定的配置）。

**プレイ可能な最小プロトタイプ**として `src/ghosthacker_memorize/terminal.kotoba`
がある。TUNINGと同じくリアルタイム判定が無いため、`future`/agentスレッド
プールを一切使わない素朴なread-line駆動ループ（1手番で"i j"形式の2
インデックスを一度に読み、collected済みは名前を、それ以外は"?"を表示）。

**ブラウザで遊べるホストアダプタ**が `src/ghosthacker_memorize/web.kotoba`
（reagent、ADR-2607023200 addendum 2の方針どおりWeb Audio不要）:
カードグリッドをクリック可能なボタンとして描画し、1枚目のクリックだけを
`:first-pick`としてUI側のローカル状態に保持、2枚目のクリックで両indexを
まとめて`core/flip-pair`に渡す（coreは「2枚同時」しか知らず、1枚目/2枚目
という手番内の順序はホストアダプタの関心事）。

## 開発

```bash
clojure -M:test
```

Lint（clj-kondo、Clojars経由でHomebrew等の別インストール不要）:

```bash
clojure -M:lint
```

`main`へのpush/PRで `.github/workflows/test.yml` が自動でテスト+lintを実行する。

`src/ghosthacker_memorize/bounded.kotoba` は固定8枚の`first-case-file`と
最大16手のflat index pair列を対象にするcapability-freeなKotobaプロファイル。
収集集合の単調増加、無効手のno-op、完了後の停止を保持し、効率はJSの
浮動小数へ委譲せず正確な分子/分母として返す。任意grid、shuffle、host状態は
CLJC oracleに残す。

ターミナルで遊んでみる:

```bash
clojure -M -m ghosthacker-memorize.terminal
```

ブラウザで遊んでみる（`npm install`は初回のみ）:

```bash
npm install
npx shadow-cljs watch app   # http://localhost:8300 で自動リロード開発
npx shadow-cljs release app # public/ に静的バンドルをビルド(デプロイ可能)
```

変更履歴は [CHANGELOG.md](CHANGELOG.md)。

## ライセンス

MIT License — [LICENSE](LICENSE) 参照。
