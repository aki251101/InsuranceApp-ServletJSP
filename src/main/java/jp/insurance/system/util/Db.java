package jp.insurance.system.util;

import jp.insurance.system.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final Logger logger = LoggerFactory.getLogger(Db.class);

    /**
     * 公開（GitHub）を前提に、接続情報は環境変数から取得する。
     * IntelliJ実行構成（Tomcat）に環境変数を設定すれば、今まで通り動作する。
     *
     * 環境変数：
     * - INSURANCEAPP_DB_URL
     * - INSURANCEAPP_DB_USER
     * - INSURANCEAPP_DB_PASSWORD
     */
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/insurance_app?useSSL=false&serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";

    private static final String URL = envOrDefault("INSURANCEAPP_DB_URL", DEFAULT_URL);
    private static final String USER = envOrDefault("INSURANCEAPP_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = envOrThrow("INSURANCEAPP_DB_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("MySQLドライバーが見つかりません", e);
            throw new SystemException("MySQLドライバーが見つかりません", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            logger.error("データベース接続に失敗しました (url={}, user={})", URL, USER, e);
            throw new SystemException("データベース接続に失敗しました", e);
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("コネクションのクローズに失敗しました", e);
            }
        }
    }

    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("ロールバックに失敗しました", e);
            }
        }
    }

    private static String envOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        return v;
    }

    private static String envOrThrow(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            // ここで落とすことで「パスワードをコードに戻す」事故を防ぐ
            throw new SystemException("環境変数 " + key + " が未設定です。IntelliJの実行構成に設定してください。");
        }
        return v;
    }
}
