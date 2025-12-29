package olegood.fenc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import olegood.fenc.jpa.SearchTokenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class DevLogs implements CommandLineRunner {

  private final SearchTokenService searchTokenService;

  @Override
  public void run(String... args) {
    var email = "bob@yahoo.com";
    log.info("tokenize({})=`{}`", email, searchTokenService.tokenize(email));
  }
}
