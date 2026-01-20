package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.model.Policy;
import jp.insurance.system.service.PolicyService;

import java.io.IOException;

public class PolicyDetailServlet extends HttpServlet {
    private final PolicyService policyService = new PolicyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            forwardBadRequest(request, response, "契約IDが指定されていません", null);
            return;
        }

        try {
            Long policyId = Long.parseLong(idParam);
            if (policyId <= 0) {
                forwardBadRequest(request, response, "契約IDが不正です", idParam);
                return;
            }
            if (policyId <= 0) {
                forwardBadRequest(request, response, "契約IDが不正です", idParam);
                return;
            }
            if (policyId <= 0) {
                forwardBadRequest(request, response, "契約IDが不正です", idParam);
                return;
            }

            Policy policy = policyService.getPolicyById(policyId);

            HttpSession session = request.getSession();
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            session.removeAttribute("message");
            session.removeAttribute("messageType");

            request.setAttribute("policy", policy);
            request.setAttribute("message", message);
            request.setAttribute("messageType", messageType);
            request.setAttribute("policyService", policyService);

            request.getRequestDispatcher("/WEB-INF/views/policy/detail.jsp")
                    .forward(request, response);

        } catch (NumberFormatException e) {
            // id=abc のように数値へ変換できない場合は 400 Bad Request
            forwardBadRequest(request, response, "契約IDが数値ではありません", idParam);

        } catch (BusinessException e) {
            // 存在しないIDは 404 Not Found
            if ("契約が見つかりません".equals(e.getMessage())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("title", "契約が見つかりません");
                request.setAttribute("active", "policies");
                request.setAttribute("message", "契約が見つかりません（ID: " + idParam + "）");
                request.setAttribute("backUrl", request.getContextPath() + "/policies");
                request.getRequestDispatcher("/WEB-INF/views/common/notFound.jsp")
                        .forward(request, response);
                return;
            }

            // その他の業務エラーは従来通り
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/policy/list.jsp")
                    .forward(request, response);
        }
    }

    /**
     * 400 Bad Request を返し、共通の notFound.jsp を使ってユーザーにも分かる形で表示する。
     */
    private void forwardBadRequest(HttpServletRequest request, HttpServletResponse response,
                                   String reason, String rawId)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String idLabel = (rawId == null || rawId.isBlank()) ? "未指定" : rawId;

        request.setAttribute("title", "不正なリクエスト");
        request.setAttribute("active", "policies");
        request.setAttribute("message", reason + "（id: " + idLabel + "）");
        request.setAttribute("backUrl", request.getContextPath() + "/policies");

        request.getRequestDispatcher("/WEB-INF/views/common/notFound.jsp")
                .forward(request, response);
    }
}
