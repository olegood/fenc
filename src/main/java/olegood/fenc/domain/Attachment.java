package olegood.fenc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "ATTACHMENT")
public class Attachment {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "ATTACHMENT_ID")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "DOCUMENT_ID")
  private Document document;

  @Column(name = "FILE_NAME")
  private String fileName;

  @Column(name = "LOCATION")
  private String location;

  @Lob
  @Column(name = "ENCRYPTED_DEK")
  private byte[] encryptedDek;

  @Column(name = "DEK_VERSION")
  private String dekVersion;

  @Column(name = "KEK_VERSION")
  private String kekVersion;

  @Column(name = "IV", length = 12)
  private byte[] iv;

  @Column(name = "CREATED_AT")
  private Instant createdAt = Instant.now();
}
