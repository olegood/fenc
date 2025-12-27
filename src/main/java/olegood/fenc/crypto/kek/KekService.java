package olegood.fenc.crypto.kek;

import java.security.KeyStore;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Service for loading and managing the Key Encryption Key (KEK) from a keystore. This service is
 * responsible for retrieving and verifying the integrity, type, and algorithm of the active KEK
 * stored in the provided keystore.
 */
@Service
public class KekService {

  /**
   * Represents the {@link KeyStore} instance used for securely storing and managing cryptographic
   * keys, including the Key Encryption Key (KEK) used in encryption and decryption processes.
   */
  private final KeyStore keyStore;

  /**
   * Represents the password used to unlock the {@link KeyStore} and retrieve cryptographic keys.
   * Note: The password is stored as a character array to minimize security risks associated with
   * string immutability and to facilitate secure erasure of sensitive data from memory.
   */
  private final char[] password;

  /**
   * Represents the alias used to identify the Key Encryption Key (KEK) within the keystore. This
   * alias is used to retrieve the active KEK securely from the keystore for cryptographic
   * operations, including encrypting and decrypting Data Encryption Keys (DEKs).
   */
  private final String alias;

  public KekService(KekProperties kekProps) {
    password = kekProps.password().toCharArray();
    alias = kekProps.alias();

    try (var is = kekProps.location().getInputStream()) {
      keyStore = KeyStore.getInstance(kekProps.keyStoreType());
      keyStore.load(is, password);

      loadKekByAlias(alias);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load KEK keystore: " + kekProps.location(), e);
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
      return loadKekByAlias(alias);
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

  public SecretKey loadKekByAlias(String alias) throws Exception {
    if (!keyStore.containsAlias(alias)) {
      throw new IllegalStateException("KEK `" + alias + "` not found in keystore");
    }

    var key = keyStore.getKey(alias, password);

    if (!(key instanceof SecretKey kek)) {
      throw new IllegalStateException("KEK is not a SecretKey");
    }

    if (!"AES".equals(kek.getAlgorithm())) {
      throw new IllegalStateException("KEK algorithm must be AES");
    }

    if (kek.getEncoded().length != 32) {
      throw new IllegalStateException("KEK must be 256-bit");
    }

    return kek;
  }
}
