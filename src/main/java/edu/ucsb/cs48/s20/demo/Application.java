package edu.ucsb.cs48.s20.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:secrets-localhost.properties", ignoreResourceNotFound = true)
@SpringBootApplication
public class Application extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .authorizeRequests()
        .antMatchers("/","/login**","/webjars/**","/error**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .oauth2Login().loginPage("/login")
        .and()
        .logout()
        .deleteCookies("remove")
        .invalidateHttpSession(true)
        .logoutUrl("/logout")
        .logoutSuccessUrl("/")
        .permitAll();
    }

}
