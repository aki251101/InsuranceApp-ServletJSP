package jp.insurance.system.service;

import jp.insurance.system.dao.PolicyDao;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.exception.SystemException;
import jp.insurance.system.model.Policy;
import jp.insurance.system.model.PolicyStatus;
import jp.insurance.system.util.DateUtil;
import jp.insurance.system.util.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PolicyService {
    private static final Logger logger = LoggerFactory.getLogger(PolicyService.class);
    private final PolicyDao policyDao = new PolicyDao();

    public List<Policy> getAllPolicies() {
        try (Connection conn = Db.getConnection()) {
            return policyDao.findAll(conn);
        } catch (SQLException e) {
            logger.error("契約一覧の取得に失敗しました", e);
            throw new SystemException("契約一覧の取得に失敗しました", e);
        }
    }

    public List<Policy> searchPolicies(String query) {
        try (Connection conn = Db.getConnection()) {
            if (query == null || query.trim().isEmpty()) {
                return policyDao.findAll(conn);
            }
            return policyDao.search(conn, query.trim());
        } catch (SQLException e) {
            logger.error("契約検索に失敗しました: query={}", query, e);
            throw new SystemException("契約検索に失敗しました", e);
        }
    }

    public List<Policy> filterByTab(List<Policy> policies, String tab) {
        LocalDate today = DateUtil.today();

        switch (tab) {
            case "renewable":
                return policies.stream()
                        .filter(p -> isRenewable(p, today))
                        .collect(Collectors.toList());
            case "active":
                return policies.stream()
                        .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                        .collect(Collectors.toList());
            case "cancelled":
                return policies.stream()
                        .filter(p -> p.getStatus() == PolicyStatus.CANCELLED)
                        .collect(Collectors.toList());
            case "lapsed":
                return policies.stream()
                        .filter(p -> isLapsed(p, today))
                        .collect(Collectors.toList());
            default:
                return policies;
        }
    }

    public Policy getPolicyById(Long id) throws BusinessException {
        try (Connection conn = Db.getConnection()) {
            Policy policy = policyDao.findById(conn, id);
            if (policy == null) {
                throw new BusinessException("契約が見つかりません");
            }
            return policy;
        } catch (SQLException e) {
            logger.error("契約取得に失敗しました: policyId={}", id, e);
            throw new BusinessException("契約取得に失敗しました", e);
        }
    }

    public void createPolicy(String policyNumber, String customerName,
                             LocalDate startDate, LocalDate endDate) throws BusinessException {
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            throw new BusinessException("契約番号は必須です");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new BusinessException("契約者名は必須です");
        }
        if (startDate == null) {
            throw new BusinessException("開始日は必須です");
        }
        if (endDate == null) {
            throw new BusinessException("満期日は必須です");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("満期日は開始日より後である必要があります");
        }

        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            if (policyDao.existsByPolicyNumber(conn, policyNumber.trim())) {
                throw new BusinessException("契約番号が既に存在します");
            }

            Policy policy = new Policy();
            policy.setPolicyNumber(policyNumber.trim());
            policy.setCustomerName(customerName.trim());
            policy.setStartDate(startDate);
            policy.setEndDate(endDate);
            policy.setStatus(PolicyStatus.ACTIVE);

            policyDao.insert(conn, policy);
            conn.commit();

            logger.info("契約を作成しました: policyId={}", policy.getId());

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("契約作成に失敗しました", e);
            throw new BusinessException("契約作成に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void renewPolicy(Long policyId) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Policy policy = policyDao.findById(conn, policyId);
            if (policy == null) {
                throw new BusinessException("契約が見つかりません");
            }

            LocalDate today = DateUtil.today();
            if (policy.getStatus() != PolicyStatus.ACTIVE) {
                throw new BusinessException("契約中の契約のみ更新できます");
            }

            LocalDate renewableStart = policy.getEndDate().minusMonths(2);
            if (today.isBefore(renewableStart) || today.isAfter(policy.getEndDate())) {
                throw new BusinessException("更新可能期間外のため更新できません");
            }

            policy.setRenewalDueEndDate(policy.getEndDate());
            policy.setEndDate(policy.getEndDate().plusYears(1));
            policy.setRenewedAt(LocalDateTime.now());

            policyDao.update(conn, policy);
            conn.commit();

            logger.info("契約を更新しました: policyId={}", policyId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("契約更新に失敗しました: policyId={}", policyId, e);
            throw new BusinessException("契約更新に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void unrenewPolicy(Long policyId) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Policy policy = policyDao.findById(conn, policyId);
            if (policy == null) {
                throw new BusinessException("契約が見つかりません");
            }

            if (policy.getRenewedAt() == null || policy.getRenewalDueEndDate() == null) {
                throw new BusinessException("更新されていない契約です");
            }

            LocalDate today = DateUtil.today();
            LocalDate renewedDate = policy.getRenewedAt().toLocalDate();
            if (!renewedDate.equals(today)) {
                throw new BusinessException("当日のみ取り消しできます");
            }

            policy.setEndDate(policy.getRenewalDueEndDate());
            policy.setRenewedAt(null);
            policy.setRenewalDueEndDate(null);

            policyDao.update(conn, policy);
            conn.commit();

            logger.info("契約更新を取り消しました: policyId={}", policyId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("契約更新取消に失敗しました: policyId={}", policyId, e);
            throw new BusinessException("契約更新取消に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void cancelPolicy(Long policyId) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Policy policy = policyDao.findById(conn, policyId);
            if (policy == null) {
                throw new BusinessException("契約が見つかりません");
            }

            if (policy.getStatus() != PolicyStatus.ACTIVE) {
                throw new BusinessException("契約中の契約のみ解約できます");
            }

            policy.setStatus(PolicyStatus.CANCELLED);
            policy.setCancelledAt(LocalDateTime.now());

            policyDao.update(conn, policy);
            conn.commit();

            logger.info("契約を解約しました: policyId={}", policyId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("契約解約に失敗しました: policyId={}", policyId, e);
            throw new BusinessException("契約解約に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void uncancelPolicy(Long policyId) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Policy policy = policyDao.findById(conn, policyId);
            if (policy == null) {
                throw new BusinessException("契約が見つかりません");
            }

            if (policy.getCancelledAt() == null) {
                throw new BusinessException("解約されていない契約です");
            }

            LocalDate today = DateUtil.today();
            LocalDate cancelledDate = policy.getCancelledAt().toLocalDate();
            if (!cancelledDate.equals(today)) {
                throw new BusinessException("当日のみ取り消しできます");
            }

            policy.setStatus(PolicyStatus.ACTIVE);
            policy.setCancelledAt(null);

            policyDao.update(conn, policy);
            conn.commit();

            logger.info("契約解約を取り消しました: policyId={}", policyId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("契約解約取消に失敗しました: policyId={}", policyId, e);
            throw new BusinessException("契約解約取消に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public boolean isRenewable(Policy policy, LocalDate today) {
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            return false;
        }
        LocalDate renewableStart = policy.getEndDate().minusMonths(2);
        return !today.isBefore(renewableStart) && !today.isAfter(policy.getEndDate());
    }

    public boolean isAttentionPeriod(Policy policy, LocalDate today) {
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            return false;
        }
        LocalDate attentionStart = policy.getEndDate().minusDays(20);
        return !today.isBefore(attentionStart) && !today.isAfter(policy.getEndDate());
    }

    public boolean isLapsed(Policy policy, LocalDate today) {
        return policy.getStatus() != PolicyStatus.CANCELLED && today.isAfter(policy.getEndDate());
    }

    public String getDisplayStatus(Policy policy) {
        LocalDate today = DateUtil.today();
        if (isLapsed(policy, today)) {
            return "失効";
        }
        return policy.getStatus().getDisplayName();
    }
}
