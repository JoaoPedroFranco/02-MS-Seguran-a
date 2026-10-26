package br.espm.ms.login.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "MS-AV-02 - Seguranca e Observabilidade",
        version = "1.0.0",
        description = """
                API de login de um sistema generico.
                Endpoints publicos: POST /login e PUT /trocarsenha/{userid}.
                POST / e GET / exigem ROLE_MANUTENCAO; PUT /unlock/{userid} exige ROLE_ADMIN
                (autenticacao HTTP Basic - clique em Authorize).
                Usuarios de seguranca padrao: admin/admin123 (ROLE_ADMIN) e manutencao/manut123 (ROLE_MANUTENCAO)."""))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
