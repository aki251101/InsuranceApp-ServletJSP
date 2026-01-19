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

public class AccidentActionServlet extends HttpServlet {
    private final AccidentService accidentService = new AccidentService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String action = getActionFromPath(request.getServletPath());
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/accidents");
            return;
        }

        try {
            Long accidentId = Long.parseLong(idParam);
            HttpSession session = request.getSession();

            switch (action) {
                case "status":
                    String statusParam = request.getParameter("status");
                    AccidentStatus newStatus = AccidentStatus.valueOf(statusParam);
                    accidentService.changeStatus(accidentId, newStatus);
                    session.setAttribute("message", "ステータスを変更しました");
                    session.setAttribute("messageType", "success");
                    break;
                    
                case "contacted":
                    accidentService.markContacted(accidentId);
                    session.setAttribute("message", "対応日時を更新しました");
                    session.setAttribute("messageType", "success");
                    break;
                    
                case "memo":
                    String memo = request.getParameter("memo");
                    accidentService.saveMemo(accidentId, memo);
                    session.setAttribute("message", "メモを保存しました");
                    session.setAttribute("messageType", "success");
                    break;
                    
                default:
                    session.setAttribute("message", "不正な操作です");
                    session.setAttribute("messageType", "danger");
            }

            response.sendRedirect(request.getContextPath() + "/accidents/detail?id=" + accidentId);

        } catch (BusinessException e) {
            HttpSession session = request.getSession();
            session.setAttribute("message", e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/accidents/detail?id=" + idParam);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/accidents");
        }
    }

    private String getActionFromPath(String path) {
        if (path.endsWith("/status")) return "status";
        if (path.endsWith("/contacted")) return "contacted";
        if (path.endsWith("/memo")) return "memo";
        return "";
    }
}