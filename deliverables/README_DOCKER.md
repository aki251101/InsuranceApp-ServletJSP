# InsuranceApp - Docker Setup Draft

このファイルは、既存 `README.md` を Docker 対応前提にした確認用ドラフトです。  
問題なければ、内容を本番 README に反映してください。

## 前提条件

- Docker Desktop（または Docker Engine + Docker Compose）が使えること

## Docker での起動手順

1. プロジェクトルートで `.env` を作成します。

```bash
cp .env.example .env
```

PowerShell の場合:

```powershell
Copy-Item .env.example .env
```

2. `.env` の `MYSQL_ROOT_PASSWORD` を必ず変更します。

3. コンテナを起動します。

```bash
docker compose up --build -d
```

4. ブラウザでアクセスします。

```text
http://localhost:8080/InsuranceApp/
```

## 停止 / 再起動 / ログ確認

- 停止:

```bash
docker compose down
```

- データを含めて削除:

```bash
docker compose down -v
```

- ログ確認:

```bash
docker compose logs -f app
docker compose logs -f db
```

## DB 初期化について

- `src/main/resources/init.sql` を MySQL コンテナ起動時に自動実行します。
- 初回起動時のみ実行されます（`db_data` ボリュームが空の場合）。
- 再初期化したい場合は `docker compose down -v` 実行後に再起動してください。

## 環境変数

### `.env`（Compose 用）

- `APP_PORT`: アプリ公開ポート（デフォルト: `8080`）
- `DB_PORT`: DB公開ポート（デフォルト: `3306`）
- `MYSQL_DATABASE`: DB名（デフォルト: `insurance_app`）
- `MYSQL_ROOT_PASSWORD`: MySQL root パスワード（必須）
- `TZ`: タイムゾーン（デフォルト: `Asia/Tokyo`）

### app コンテナ（Db.java が参照）

- `INSURANCEAPP_DB_HOST=db`
- `INSURANCEAPP_DB_PORT=3306`
- `INSURANCEAPP_DB_NAME=${MYSQL_DATABASE}`
- `INSURANCEAPP_DB_USER=root`
- `INSURANCEAPP_DB_PASSWORD=${MYSQL_ROOT_PASSWORD}`

## 追加した Docker 関連ファイル

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`
- `.env.example`
