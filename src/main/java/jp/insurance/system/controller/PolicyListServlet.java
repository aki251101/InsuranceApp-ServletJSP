package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.insurance.system.model.Policy;
import jp.insurance.system.model.RenewalStats;
import jp.insurance.system.service.PolicyService;
import jp.insurance.system.service.StatsService;
import jp.insurance.system.util.DataInitializer;

import java.io.IOException;
import java.util.List;

public class PolicyListServlet extends HttpServlet {
    private final PolicyService policyService = new PolicyService();
    private final StatsService statsService = new StatsService();

    @Override
    public void init() throws ServletException {
        DataInitializer.initializeIfNeeded();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String query = request.getParameter("q");
        String tab = request.getParameter("tab");
        if (tab == null || tab.isEmpty()) {
            tab = "renewable";
        }

        List<Policy> policies;
        if (query != null && !query.trim().isEmpty()) {
            policies = policyService.searchPolicies(query);
        } else {
            policies = policyService.getAllPolicies();
        }

        policies = policyService.filterByTab(policies, tab);

        RenewalStats fiscalStats = statsService.getFiscalYearStats();
        RenewalStats monthlyStats = statsService.getMonthlyStats();

        request.setAttribute("policies", policies);
        request.setAttribute("currentTab", tab);
        request.setAttribute("query", query);
        request.setAttribute("fiscalStats", fiscalStats);
        request.setAttribute("monthlyStats", monthlyStats);
        request.setAttribute("policyService", policyService);

        request.getRequestDispatcher("/WEB-INF/views/policy/list.jsp").forward(request, response);
    }
}