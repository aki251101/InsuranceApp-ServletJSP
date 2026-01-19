package jp.insurance.system.service;

import jp.insurance.system.dao.PolicyDao;
import jp.insurance.system.exception.SystemException;
import jp.insurance.system.model.Policy;
import jp.insurance.system.model.RenewalStats;
import jp.insurance.system.util.DateUtil;
import jp.insurance.system.util.Db;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StatsService {
    private final PolicyDao policyDao = new PolicyDao();

    public RenewalStats getFiscalYearStats() {
        try (Connection conn = Db.getConnection()) {
            List<Policy> policies = policyDao.findAll(conn);
            LocalDate today = DateUtil.today();
            LocalDate fiscalStart = DateUtil.getFiscalYearStart(today);
            LocalDate fiscalEnd = DateUtil.getFiscalYearEnd(today);

            return calculateStats(policies, today, fiscalStart, fiscalEnd);
        } catch (SQLException e) {
            throw new SystemException("年度更新統計の取得に失敗しました", e);
        }
    }

    public RenewalStats getMonthlyStats() {
        try (Connection conn = Db.getConnection()) {
            List<Policy> policies = policyDao.findAll(conn);
            LocalDate today = DateUtil.today();
            LocalDate monthStart = DateUtil.getMonthStart(today);
            LocalDate monthEnd = DateUtil.getMonthEnd(today);

            return calculateStats(policies, today, monthStart, monthEnd);
        } catch (SQLException e) {
            throw new SystemException("月次更新統計の取得に失敗しました", e);
        }
    }

    private RenewalStats calculateStats(List<Policy> policies, LocalDate today,
                                        LocalDate periodStart, LocalDate periodEnd) {
        int totalRenewalCount = 0;
        int earlyRenewalCount = 0;

        for (Policy policy : policies) {
            LocalDateTime renewedAt = policy.getRenewedAt();
            if (renewedAt == null || renewedAt.toLocalDate().isAfter(today)) {
                continue;
            }

            LocalDate dueEndDate = policy.getRenewalDueEndDate() != null
                    ? policy.getRenewalDueEndDate()
                    : policy.getEndDate();

            if (dueEndDate.isBefore(periodStart) || dueEndDate.isAfter(periodEnd)) {
                continue;
            }

            totalRenewalCount++;

            LocalDate earlyRenewalStart = dueEndDate.minusMonths(2);
            LocalDate earlyRenewalEnd = dueEndDate.minusDays(21);

            LocalDate renewedDate = renewedAt.toLocalDate();
            if (!renewedDate.isBefore(earlyRenewalStart) && !renewedDate.isAfter(earlyRenewalEnd)) {
                earlyRenewalCount++;
            }
        }

        return new RenewalStats(earlyRenewalCount, totalRenewalCount);
    }
}
