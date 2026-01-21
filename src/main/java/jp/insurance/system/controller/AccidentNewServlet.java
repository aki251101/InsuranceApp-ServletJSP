package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.model.Policy;
import jp.insurance.system.service.AccidentService;
import jp.insurance.system.service.PolicyService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Day51: 入力検証（必須/形式/範囲） + 入力不正は400 + errors
 *
 * 修正ポイント（重要）:
 * - policyId=abc のような「形式不正」を request attribute に String のまま入れると、
 *   JSP側で `${policyId == policy.id ? ...}` の比較時に EL が String→Long 変換を試みて例外(500)になります。
 * - そのため、JSPへ渡す policyId は「Long（またはnull）」に統一し、形式不正時は null を渡します。
 */
public class AccidentNewServlet extends HttpServlet {
    private final AccidentService accidentService = new AccidentService();
    private final PolicyService policyService = new PolicyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Policy> policies = policyService.getAllPolicies();
        request.setAttribute("policies", policies);
        request.getRequestDispatcher("/WEB-INF/views/accident/new.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String policyIdStr = request.getParameter("policyId");
        String occurredAtStr = request.getParameter("occurredAt");
        String place = request.getParameter("place");
        String description = request.getParameter("description");

        // null対策（任意項目）
        if (place == null) place = "";
        if (description == null) description = "";

        // C: 入力検証（最低3つ：必須/形式/範囲）
        List<String> errors = new ArrayList<>();

        // 必須
        if (policyIdStr == null || policyIdStr.isBlank()) {
            errors.add("契約を選択してください。");
        }
        if (occurredAtStr == null || occurredAtStr.isBlank()) {
            errors.add("事故受付日を入力してください。");
        }

        // 形式 + 範囲（policyId）
        Long policyId = null;
        if (policyIdStr != null && !policyIdStr.isBlank()) {
            try {
                policyId = Long.parseLong(policyIdStr);
                if (policyId <= 0) {
                    errors.add("契約IDが不正です。");
                    policyId = null; // JSPへはnullを渡す（ELの型変換例外を防ぐ）
                }
            } catch (NumberFormatException ex) {
                errors.add("契約IDが不正です。");
                policyId = null; // JSPへはnullを渡す（ELの型変換例外を防ぐ）
            }
        }

        // 形式（occurredAt）
        LocalDate occurredAt = null;
        if (occurredAtStr != null && !occurredAtStr.isBlank()) {
            try {
                occurredAt = LocalDate.parse(occurredAtStr);
            } catch (Exception ex) {
                errors.add("事故受付日の形式が不正です。（例：2026-01-21）");
            }
        }

        // 範囲（文字数上限）
        if (place.length() > 100) {
            errors.add("場所は100文字以内で入力してください。");
        }
        if (description.length() > 500) {
            errors.add("概要は500文字以内で入力してください。");
        }

        // D: 入力不正は 400 + errors で返す（入力値は画面に戻す）
        if (!errors.isEmpty()) {
            response.setStatus(400);
            forwardToNew(request, response, errors, null, policyId, occurredAtStr, place, description);
            return;
        }

        try {
            // ここに来る時点で、policyId と occurredAt は必須/形式チェックを通過している想定
            accidentService.createAccident(policyId, occurredAt, place, description);
            response.sendRedirect(request.getContextPath() + "/accidents");

        } catch (BusinessException e) {
            // サービス層の業務エラーも 400 + errors として返す
            response.setStatus(400);
            errors.add(e.getMessage());
            forwardToNew(request, response, errors, null, policyId, occurredAtStr, place, description);

        } catch (Exception e) {
            // 予期しない例外（システムエラー）
            response.setStatus(500);
            forwardToNew(
                    request,
                    response,
                    null,
                    "システムエラーが発生しました。時間をおいて再度お試しください。",
                    policyId,
                    occurredAtStr,
                    place,
                    description
            );
        }
    }

    /**
     * 新規登録フォームへ戻す共通処理
     */
    private void forwardToNew(
            HttpServletRequest request,
            HttpServletResponse response,
            List<String> errors,
            String errorMessage,
            Long policyId,
            String occurredAtStr,
            String place,
            String description
    ) throws ServletException, IOException {

        List<Policy> policies = policyService.getAllPolicies();
        request.setAttribute("policies", policies);

        if (errors != null && !errors.isEmpty()) {
            request.setAttribute("errors", errors);
        }
        if (errorMessage != null && !errorMessage.isBlank()) {
            request.setAttribute("errorMessage", errorMessage);
        }

        // 再表示用（ユーザーが入力した値を戻す）
        // ★重要：policyIdは Long（またはnull）で渡す（JSP ELの型変換例外を防ぐ）
        request.setAttribute("policyId", policyId);
        request.setAttribute("occurredAt", occurredAtStr);
        request.setAttribute("place", place);
        request.setAttribute("description", description);

        request.getRequestDispatcher("/WEB-INF/views/accident/new.jsp")
                .forward(request, response);
    }
}
