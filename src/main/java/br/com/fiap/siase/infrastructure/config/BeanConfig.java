package br.com.fiap.siase.infrastructure.config;

import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CriarOsUC criarOsUC(OrdemServicoRepositoryPort repo,
                                EventPublisherPort pub,
                                EventoOSRepositoryPort eventos) {
        return new CriarOsUC(repo, pub, eventos);
    }

    @Bean
    public ConsultarOsUC consultarOsUC(OrdemServicoRepositoryPort repo) {
        return new ConsultarOsUC(repo);
    }

    @Bean
    public AvancarStatusOsUC avancarStatusOsUC(OrdemServicoRepositoryPort repo,
                                                EventPublisherPort pub,
                                                EventoOSRepositoryPort eventos) {
        return new AvancarStatusOsUC(repo, pub, eventos);
    }

    @Bean
    public CancelarOsUC cancelarOsUC(OrdemServicoRepositoryPort repo,
                                      EventPublisherPort pub,
                                      EventoOSRepositoryPort eventos) {
        return new CancelarOsUC(repo, pub, eventos);
    }

    @Bean
    public AprovarOrcamentoUC aprovarOrcamentoUC(OrdemServicoRepositoryPort repo,
                                                  EventPublisherPort pub,
                                                  EventoOSRepositoryPort eventos) {
        return new AprovarOrcamentoUC(repo, pub, eventos);
    }

    @Bean
    public AtualizarStatusPorEventoUC atualizarStatusPorEventoUC(OrdemServicoRepositoryPort repo,
                                                                   EventoOSRepositoryPort eventos) {
        return new AtualizarStatusPorEventoUC(repo, eventos);
    }
}
