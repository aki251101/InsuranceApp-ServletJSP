package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.insurance.system.exception.BusinessException;
import jp.insurance.system.service.PolicyService;

import java.io.IOException;
import java.time.LocalDate;

public class PolicyNewServlet extends HttpServlet {
    private final PolicyService policyService = new PolicyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/policy/new.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String policyNumber = request.getParameter("policyNumber");
        String customerName = request.getParameter("customerName");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            policyService.createPolicy(policyNumber, customerName, startDate, endDate);

            response.sendRedirect(request.getContextPath() + "/policies");

        } catch (BusinessException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("policyNumber", policyNumber);
            request.setAttribute("customerName", customerName);
            request.setAttribute("startDate", startDateStr);
            request.setAttribute("endDate", endDateStr);
            request.getRequestDispatcher("/WEB-INF/views/policy/new.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "入力内容に誤りがあります");
            request.getRequestDispatcher("/WEB-INF/views/policy/new.jsp")
                   .forward(request, response);
        }
    }
}