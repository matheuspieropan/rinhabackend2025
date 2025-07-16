package org.pieropan.rinhabackend.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pieropan.rinhabackend.dto.PaymentRequest;
import org.pieropan.rinhabackend.service.ProcessorPaymentService;
import org.pieropan.rinhabackend.util.ObjectMapperUtil;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentServlet extends HttpServlet {

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ProcessorPaymentService processorPaymentService = new ProcessorPaymentService();

    private final ObjectMapper objectMapper = ObjectMapperUtil.getObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        PaymentRequest paymentRequest = objectMapper.readValue(req.getReader(), PaymentRequest.class);

        virtualExecutor.submit(() -> processorPaymentService.processPayment(paymentRequest));

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}