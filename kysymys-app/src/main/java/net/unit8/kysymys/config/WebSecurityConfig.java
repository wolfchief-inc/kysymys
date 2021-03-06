package net.unit8.kysymys.config;

import net.unit8.kysymys.user.application.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Configuration
@Order(1)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/images/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
                .xssProtection();

        http.authorizeRequests()
                .antMatchers("/login", "/login/oauth2", "/signup", "/h2/**", "/token/watch/*", "/actuator/health").permitAll()
                .antMatchers("/admin/lesson/**").hasAuthority("CREATE_PROBLEM")
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    String url = Optional.ofNullable(req.getSession(false))
                            .map(s -> s.getAttribute("SPRING_SECURITY_SAVED_REQUEST"))
                            .filter(SavedRequest.class::isInstance)
                            .map(SavedRequest.class::cast)
                            .map(SavedRequest::getRedirectUrl)
                            .orElse("/");
                    res.sendRedirect(url);
                })
                .failureUrl("/login?error")
                .permitAll();

        http.oauth2Login()
                .loginPage("/login")
                .authorizationEndpoint()
                .baseUri("/oauth2/authorize")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService);


        http.logout()
                .logoutUrl("/logout")
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));

        http.csrf().ignoringAntMatchers("/h2/**", "/token/watch/*");
        http.headers().frameOptions().disable();
    }
}
