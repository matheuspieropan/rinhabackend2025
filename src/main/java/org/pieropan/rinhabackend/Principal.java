package org.pieropan.rinhabackend;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import org.pieropan.rinhabackend.servlet.PaymentServlet;
import org.pieropan.rinhabackend.servlet.PaymentSummaryServlet;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

public class Principal {

    public static void main(String[] args) throws ServletException {
        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(Principal.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("myapp.war")
                .addServlets(
                        servlet("PaymentServlet", PaymentServlet.class)
                                .addMapping("/payments"),
                        servlet("PaymentSummaryServlet", PaymentSummaryServlet.class)
                                .addMapping("/payments-summary")
                );

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        PathHandler path = new PathHandler(manager.start());

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(path)
                .build();

        server.start();
    }
}