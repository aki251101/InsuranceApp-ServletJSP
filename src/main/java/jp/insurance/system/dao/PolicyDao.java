package jp.insurance.system.dao;

import jp.insurance.system.exception.SystemException;
import jp.insurance.system.model.Policy;
import jp.insurance.system.model.PolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PolicyDao {
    private static final Logger logger = LoggerFactory.getLogger(PolicyDao.class);

    public List<Policy> findAll(Connection conn) {
        String sql = "SELECT * FROM policies ORDER BY end_date ASC, policy_number ASC";
        List<Policy> policies = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                policies.add(mapResultSetToPolicy(rs));
            }

        } catch (SQLException e) {
            logger.error("契約一覧取得に失敗しました", e);
            throw new SystemException("契約一覧取得に失敗しました", e);
        }

        return policies;
    }

    public List<Policy> search(Connection conn, String query) {
        String sql = "SELECT * FROM policies WHERE policy_number LIKE ? OR customer_name LIKE ? " +
                "ORDER BY end_date ASC, policy_number ASC";
        List<Policy> policies = new ArrayList<>();
        String likeQuery = "%" + query + "%";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, likeQuery);
            pstmt.setString(2, likeQuery);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    policies.add(mapResultSetToPolicy(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("契約検索に失敗しました", e);
            throw new SystemException("契約検索に失敗しました", e);
        }

        return policies;
    }

    public Policy findById(Connection conn, Long id) {
        String sql = "SELECT * FROM policies WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPolicy(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("契約取得に失敗しました: id={}", id, e);
            throw new SystemException("契約取得に失敗しました", e);
        }

        return null;
    }

    public void insert(Connection conn, Policy policy) {
        String sql = "INSERT INTO policies (policy_number, customer_name, start_date, end_date, " +
                "status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();

            pstmt.setString(1, policy.getPolicyNumber());
            pstmt.setString(2, policy.getCustomerName());
            pstmt.setDate(3, Date.valueOf(policy.getStartDate()));
            pstmt.setDate(4, Date.valueOf(policy.getEndDate()));
            pstmt.setString(5, policy.getStatus().name());
            pstmt.setTimestamp(6, Timestamp.valueOf(now));
            pstmt.setTimestamp(7, Timestamp.valueOf(now));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    policy.setId(rs.getLong(1));
                }
            }

            logger.info("契約を登録しました: policyId={}", policy.getId());

        } catch (SQLException e) {
            logger.error("契約登録に失敗しました", e);
            throw new SystemException("契約登録に失敗しました", e);
        }
    }

    public void update(Connection conn, Policy policy) {
        String sql = "UPDATE policies SET end_date = ?, status = ?, renewal_due_end_date = ?, " +
                "renewed_at = ?, cancelled_at = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(policy.getEndDate()));
            pstmt.setString(2, policy.getStatus().name());

            if (policy.getRenewalDueEndDate() != null) {
                pstmt.setDate(3, Date.valueOf(policy.getRenewalDueEndDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            if (policy.getRenewedAt() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(policy.getRenewedAt()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }

            if (policy.getCancelledAt() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(policy.getCancelledAt()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(7, policy.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("契約更新に失敗しました: policyId={}", policy.getId(), e);
            throw new SystemException("契約更新に失敗しました", e);
        }
    }

    public boolean existsByPolicyNumber(Connection conn, String policyNumber) {
        String sql = "SELECT COUNT(*) FROM policies WHERE policy_number = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, policyNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("契約番号の存在チェックに失敗しました", e);
            throw new SystemException("契約番号の存在チェックに失敗しました", e);
        }

        return false;
    }

    /**
     * 指定年度の最大連番を取得する。
     * 契約番号の形式: P-YYYY-NNNN
     * 該当年度のデータがない場合は0を返す。
     */
    public int findMaxSequenceByFiscalYear(Connection conn, int year) {
        String prefix = "P-" + year + "-";
        String sql = "SELECT MAX(CAST(SUBSTRING(policy_number, ?) AS UNSIGNED)) " +
                "FROM policies WHERE policy_number LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prefix.length() + 1);
            pstmt.setString(2, prefix + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int maxSeq = rs.getInt(1);
                    if (rs.wasNull()) {
                        return 0;
                    }
                    return maxSeq;
                }
            }

        } catch (SQLException e) {
            logger.error("契約番号の最大連番取得に失敗しました: year={}", year, e);
            throw new SystemException("契約番号の最大連番取得に失敗しました", e);
        }

        return 0;
    }

    private Policy mapResultSetToPolicy(ResultSet rs) throws SQLException {
        Policy policy = new Policy();
        policy.setId(rs.getLong("id"));
        policy.setPolicyNumber(rs.getString("policy_number"));
        policy.setCustomerName(rs.getString("customer_name"));
        policy.setStartDate(rs.getDate("start_date").toLocalDate());
        policy.setEndDate(rs.getDate("end_date").toLocalDate());
        policy.setStatus(PolicyStatus.valueOf(rs.getString("status")));

        Date renewalDueEndDate = rs.getDate("renewal_due_end_date");
        if (renewalDueEndDate != null) {
            policy.setRenewalDueEndDate(renewalDueEndDate.toLocalDate());
        }

        Timestamp renewedAt = rs.getTimestamp("renewed_at");
        if (renewedAt != null) {
            policy.setRenewedAt(renewedAt.toLocalDateTime());
        }

        Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
        if (cancelledAt != null) {
            policy.setCancelledAt(cancelledAt.toLocalDateTime());
        }

        policy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        policy.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return policy;
    }
}