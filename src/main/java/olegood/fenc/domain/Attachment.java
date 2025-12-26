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
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;

@Accessors(chain = true)
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

  @Column(name = "FILE_IV", length = 12, nullable = false)
  private byte[] fileIv;

  @Column(name = "DEK_IV", length = 12, nullable = false)
  private byte[] dekIv;

  @Lob
  @JdbcTypeCode(java.sql.Types.BINARY)
  @Column(name = "ENCRYPTED_DEK", columnDefinition = "BYTEA", nullable = false)
  private byte[] encryptedDek;

  @Column(name = "DEK_COMPROMISED", nullable = false)
  private boolean dekCompromised;

  @Column(name = "KEK_VERSION")
  private String kekVersion;

  @Column(name = "CREATED_AT")
  private Instant createdAt = Instant.now();
}
