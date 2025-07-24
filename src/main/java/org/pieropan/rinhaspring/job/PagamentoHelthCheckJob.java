package org.pieropan.rinhaspring.job;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.HealthResponse;
import org.pieropan.rinhaspring.model.MelhorOpcao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PagamentoHelthCheckJob {

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    private final PagamentoProcessorManualClient pagamentoProcessorFallback;

    public static MelhorOpcao melhorOpcao = new MelhorOpcao(true);

    public PagamentoHelthCheckJob(@Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                  @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.pagamentoProcessorFallback = pagamentoProcessorFallback;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5100)
    public void checaProcessadorDefault() {
        HealthResponse healthResponseDefault = null;
        HealthResponse healthResponseFallback = null;

        try {
            healthResponseDefault = pagamentoProcessorDefault.healthCheck();
        } catch (Exception ignored) {
        }

        try {
            healthResponseFallback = pagamentoProcessorFallback.healthCheck();
        } catch (Exception ignored) {
        }

        if (healthResponseDefault == null && healthResponseFallback == null) {
            melhorOpcao = null;
            return;
        }

        boolean ambosResponderam = healthResponseDefault != null && healthResponseFallback != null;

        if (ambosResponderam) {

            boolean defaultDisponivel = !healthResponseDefault.failing();
            boolean fallbackDisponivel = !healthResponseFallback.failing();

            if (defaultDisponivel && fallbackDisponivel) {

                int tempoDefault = healthResponseDefault.minResponseTime();
                int tempoFallback = healthResponseFallback.minResponseTime();

                if (tempoDefault < tempoFallback) {
                    melhorOpcao = new MelhorOpcao(true);
                } else if (tempoFallback < tempoDefault) {
                    melhorOpcao = new MelhorOpcao(false);
                } else {
                    melhorOpcao = new MelhorOpcao(true);
                }

            } else if (defaultDisponivel) {

                melhorOpcao = new MelhorOpcao(true);

            } else if (fallbackDisponivel) {

                melhorOpcao = new MelhorOpcao(false);
            } else {

                melhorOpcao = null;
            }

        } else if (healthResponseDefault != null && !healthResponseDefault.failing()) {

            melhorOpcao = new MelhorOpcao(true);

        } else if (healthResponseFallback != null && !healthResponseFallback.failing()) {
            melhorOpcao = new MelhorOpcao(false);

        } else {

            melhorOpcao = null;
        }
    }
}