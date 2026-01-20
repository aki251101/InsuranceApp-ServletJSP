package jp.insurance.system.util;

import jp.insurance.system.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static boolean initialized = false;

    /**
     * 安全柵：このDB名（スキーマ名）のときだけ init.sql を実行する。
     * ※JDBC URL が jdbc:mysql://.../insurance_app の場合、SELECT DATABASE() は通常 insurance_app を返す。
     */
    private static final String EXPECTED_DB = "insurance_app";

    public static synchronized void initializeIfNeeded() {
        if (initialized) {
            return;
        }

        logger.info("データベース初期化を開始します");

        try (Connection conn = Db.getConnection()) {

            // --- 安全柵：接続先DB名を確認し、想定外なら初期化をスキップ（アプリは落とさない） ---
            String actualDb = getCurrentDatabaseName(conn);
            if (actualDb == null || !EXPECTED_DB.equals(actualDb)) {
                logger.warn("データベース初期化をスキップします（接続先DBが想定外） expected={}, actual={}", EXPECTED_DB, actualDb);
                return; // A案：初期化だけ中断し、アプリ起動は継続
            }

            try (Statement stmt = conn.createStatement()) {
                InputStream is = DataInitializer.class.getClassLoader().getResourceAsStream("init.sql");

                if (is == null) {
                    throw new SystemException("init.sqlが見つかりません");
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sql = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("--")) {
                            continue;
                        }
                        sql.append(line).append(' ');
                        if (line.endsWith(";")) {
                            stmt.execute(sql.toString());
                            sql.setLength(0);
                        }
                    }
                }
            }

            initialized = true;
            logger.info("データベース初期化が完了しました");

        } catch (Exception e) {
            logger.error("データベース初期化に失敗しました", e);
            throw new SystemException("データベース初期化に失敗しました", e);
        }
    }

    /**
     * SELECT DATABASE() の結果（現在のデフォルトDB名）を取得する。
     * - DB名が未選択の接続（jdbc:mysql://host:port/ のようにDB名なし）だと null になり得る。
     */
    private static String getCurrentDatabaseName(Connection conn) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (Exception e) {
            // 安全柵の取得で例外が出る場合も「初期化しない」方針に倒す
            logger.warn("接続先DB名の取得に失敗したため初期化をスキップします", e);
            return null;
        }
    }
}
