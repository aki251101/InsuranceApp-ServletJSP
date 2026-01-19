package jp.insurance.system.dao;

import jp.insurance.system.exception.SystemException;
import jp.insurance.system.model.Accident;
import jp.insurance.system.model.AccidentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccidentDao {
    private static final Logger logger = LoggerFactory.getLogger(AccidentDao.class);

    public List<Accident> findAll(Connection conn) {
        String sql = "SELECT a.*, p.policy_number, p.customer_name " +
                     "FROM accidents a " +
                     "INNER JOIN policies p ON a.policy_id = p.id " +
                     "ORDER BY a.occurred_at DESC";
        List<Accident> accidents = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                accidents.add(mapResultSetToAccident(rs));
            }
            
        } catch (SQLException e) {
            logger.error("事故一覧取得に失敗しました", e);
            throw new SystemException("事故一覧取得に失敗しました", e);
        }

        return accidents;
    }

    public List<Accident> findByStatus(Connection conn, List<AccidentStatus> statuses) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, p.policy_number, p.customer_name " +
            "FROM accidents a " +
            "INNER JOIN policies p ON a.policy_id = p.id " +
            "WHERE a.status IN ("
        );
        
        for (int i = 0; i < statuses.size(); i++) {
            sql.append("?");
            if (i < statuses.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") ORDER BY a.occurred_at DESC");

        List<Accident> accidents = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < statuses.size(); i++) {
                pstmt.setString(i + 1, statuses.get(i).name());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accidents.add(mapResultSetToAccident(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("事故検索に失敗しました", e);
            throw new SystemException("事故検索に失敗しました", e);
        }

        return accidents;
    }

    public Accident findById(Connection conn, Long id) {
        String sql = "SELECT a.*, p.policy_number, p.customer_name " +
                     "FROM accidents a " +
                     "INNER JOIN policies p ON a.policy_id = p.id " +
                     "WHERE a.id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccident(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("事故取得に失敗しました: id={}", id, e);
            throw new SystemException("事故取得に失敗しました", e);
        }

        return null;
    }

    public void insert(Connection conn, Accident accident) {
        String sql = "INSERT INTO accidents (policy_id, occurred_at, place, description, " +
                     "status, memo, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            
            pstmt.setLong(1, accident.getPolicyId());
            pstmt.setDate(2, Date.valueOf(accident.getOccurredAt()));
            pstmt.setString(3, accident.getPlace());
            pstmt.setString(4, accident.getDescription());
            pstmt.setString(5, accident.getStatus().name());
            pstmt.setString(6, accident.getMemo() != null ? accident.getMemo() : "");
            pstmt.setTimestamp(7, Timestamp.valueOf(now));
            pstmt.setTimestamp(8, Timestamp.valueOf(now));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    accident.setId(rs.getLong(1));
                }
            }
            
            logger.info("事故を登録しました: accidentId={}", accident.getId());
            
        } catch (SQLException e) {
            logger.error("事故登録に失敗しました", e);
            throw new SystemException("事故登録に失敗しました", e);
        }
    }

    public void update(Connection conn, Accident accident) {
        String sql = "UPDATE accidents SET status = ?, last_contacted_at = ?, " +
                     "memo = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accident.getStatus().name());
            
            if (accident.getLastContactedAt() != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(accident.getLastContactedAt()));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            
            pstmt.setString(3, accident.getMemo());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(5, accident.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("事故更新に失敗しました: accidentId={}", accident.getId(), e);
            throw new SystemException("事故更新に失敗しました", e);
        }
    }

    private Accident mapResultSetToAccident(ResultSet rs) throws SQLException {
        Accident accident = new Accident();
        accident.setId(rs.getLong("id"));
        accident.setPolicyId(rs.getLong("policy_id"));
        accident.setOccurredAt(rs.getDate("occurred_at").toLocalDate());
        accident.setPlace(rs.getString("place"));
        accident.setDescription(rs.getString("description"));
        accident.setStatus(AccidentStatus.valueOf(rs.getString("status")));
        
        Timestamp lastContactedAt = rs.getTimestamp("last_contacted_at");
        if (lastContactedAt != null) {
            accident.setLastContactedAt(lastContactedAt.toLocalDateTime());
        }
        
        accident.setMemo(rs.getString("memo"));
        accident.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        accident.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // JOIN結果
        accident.setPolicyNumber(rs.getString("policy_number"));
        accident.setCustomerName(rs.getString("customer_name"));
        
        return accident;
    }
}