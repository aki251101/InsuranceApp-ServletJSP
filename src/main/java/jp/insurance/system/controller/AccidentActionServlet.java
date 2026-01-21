package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.model.AccidentStatus;
import jp.insurance.system.service.AccidentService;

import java.io.IOException;

/**
 * Day51（E）：例外→応答の統一（業務/システム）
 *
 * 方針：
 * - 入力不正（必須不足/形式不正/範囲不正）: 400（Bad Request）
 * - 業務例外（BusinessException）: 400（Bad Request）
 * - 想定外例外: 500（Internal Server Error）
 *
 * NOTE:
 * - このServletは通常「画面操作」から呼ばれるため、正常系は従来どおり redirect + セッションメッセージ。
 * - curl 等で不正値を送った場合でも、JSPで落ちないように sendError で明示的に 400/500 を返す。
 */
public class AccidentActionServlet extends HttpServlet {
    private final AccidentService accidentService = new AccidentService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ServletPath は web.xml の url-pattern（例：/accidents/status）になります
        String action = getActionFromPath(request.getServletPath());
        String idParam = request.getParameter("id");

        // --- 入力検証（必須/形式） ---
        if (action.isEmpty()) {
            sendBadRequest(response, "不正な操作です。");
            return;
        }
        if (idParam == null || idParam.isBlank()) {
            sendBadRequest(response, "id が指定されていません。");
            return;
        }

        Long accidentId;
        try {
            accidentId = Long.parseLong(idParam);
            if (accidentId <= 0) {
                sendBadRequest(response, "id が不正です。");
                return;
            }
        } catch (NumberFormatException e) {
            // 例: id=abc
            sendBadRequest(response, "id が不正です。");
            return;
        }

        try {
            HttpSession session = request.getSession();

            switch (action) {
                case "status": {
                    String statusParam = request.getParameter("status");
                    if (statusParam == null || statusParam.isBlank()) {
                        sendBadRequest(response, "status が指定されていません。");
                        return;
                    }

                    AccidentStatus newStatus;
                    try {
                        newStatus = AccidentStatus.valueOf(statusParam);
                    } catch (IllegalArgumentException ex) {
                        // 例: status=ABC
                        sendBadRequest(response, "status が不正です。");
                        return;
                    }

                    accidentService.changeStatus(accidentId, newStatus);
                    session.setAttribute("message", "ステータスを変更しました");
                    session.setAttribute("messageType", "success");
                    break;
                }

                case "contacted":
                    accidentService.markContacted(accidentId);
                    session.setAttribute("message", "対応日時を更新しました");
                    session.setAttribute("messageType", "success");
                    break;

                case "memo": {
                    String memo = request.getParameter("memo");
                    if (memo == null) memo = "";

                    // 任意：メモの簡易上限（新規登録の description と同じ思想で「範囲」）
                    if (memo.length() > 500) {
                        sendBadRequest(response, "メモは500文字以内で入力してください。");
                        return;
                    }

                    accidentService.saveMemo(accidentId, memo);
                    session.setAttribute("message", "メモを保存しました");
                    session.setAttribute("messageType", "success");
                    break;
                }

                default:
                    // getActionFromPath で空文字以外が来る設計だが、保険として
                    sendBadRequest(response, "不正な操作です。");
                    return;
            }

            // 正常系：従来どおり詳細へ戻す
            response.sendRedirect(request.getContextPath() + "/accidents/detail?id=" + accidentId);

        } catch (BusinessException e) {
            // 業務例外：400（Bad Request）
            // 画面操作のUXを保つため、ブラウザ利用時は従来のメッセージ+redirectも有効だが、
            // Day51の要件に合わせてHTTPとしては400を返す。
            // （ブラウザ上での表示より、APIとしての整合を優先）
            sendBadRequest(response, e.getMessage());

        } catch (Exception e) {
            // 想定外例外：500
            sendServerError(response, "システムエラーが発生しました。");
        }
    }

    private String getActionFromPath(String path) {
        if (path == null) return "";
        if (path.endsWith("/status")) return "status";
        if (path.endsWith("/contacted")) return "contacted";
        if (path.endsWith("/memo")) return "memo";
        return "";
    }

    private void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    }

    private void sendServerError(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }
}
