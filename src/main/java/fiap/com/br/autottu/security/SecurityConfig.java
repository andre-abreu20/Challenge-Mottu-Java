package fiap.com.br.autottu.security;

import fiap.com.br.autottu.domain.service.UsuarioDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailService usuarioDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // fonte de usuários (banco)
                .userDetailsService(usuarioDetailService)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/logout-success", "/acesso_negado",
                                "/webjars/**", "/css/**", "/js/**", "/images/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/motos/**", "/checkins/**", "/slots/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/login")
                        // Se o seu formulário usa <input name="username"> e <input name="password">, não precisa mudar.
                        // Se quiser usar "email", descomente a linha abaixo e troque o name no form.
                        // .usernameParameter("email")
                        .defaultSuccessUrl("/motos", true)
                        .failureUrl("/login?falha=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((req, res, ex1) -> res.sendRedirect("/acesso_negado"))
                );

        // Mantemos CSRF ativo (recomendado). Inclua o token nos formulários.
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioDetailService uds) {
        // expõe o mesmo bean explicitamente (opcional, mas deixa claro)
        return uds;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}