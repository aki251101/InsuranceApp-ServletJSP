package jp.insurance.system.util;

import jp.insurance.system.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final Logger logger = LoggerFactory.getLogger(Db.class);
    
    private static final String URL = "jdbc:mysql://localhost:3306/insurance_app?useSSL=false&serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "xV3%jlAkrasV";

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
            logger.error("データベース接続に失敗しました", e);
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
}