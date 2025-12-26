package olegood.fenc.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "DOCUMENT")
public class Document {

  @Id
  @Column(name = "DOCUMENT_ID")
  @GeneratedValue(generator = "UUID")
  private UUID id;

  @Column(name = "ORGANIZATION_CODE", nullable = false)
  private String organizationCode;

  @Column(name = "CREATED_AT")
  private Instant createdAt = Instant.now();

  @Column(name = "RETENTION_UNTIL")
  private Instant retentionUntil;

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
  private List<Attachment> attachments = new ArrayList<>();
}
