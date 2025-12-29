package olegood.fenc.jpa;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import olegood.fenc.domain.Clerk;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ClerkSearchTokensListener {

  private final SearchTokenService searchTokenService;

  @PrePersist
  @PreUpdate
  void beforeAnyUpdate(Clerk clerk) {
    // computeSearchTokens
    clerk.setSearchEmail(searchTokenService.tokenize(clerk.getEmail()));
    clerk.setSearchSsn(searchTokenService.tokenize(clerk.getSsn()));
  }
}
