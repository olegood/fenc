package olegood.fenc.batch.clerk;

import olegood.fenc.domain.Clerk;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ClerkReEncryptionProcessor implements ItemProcessor<Clerk, Clerk> {

  @Override
  public Clerk process(Clerk clerk) {
    // decrypted plaintext already present in entity
    clerk.setEmail(new String(clerk.getEmail()));
    clerk.setSsn(new String(clerk.getSsn()));
    return clerk;
  }
}
