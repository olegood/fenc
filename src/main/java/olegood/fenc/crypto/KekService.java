package olegood.fenc.crypto;

import java.security.KeyStore;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Service for loading and managing the Key Encryption Key (KEK) from a keystore. This service is
 * responsible for retrieving and verifying the integrity, type, and algorithm of the active KEK
 * stored in the provided keystore.
 */
@Service
public class KekService {

  /**
   * Represents a Java {@link KeyStore} instance used for securely storing and managing
   * cryptographic keys within the context of the service. The keystore is primarily employed to
   * load and manage the KEK, which is then used for encrypting or decrypting Data Encryption Keys
   * (DEKs) within the broader cryptographic service implementation.
   */
  private final KeyStore keyStore;

  /**
   * Stores the password used to access the keystore. This password is converted to a character
   * array for secure handling in memory and is used during the keystore initialization and
   * retrieval of cryptographic keys.
   */
  private final char[] password;

  /**
   * Represents the alias used to identify the Key Encryption Key (KEK) within the keystore. This
   * alias is used to retrieve the active KEK securely from the keystore for cryptographic
   * operations, including encrypting and decrypting Data Encryption Keys (DEKs).
   */
  private final String alias;

  public KekService(
      @Value("${crypto.kek.keystore-type}") String keyStoreType,
      @Value("${crypto.kek.location}") Resource location,
      @Value("${crypto.kek.password}") String password,
      @Value("${crypto.kek.alias}") String alias) {

    try {
      this.password = password.toCharArray();
      this.alias = alias;

      this.keyStore = KeyStore.getInstance(keyStoreType);

      try (var is = location.getInputStream()) {
        keyStore.load(is, this.password);
      }

      validateActiveKey();

    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize KEK keystore", e);
    }
  }

  private void validateActiveKey() throws Exception {
    var activeKey = keyStore.getKey(alias, password);

    if (!(activeKey instanceof SecretKey sk)) {
      throw new IllegalStateException("KEK is not a SecretKey");
    }

    if (!"AES".equals(sk.getAlgorithm())) {
      throw new IllegalStateException("KEK algorithm must be AES");
    }

    if (sk.getEncoded().length != 32) {
      throw new IllegalStateException("KEK must be 256-bit");
    }
  }

  /**
   * Retrieves the active Key Encryption Key (KEK) securely stored in the keystore. The KEK is
   * validated during initialization to ensure that it meets the required specifications, including
   * being a 256-bit AES key.
   *
   * @return the active {@link SecretKey} representing the KEK used for cryptographic operations,
   *     such as encrypting and decrypting Data Encryption Keys (DEKs).
   * @throws IllegalStateException if the KEK cannot be loaded from the keystore or if an error
   *     occurs during the key retrieval process.
   */
  public SecretKey getActiveKek() {
    try {
      return (SecretKey) keyStore.getKey(alias, password);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load active KEK", e);
    }
  }

  /**
   * Retrieves the alias of the active Key Encryption Key (KEK) stored in the keystore. The alias
   * identifies the KEK used for cryptographic operations, including encrypting and decrypting Data
   * Encryption Keys (DEKs).
   *
   * @return the alias of the active KEK as a {@code String}.
   */
  public String getActiveAlias() {
    return alias;
  }
}
