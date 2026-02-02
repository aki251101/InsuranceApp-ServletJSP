# InsuranceApp - 損保業務支援アプリ

自動車保険の契約管理と事故対応を支援するWebアプリケーションです。  
保険代理店の日常業務で発生する「契約の更新漏れ」や「事故対応の滞留」を防ぐことを目的としています。

## できること

### 契約管理
- 契約一覧の表示（タブ切り替え：更新可能契約 / 契約中 / 解約 / 失効 / 全件）
- 契約番号による検索、契約者名による検索
- 契約の新規登録（契約番号は年度ごとに自動採番、満期日は開始日から1年後を自動計算）
- 契約の更新（満期2か月前〜満期日の期間に実行可能）
- 契約の更新取消（当日限定）
- 契約の解約 / 解約取消（当日限定）
- 早期更改率の集計表示（当年度 / 当月）
- 要注意バッジ（満期20日前〜満期日の契約に表示）

### 事故管理
- 事故一覧の表示（タブ切り替え：対応中 / 完了）
- 事故の新規登録（契約を選択して登録）
- ステータス管理（受付 → 対応中 → 完了、完了からは戻せない）
- 「対応した」ボタンで最終対応日時を記録
- 対応メモの保存
- 滞留バッジ（最終対応から7日以上経過、または未対応の事故に表示）

## 技術スタック

| 項目 | 内容 |
|------|------|
| 言語 | Java 17 |
| サーバー | Apache Tomcat 10.1.x |
| フレームワーク | Servlet 6.0 / JSP 3.1（フレームワークなし） |
| データベース | MySQL 8.x |
| ビルドツール | Maven |
| CSS | Bootstrap 5 |
| ログ | SLF4J + Logback |

### 主な依存ライブラリ

| ライブラリ | バージョン | 用途 |
|-----------|-----------|------|
| jakarta.servlet-api | 6.0.0 | Servlet API |
| jakarta.servlet.jsp-api | 3.1.1 | JSP API |
| jakarta.servlet.jsp.jstl-api | 3.0.0 | JSTL API |
| mysql-connector-j | 8.2.0 | MySQL接続 |
| slf4j-api | 2.0.9 | ログAPI |
| logback-classic | 1.4.14 | ログ実装 |

## プロジェクト構成

```
src/main/
├── java/jp/insurance/system/
│   ├── controller/          ← Servlet（画面の入口）
│   │   ├── PolicyListServlet.java
│   │   ├── PolicyDetailServlet.java
│   │   ├── PolicyNewServlet.java
│   │   ├── PolicyActionServlet.java
│   │   ├── AccidentListServlet.java
│   │   ├── AccidentDetailServlet.java
│   │   ├── AccidentNewServlet.java
│   │   └── AccidentActionServlet.java
│   ├── service/             ← 業務ロジック
│   │   ├── PolicyService.java
│   │   ├── AccidentService.java
│   │   └── StatsService.java
│   ├── dao/                 ← データベースアクセス
│   │   ├── PolicyDao.java
│   │   └── AccidentDao.java
│   ├── model/               ← データの入れ物
│   │   ├── Policy.java
│   │   ├── PolicyStatus.java
│   │   ├── Accident.java
│   │   ├── AccidentStatus.java
│   │   └── RenewalStats.java
│   ├── exception/           ← 例外クラス
│   │   ├── BusinessException.java
│   │   └── SystemException.java
│   └── util/                ← 共通ユーティリティ
│       ├── Db.java
│       ├── DateUtil.java
│       └── DataInitializer.java
├── resources/
│   └── init.sql             ← テーブル作成・初期データ投入SQL
└── webapp/
    ├── index.jsp
    ├── css/style.css
    └── WEB-INF/
        ├── web.xml
        └── views/
            ├── policy/      ← 契約画面
            │   ├── list.jsp
            │   ├── detail.jsp
            │   └── new.jsp
            ├── accident/    ← 事故画面
            │   ├── list.jsp
            │   ├── detail.jsp
            │   └── new.jsp
            └── common/      ← 共通部品
                ├── header.jsp
                ├── footer.jsp
                └── notFound.jsp
```

## セットアップ手順

### 前提条件
- JDK 17 がインストールされていること
- Apache Tomcat 10.1.x がインストールされていること
- MySQL 8.x がインストール・起動されていること
- Maven がインストールされていること

### 1. データベースの準備

MySQL にログインして、`src/main/resources/init.sql` を実行します。

```sql
source /path/to/init.sql
```

これにより `insurance_app` データベースの作成、テーブル作成、初期データ投入が行われます。

### 2. データベース接続設定

`src/main/java/jp/insurance/system/util/Db.java` の接続情報を環境に合わせて変更してください。

```java
private static final String URL = "jdbc:mysql://localhost:3306/insurance_app";
private static final String USER = "root";
private static final String PASSWORD = "";  // ご自身のパスワードに変更
```

### 3. ビルド

プロジェクトのルートディレクトリで以下を実行します。

```bash
mvn clean package
```

`target/InsuranceApp.war` が生成されます。

### 4. デプロイ・起動

生成された `InsuranceApp.war` を Tomcat の `webapps/` ディレクトリに配置して Tomcat を起動します。

ブラウザで以下にアクセスしてください。

```
http://localhost:8080/InsuranceApp/
```

## 画面一覧

| 画面 | URL | 説明 |
|------|-----|------|
| トップ | `/` | 契約一覧へリダイレクト |
| 契約一覧 | `/policies` | タブ切り替え・検索・集計表示 |
| 契約詳細 | `/policies/detail?id={id}` | 更新・解約などの操作 |
| 契約新規登録 | `/policies/new` | 契約者名と開始日を入力して登録 |
| 事故一覧 | `/accidents` | 対応中・完了のタブ切り替え |
| 事故詳細 | `/accidents/detail?id={id}` | ステータス変更・メモ保存 |
| 事故新規登録 | `/accidents/new` | 契約を選択して事故を登録 |

## 設計方針

### アーキテクチャ
フレームワークを使わず、Servlet/JSPで基本的なMVCパターンを実装しています。

- **Controller（Servlet）**: リクエストの受付、パラメータ取得、Serviceの呼び出し、画面遷移
- **Service**: 業務ルールのチェック、トランザクション管理
- **DAO**: SQLの実行、ResultSetからModelへの変換
- **Model**: データの入れ物（Policy、Accidentなど）
- **View（JSP）**: 画面表示

### 例外処理
- **BusinessException**: 業務ルール違反（ユーザーにメッセージを表示）
- **SystemException**: DB接続エラーなどの想定外エラー

### ログ
- 重要な操作（更新・解約・ステータス変更など）をINFOレベルで記録
- 業務ルール違反をWARNレベルで記録
- DB例外をERRORレベルで記録
- 個人情報（氏名）はログに出さず、IDのみ記録

## このアプリを作った背景

保険代理店で13年間働いた経験から、日常業務で特に重要だった「契約更新の管理」と「事故対応の進捗管理」をWebアプリとして実装しました。早期更改期間（更新手続き可能な満期日2か月前から3週間前まで）での更新を保険会社から推奨されていたため、満期日の3週間前を過ぎた契約は契約一覧画面に「要注意」と表示するようにしました。また、事故対応一覧画面では、前回の対応から1週間過ぎると「滞留」と表示するようにしました。この2点により更新漏れ、対応漏れを防ぐ仕組みを作ることとしました。
