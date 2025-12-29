package olegood.fenc.web;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import olegood.fenc.domain.Clerk;
import olegood.fenc.jpa.SearchTokenService;
import olegood.fenc.repository.ClerkRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/clerks")
public class ClerkController {

  private final SearchTokenService tokenService;
  private final ClerkRepository repository;

  @PostMapping
  public Clerk create(@RequestBody Clerk cler) {
    return repository.save(cler);
  }

  @GetMapping("/{id}")
  public Clerk get(@PathVariable UUID id) {
    return repository.findById(id).orElseThrow();
  }

  @GetMapping("/search")
  public Clerk search(@RequestParam String email) {
    var token = tokenService.tokenize(email);
    return repository.findBySearchEmail(token).orElseThrow();
  }
}
