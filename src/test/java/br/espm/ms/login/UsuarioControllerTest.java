package br.espm.ms.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureObservability   // em testes o Spring Boot desliga o /actuator/prometheus por padrao
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limparBase() {
        jdbc.update("DELETE FROM login_attempts");
        jdbc.update("DELETE FROM usuarios");
        jdbc.update("INSERT INTO usuarios (username, senha) VALUES ('teste', 'senha1')");
    }

    private ResultActions login(String user, String senha) throws Exception {
        return mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user + "\",\"senha\":\"" + senha + "\"}"));
    }

    @Test
    void loginValidoRetorna200() throws Exception {
        login("teste", "senha1").andExpect(status().isOk());
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        login("teste", "errada").andExpect(status().isUnauthorized());
        login("naoexiste", "x").andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueiaAposMaisDeUmaFalha() throws Exception {
        login("teste", "errada").andExpect(status().isUnauthorized());
        login("teste", "errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", containsString("bloqueado")));
        // mesmo com a senha correta continua bloqueado
        login("teste", "senha1").andExpect(status().isUnauthorized());
        Boolean locked = jdbc.queryForObject(
                "SELECT locked FROM login_attempts WHERE username='teste'", Boolean.class);
        org.junit.jupiter.api.Assertions.assertTrue(locked);
    }

    @Test
    void loginValidoZeraFalhaAnterior() throws Exception {
        login("teste", "errada").andExpect(status().isUnauthorized());
        login("teste", "senha1").andExpect(status().isOk());
        login("teste", "errada").andExpect(status().isUnauthorized());
        login("teste", "senha1").andExpect(status().isOk());   // nao bloqueou
    }

    @Test
    void trocarSenha() throws Exception {
        mvc.perform(put("/trocarsenha/teste").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"errada\",\"nova\":\"nova1\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/trocarsenha/teste").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"senha1\",\"nova\":\"nova1\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/trocarsenha/ninguem").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"a\",\"nova\":\"b\"}"))
                .andExpect(status().isNotFound());
        login("teste", "nova1").andExpect(status().isOk());
    }

    @Test
    void cadastroExigeRoleManutencao() throws Exception {
        String body = "{\"username\":\"novo\",\"senha\":\"abc\"}";
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/").with(httpBasic("manutencao", "manut123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/").with(httpBasic("manutencao", "manut123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        login("novo", "abc").andExpect(status().isOk());
    }

    @Test
    void listagemExigeRoleManutencao() throws Exception {
        mvc.perform(get("/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/").with(httpBasic("admin", "admin123"))).andExpect(status().isForbidden());
        mvc.perform(get("/").with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("teste"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void unlockExigeRoleAdmin() throws Exception {
        login("teste", "errada");
        login("teste", "errada");
        mvc.perform(put("/unlock/teste")).andExpect(status().isUnauthorized());
        mvc.perform(put("/unlock/teste").with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/unlock/teste").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
        login("teste", "senha1").andExpect(status().isOk());
    }

    @Test
    void indicadoresExpostosNoPrometheus() throws Exception {
        login("teste", "errada");
        login("teste", "errada");
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("login_validos_total")))
                .andExpect(content().string(containsString("login_invalidos_total")))
                .andExpect(content().string(containsString("login_usuarios_bloqueados{application=\"ms-av-02-login\"} 1.0")));
    }

    @Test
    void swaggerDisponivel() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(content().string(containsString("/trocarsenha/{userid}")));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
