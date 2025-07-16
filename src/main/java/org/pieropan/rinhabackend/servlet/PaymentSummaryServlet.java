package org.pieropan.rinhabackend.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pieropan.rinhabackend.dto.PaymentSummaryResponse;
import org.pieropan.rinhabackend.service.PaymentSummaryService;

public class PaymentSummaryServlet extends HttpServlet {

    private final PaymentSummaryService paymentSummaryService = new PaymentSummaryService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            try {
                String from = req.getParameter("from");
                String to = req.getParameter("to");

                PaymentSummaryResponse response = paymentSummaryService.paymentSummary(from, to);
                String json = objectMapper.writeValueAsString(response);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(json);
            } catch (Exception ignored) {
            }
            asyncContext.complete();
        });
    }
}