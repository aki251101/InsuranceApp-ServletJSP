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

        // Day50（合意した仕様：検索＝全件検索モード）
        // 1) q がある（検索した）場合は、タブは必ず all として扱う。
        //    →「検索したら勝手に更新可能契約になる」問題を根本から消す。
        // 2) q がない通常表示の場合は、tab 未指定なら renewable（更新可能契約）をデフォルトにする。
        boolean hasQuery = (query != null && !query.trim().isEmpty());
        if (hasQuery) {
            tab = "all";
        } else {
            if (tab == null || tab.isEmpty()) {
                tab = "renewable";
            }
        }

        List<Policy> policies;
        if (hasQuery) {
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