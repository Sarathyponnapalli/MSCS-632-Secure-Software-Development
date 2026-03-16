package secureportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder()
                .username("employee")
                .password(encoder.encode("Emp@2024Secure"))
                .roles("USER").build();

        UserDetails admin = User.builder()
                .username("hradmin")
                .password(encoder.encode("Admin@2024Secure"))
                .roles("ADMIN", "USER").build();

        UserDetails manager = User.builder()
                .username("manager")
                .password(encoder.encode("Mgr@2024Secure"))
                .roles("MANAGER", "USER").build();

        return new InMemoryUserDetailsManager(user, admin, manager);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(ch -> ch.anyRequest().requiresSecure())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/error").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/employees/**")
                    .hasAnyRole("USER", "ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/employees/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**")
                    .hasRole("ADMIN")
                .requestMatchers("/employees")
                    .hasAnyRole("USER", "ADMIN", "MANAGER")
                .requestMatchers("/employees/add", "/employees/edit/**",
                                 "/employees/save", "/employees/delete/**")
                    .hasRole("ADMIN")
                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll())

            .httpBasic(basic -> {})

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())

            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }
}
