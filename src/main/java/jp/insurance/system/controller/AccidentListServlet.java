package jp.insurance.system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.insurance.system.model.Accident;
import jp.insurance.system.service.AccidentService;

import java.io.IOException;
import java.util.List;

public class AccidentListServlet extends HttpServlet {
    private final AccidentService accidentService = new AccidentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String tab = request.getParameter("tab");
        if (tab == null || tab.isEmpty()) {
            tab = "active";
        }

        List<Accident> accidents = accidentService.getAccidentsByTab(tab);

        request.setAttribute("accidents", accidents);
        request.setAttribute("currentTab", tab);
        request.setAttribute("accidentService", accidentService);

        request.getRequestDispatcher("/WEB-INF/views/accident/list.jsp")
               .forward(request, response);
    }
}