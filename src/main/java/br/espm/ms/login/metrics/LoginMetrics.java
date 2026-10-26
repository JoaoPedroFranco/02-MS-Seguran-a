package br.espm.ms.login.metrics;

import br.espm.ms.login.repository.LoginAttemptRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Indicadores expostos em /actuator/prometheus:
 *   login_validos_total          -> quantidade de logins validos
 *   login_invalidos_total        -> quantidade de logins invalidos
 *   login_usuarios_bloqueados    -> total de usuarios bloqueados (lido da tabela login_attempts)
 */
@Component
public class LoginMetrics {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetrics(MeterRegistry registry, LoginAttemptRepository attempts) {
        this.loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins validos")
                .register(registry);
        this.loginsInvalidos = Counter.builder("login.invalidos")
                .description("Quantidade de logins invalidos (senha errada, usuario inexistente ou bloqueado)")
                .register(registry);
        Gauge.builder("login.usuarios.bloqueados", attempts, LoginAttemptRepository::contarBloqueados)
                .description("Total de usuarios bloqueados")
                .register(registry);
    }

    public void registrarValido() {
        loginsValidos.increment();
    }

    public void registrarInvalido() {
        loginsInvalidos.increment();
    }
}
