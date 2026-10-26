package br.espm.ms.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())   // API REST stateless, sem formulario/cookie de sessao
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // publicos
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/error").permitAll()
                // protegidos por role
                .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Usuarios/roles de seguranca persistidos nas tabelas users e authorities. */
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    /** Cria o administrador (ROLE_ADMIN) e o usuario de manutencao (ROLE_MANUTENCAO) na subida. */
    @Bean
    public CommandLineRunner criarUsuariosDeSeguranca(
            JdbcUserDetailsManager manager,
            PasswordEncoder encoder,
            @Value("${app.seguranca.admin.username}") String adminUser,
            @Value("${app.seguranca.admin.senha}") String adminSenha,
            @Value("${app.seguranca.manutencao.username}") String manutUser,
            @Value("${app.seguranca.manutencao.senha}") String manutSenha) {
        return args -> {
            if (!manager.userExists(adminUser)) {
                manager.createUser(User.withUsername(adminUser)
                        .password(encoder.encode(adminSenha))
                        .roles("ADMIN")
                        .build());
            }
            if (!manager.userExists(manutUser)) {
                manager.createUser(User.withUsername(manutUser)
                        .password(encoder.encode(manutSenha))
                        .roles("MANUTENCAO")
                        .build());
            }
        };
    }
}
