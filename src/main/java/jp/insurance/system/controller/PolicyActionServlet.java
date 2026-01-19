package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.service.PolicyService;

import java.io.IOException;

public class PolicyActionServlet extends HttpServlet {
    private final PolicyService policyService = new PolicyService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = getActionFromPath(request.getServletPath());
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/policies");
            return;
        }

        try {
            Long policyId = Long.parseLong(idParam);
            HttpSession session = request.getSession();

            switch (action) {
                case "renew":
                    policyService.renewPolicy(policyId);
                    session.setAttribute("message", "更新しました");
                    session.setAttribute("messageType", "success");
                    break;
                case "unrenew":
                    policyService.unrenewPolicy(policyId);
                    session.setAttribute("message", "更新を取り消しました");
                    session.setAttribute("messageType", "success");
                    break;
                case "cancel":
                    policyService.cancelPolicy(policyId);
                    session.setAttribute("message", "解約しました");
                    session.setAttribute("messageType", "success");
                    break;
                case "uncancel":
                    policyService.uncancelPolicy(policyId);
                    session.setAttribute("message", "解約を取り消しました");
                    session.setAttribute("messageType", "success");
                    break;
                default:
                    session.setAttribute("message", "不正な操作です");
                    session.setAttribute("messageType", "danger");
            }

            response.sendRedirect(request.getContextPath() + "/policies/detail?id=" + policyId);

        } catch (BusinessException e) {
            HttpSession session = request.getSession();
            session.setAttribute("message", e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/policies/detail?id=" + idParam);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/policies");
        }
    }

    private String getActionFromPath(String path) {
        if (path.endsWith("/renew")) return "renew";
        if (path.endsWith("/unrenew")) return "unrenew";
        if (path.endsWith("/cancel")) return "cancel";
        if (path.endsWith("/uncancel")) return "uncancel";
        return "";
    }
}