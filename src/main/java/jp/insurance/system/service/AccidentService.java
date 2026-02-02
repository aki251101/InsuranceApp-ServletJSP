package jp.insurance.system.service;

import jp.insurance.system.dao.AccidentDao;
import jp.insurance.system.dao.PolicyDao;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.exception.SystemException;
import jp.insurance.system.model.Accident;
import jp.insurance.system.model.AccidentStatus;
import jp.insurance.system.model.Policy;
import jp.insurance.system.util.DateUtil;
import jp.insurance.system.util.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class AccidentService {
    private static final Logger logger = LoggerFactory.getLogger(AccidentService.class);
    private final AccidentDao accidentDao = new AccidentDao();
    private final PolicyDao policyDao = new PolicyDao();

    public List<Accident> getAccidentsByTab(String tab) {
        try (Connection conn = Db.getConnection()) {
            if ("active".equals(tab)) {
                return accidentDao.findByStatus(conn,
                        Arrays.asList(AccidentStatus.OPEN, AccidentStatus.IN_PROGRESS));
            } else if ("resolved".equals(tab)) {
                return accidentDao.findByStatus(conn,
                        Arrays.asList(AccidentStatus.RESOLVED));
            } else {
                return accidentDao.findAll(conn);
            }
        } catch (SQLException e) {
            logger.error("事故一覧の取得に失敗しました", e);
            throw new SystemException("事故一覧の取得に失敗しました", e);
        }
    }

    public Accident getAccidentById(Long id) throws BusinessException {
        try (Connection conn = Db.getConnection()) {
            Accident accident = accidentDao.findById(conn, id);
            if (accident == null) {
                throw new BusinessException("事故が見つかりません");
            }
            return accident;
        } catch (SQLException e) {
            logger.error("事故取得に失敗しました", e);
            throw new BusinessException("事故取得に失敗しました", e);
        }
    }

    public void createAccident(Long policyId, LocalDate occurredAt, String place,
                               String description) throws BusinessException {
        if (policyId == null) {
            throw new BusinessException("契約IDは必須です");
        }
        if (occurredAt == null) {
            throw new BusinessException("事故受付日は必須です");
        }

        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Policy policy = policyDao.findById(conn, policyId);
            if (policy == null) {
                throw new BusinessException("指定された契約が見つかりません");
            }

            Accident accident = new Accident();
            accident.setPolicyId(policyId);
            accident.setOccurredAt(occurredAt);
            accident.setPlace(place != null ? place.trim() : "");
            accident.setDescription(description != null ? description.trim() : "");
            accident.setStatus(AccidentStatus.OPEN);
            accident.setMemo("");

            accidentDao.insert(conn, accident);
            conn.commit();

            logger.info("事故を登録しました: accidentId={}", accident.getId());

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("事故登録に失敗しました", e);
            throw new BusinessException("事故登録に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void changeStatus(Long accidentId, AccidentStatus newStatus) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Accident accident = accidentDao.findById(conn, accidentId);
            if (accident == null) {
                throw new BusinessException("事故が見つかりません");
            }

            AccidentStatus currentStatus = accident.getStatus();

            if (currentStatus == AccidentStatus.RESOLVED) {
                throw new BusinessException("完了済みの事故は変更できません");
            }

            if (currentStatus == AccidentStatus.OPEN && newStatus == AccidentStatus.IN_PROGRESS) {
                accident.setStatus(AccidentStatus.IN_PROGRESS);
            } else if (currentStatus == AccidentStatus.IN_PROGRESS && newStatus == AccidentStatus.RESOLVED) {
                accident.setStatus(AccidentStatus.RESOLVED);
            } else {
                throw new BusinessException("不正な状態遷移です");
            }

            // ステータス変更 = 対応した行為なので、最終対応日時も更新する
            accident.setLastContactedAt(LocalDateTime.now());

            accidentDao.update(conn, accident);
            conn.commit();

            logger.info("事故のステータスを変更しました: accidentId={}, status={}",
                    accidentId, newStatus);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("事故ステータス変更に失敗しました: accidentId={}", accidentId, e);
            throw new BusinessException("事故ステータス変更に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void markContacted(Long accidentId) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Accident accident = accidentDao.findById(conn, accidentId);
            if (accident == null) {
                throw new BusinessException("事故が見つかりません");
            }

            if (accident.getStatus() == AccidentStatus.RESOLVED) {
                throw new BusinessException("完了済みの事故は更新できません");
            }

            accident.setLastContactedAt(LocalDateTime.now());
            accidentDao.update(conn, accident);
            conn.commit();

            logger.info("事故対応日時を更新しました: accidentId={}", accidentId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("事故対応日時更新に失敗しました: accidentId={}", accidentId, e);
            throw new BusinessException("事故対応日時更新に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public void saveMemo(Long accidentId, String memo) throws BusinessException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            Accident accident = accidentDao.findById(conn, accidentId);
            if (accident == null) {
                throw new BusinessException("事故が見つかりません");
            }

            accident.setMemo(memo != null ? memo : "");
            accidentDao.update(conn, accident);
            conn.commit();

            logger.info("事故メモを保存しました: accidentId={}", accidentId);

        } catch (BusinessException e) {
            Db.rollback(conn);
            throw e;
        } catch (Exception e) {
            Db.rollback(conn);
            logger.error("事故メモ保存に失敗しました: accidentId={}", accidentId, e);
            throw new BusinessException("事故メモ保存に失敗しました", e);
        } finally {
            Db.close(conn);
        }
    }

    public boolean isStagnant(Accident accident) {
        if (accident.getStatus() == AccidentStatus.RESOLVED) {
            return false;
        }

        LocalDateTime lastContacted = accident.getLastContactedAt();
        if (lastContacted == null) {
            return true;
        }

        LocalDate sevenDaysAgo = DateUtil.today().minusDays(7);
        return lastContacted.toLocalDate().isBefore(sevenDaysAgo) ||
                lastContacted.toLocalDate().isEqual(sevenDaysAgo);
    }
}
