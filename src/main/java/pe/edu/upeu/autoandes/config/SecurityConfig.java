package pe.edu.upeu.autoandes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pe.edu.upeu.autoandes.security.JwtAuthenticationFilter;
import pe.edu.upeu.autoandes.security.RestAccessDeniedHandler;
import pe.edu.upeu.autoandes.security.RestAuthenticationEntryPoint;
import pe.edu.upeu.autoandes.security.UsuarioDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final UsuarioDetailsService usuarioDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Peticiones OPTIONS para preflight de CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rutas publicas: Login y documentacion OpenAPI
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Perfil de usuario autenticado
                        .requestMatchers("/api/v1/auth/me").authenticated()

                        // Clientes
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAuthority("VEHICULO_LEER")

                        // Servicios
                        .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**").hasAuthority("SERVICIO_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/servicios").hasAuthority("SERVICIO_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/servicios/**").hasAuthority("SERVICIO_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/servicios/**").hasAuthority("SERVICIO_ESCRIBIR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/servicios/*/estado").hasAuthority("SERVICIO_ESCRIBIR")

                        // Vehiculos
                        .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos/**").hasAuthority("VEHICULO_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/vehiculos").hasAuthority("VEHICULO_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/vehiculos/**").hasAuthority("VEHICULO_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/vehiculos/**").hasAuthority("VEHICULO_ESCRIBIR")

                        // Ordenes de Servicio
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").hasAuthority("ORDEN_REGISTRAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/**").hasAuthority("ORDEN_LEER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/anular").hasAuthority("ORDEN_ANULAR")

                        // Reportes
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/**").hasAuthority("REPORTE_VER")

                        // Cualquier otra peticion requiere autenticacion
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
