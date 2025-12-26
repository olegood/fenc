package olegood.fenc.conf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserManagementConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var demi =
        User.withUsername("demi")
            .password(passwordEncoder().encode("demi"))
            .roles("USER", "AUDITOR")
            .build();

    var jade =
        User.withUsername("jade")
            .password(passwordEncoder().encode("jade"))
            .roles("KEY_MANAGEMENT_ADMIN")
            .build();

    var gwen =
        User.withUsername("gwen")
            .password(passwordEncoder().encode("gwen"))
            .roles("KEY_MANAGEMENT_ADMIN", "BREAK_GLASS")
            .build();
    return new InMemoryUserDetailsManager(demi, jade, gwen);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
