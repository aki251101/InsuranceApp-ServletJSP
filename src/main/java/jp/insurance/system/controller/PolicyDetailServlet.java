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
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/policies");
            return;
        }

        try {
            Long policyId = Long.parseLong(idParam);
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
            response.sendRedirect(request.getContextPath() + "/policies");
        } catch (BusinessException e) {
            // 存在しないIDは 404 相当にする
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
}
