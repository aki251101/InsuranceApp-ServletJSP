package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.model.Accident;
import jp.insurance.system.service.AccidentService;

import java.io.IOException;

public class AccidentDetailServlet extends HttpServlet {
    private final AccidentService accidentService = new AccidentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/accidents");
            return;
        }

        try {
            Long accidentId = Long.parseLong(idParam);
            Accident accident = accidentService.getAccidentById(accidentId);

            HttpSession session = request.getSession();
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            session.removeAttribute("message");
            session.removeAttribute("messageType");

            request.setAttribute("accident", accident);
            request.setAttribute("message", message);
            request.setAttribute("messageType", messageType);
            request.setAttribute("accidentService", accidentService);

            request.getRequestDispatcher("/WEB-INF/views/accident/detail.jsp")
                    .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/accidents");
        } catch (BusinessException e) {
            // 存在しないIDは 404 相当にする
            if ("事故が見つかりません".equals(e.getMessage())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("title", "事故が見つかりません");
                request.setAttribute("active", "accidents");
                request.setAttribute("message", "事故が見つかりません（ID: " + idParam + "）");
                request.setAttribute("backUrl", request.getContextPath() + "/accidents");
                request.getRequestDispatcher("/WEB-INF/views/common/notFound.jsp")
                        .forward(request, response);
                return;
            }

            // その他の業務エラーは従来通り
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/accident/list.jsp")
                    .forward(request, response);
        }
    }
}
