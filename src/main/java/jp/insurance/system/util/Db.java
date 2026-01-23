package jp.insurance.system.util;

import jp.insurance.system.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Db接続ユーティリティ（GitHub公開前提）
 *
 * ■方針
 * - DB接続情報は環境変数から取得（パスワード等の秘密情報をコードに含めない）
 * - URLは「組み立て」または「完全URL指定」のどちらでもOK
 * - ログにURLを出さない（URLに資格情報が混入する事故を防ぐ）
 *
 * ■推奨（組み立て方式）
 * - INSURANCEAPP_DB_HOST      (例: localhost)
 * - INSURANCEAPP_DB_PORT      (例: 3306)
 * - INSURANCEAPP_DB_NAME      (例: insurance_app)
 * - INSURANCEAPP_DB_USER      (例: root)
 * - INSURANCEAPP_DB_PASSWORD  (必須)
 *
 * ■互換（完全URL指定：任意）
 * - INSURANCEAPP_DB_URL
 *   例: jdbc:mysql://localhost:3306/insurance_app?useSSL=false&serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true
 *   ※ DB_URL を指定した場合、HOST/PORT/NAME は無視されます
 */
public class Db {
    private static final Logger logger = LoggerFactory.getLogger(Db.class);

    // --- env keys（キー名は公開OK） ---
    private static final String KEY_DB_URL = "INSURANCEAPP_DB_URL";
    private static final String KEY_DB_HOST = "INSURANCEAPP_DB_HOST";
    private static final String KEY_DB_PORT = "INSURANCEAPP_DB_PORT";
    private static final String KEY_DB_NAME = "INSURANCEAPP_DB_NAME";
    private static final String KEY_DB_USER = "INSURANCEAPP_DB_USER";
    private static final String KEY_DB_PASSWORD = "INSURANCEAPP_DB_PASSWORD";

    // --- defaults（公開して問題ない範囲のローカル向けデフォルト） ---
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "insurance_app";
    private static final String DEFAULT_USER = "root";

    // NOTE: URLの組み立て用（資格情報は含めない）
    private static final String JDBC_PARAMS =
            "useSSL=false&serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true";

    // 初期化（ドライバー）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("MySQLドライバーが見つかりません", e);
            throw new SystemException("MySQLドライバーが見つかりません", e);
        }
    }

    /**
     * コネクション取得
     */
    public static Connection getConnection() {
        String url = resolveJdbcUrl();
        String user = envOrDefault(KEY_DB_USER, DEFAULT_USER);
        String password = envOrThrow(KEY_DB_PASSWORD);

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // URLは出さない（URLに資格情報を混ぜてしまった場合の漏洩対策）
            logger.error("データベース接続に失敗しました (user={})", user, e);
            throw new SystemException("データベース接続に失敗しました", e);
        }
    }

    /**
     * クローズ（null安全）
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("コネクションのクローズに失敗しました", e);
            }
        }
    }

    /**
     * ロールバック（null安全）
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("ロールバックに失敗しました", e);
            }
        }
    }

    /**
     * JDBC URL を解決する。
     * 1) INSURANCEAPP_DB_URL があればそれを優先（互換）
     * 2) なければ host/port/dbName から組み立て（推奨）
     */
    private static String resolveJdbcUrl() {
        String url = System.getenv(KEY_DB_URL);
        if (url != null && !url.isBlank()) {
            return url.trim();
        }

        String host = envOrDefault(KEY_DB_HOST, DEFAULT_HOST);
        String port = envOrDefault(KEY_DB_PORT, DEFAULT_PORT);
        String dbName = envOrDefault(KEY_DB_NAME, DEFAULT_DB_NAME);

        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?" + JDBC_PARAMS;
    }

    /**
     * env取得（未設定ならデフォルト）
     */
    private static String envOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        return v.trim();
    }

    /**
     * env取得（未設定なら例外）
     */
    private static String envOrThrow(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            // ここで落とすことで「パスワードをコードに戻す」事故を防ぐ
            throw new SystemException("環境変数 " + key + " が未設定です。IntelliJの実行構成に設定してください。");
        }
        return v.trim();
    }
}
