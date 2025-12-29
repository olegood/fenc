package olegood.fenc.web;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  @GetMapping("/me")
  public Me me(Authentication authentication) {
    var principal = (User) authentication.getPrincipal();
    return Me.from(principal);
  }

  record Me(String username, Set<String> authorities) {
    static Me from(User user) {
      Set<String> authorities =
          user.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toSet());
      return new Me(user.getUsername(), authorities);
    }
  }
}
