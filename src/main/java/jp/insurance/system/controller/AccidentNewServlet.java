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
import java.util.List;

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

        try {
            Long policyId = Long.parseLong(policyIdStr);
            LocalDate occurredAt = LocalDate.parse(occurredAtStr);

            accidentService.createAccident(policyId, occurredAt, place, description);

            response.sendRedirect(request.getContextPath() + "/accidents");

        } catch (BusinessException e) {
            List<Policy> policies = policyService.getAllPolicies();
            request.setAttribute("policies", policies);
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("policyId", policyIdStr);
            request.setAttribute("occurredAt", occurredAtStr);
            request.setAttribute("place", place);
            request.setAttribute("description", description);
            request.getRequestDispatcher("/WEB-INF/views/accident/new.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            List<Policy> policies = policyService.getAllPolicies();
            request.setAttribute("policies", policies);
            request.setAttribute("errorMessage", "入力内容に誤りがあります");
            request.getRequestDispatcher("/WEB-INF/views/accident/new.jsp")
                   .forward(request, response);
        }
    }
}