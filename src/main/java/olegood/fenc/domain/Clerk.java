package olegood.fenc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import olegood.fenc.jpa.ClerkSearchTokensListener;
import olegood.fenc.jpa.EncryptedStringConverter;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@EntityListeners(ClerkSearchTokensListener.class)
@Table(
    name = "CLERK",
    indexes = {
      @Index(name = "idx_customer_search_email", columnList = "search_email"),
      @Index(name = "idx_customer_search_ssn", columnList = "search_ssn")
    })
public class Clerk {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "CLERK_ID")
  private UUID id;

  @Column(name = "NAME")
  private String name;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "EMAIL")
  private String email;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "SSN")
  private String ssn;

  // search

  @Column(name = "SEARCH_EMAIL", nullable = false, updatable = false)
  private String searchEmail;

  @Column(name = "SEARCH_SSN", nullable = false, updatable = false)
  private String searchSsn;
}
