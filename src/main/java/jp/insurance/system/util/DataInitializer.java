package jp.insurance.system.util;

import jp.insurance.system.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static boolean initialized = false;

    public static synchronized void initializeIfNeeded() {
        if (initialized) {
            return;
        }

        logger.info("データベース初期化を開始します");
        
        try (Connection conn = Db.getConnection();
             Statement stmt = conn.createStatement()) {
            
            InputStream is = DataInitializer.class.getClassLoader()
                    .getResourceAsStream("init.sql");
            
            if (is == null) {
                throw new SystemException("init.sqlが見つかりません");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sql.append(line).append(" ");
                if (line.endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql = new StringBuilder();
                }
            }

            initialized = true;
            logger.info("データベース初期化が完了しました");
            
        } catch (Exception e) {
            logger.error("データベース初期化に失敗しました", e);
            throw new SystemException("データベース初期化に失敗しました", e);
        }
    }
}