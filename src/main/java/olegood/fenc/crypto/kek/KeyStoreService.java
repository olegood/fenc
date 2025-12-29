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
public class KeyStoreService {

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

  private final Alias alias;

  public KeyStoreService(KeyStoreProps keystore, Alias alias) {
    password = keystore.password().toCharArray();
    this.alias = alias;

    try (var is = keystore.location().getInputStream()) {
      keyStore = KeyStore.getInstance(keystore.type());
      keyStore.load(is, password);

      loadKekByAlias(alias.kek());
      loadKekByAlias(alias.cek());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load KEK keystore: " + keystore.location(), e);
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
      return loadKekByAlias(alias.kek());
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
  public String getActiveKekAlias() {
    return alias.kek();
  }

  public SecretKey getActiveCek() {
    try {
      return loadKekByAlias(alias.cek());
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load active CEK", e);
    }
  }

  public String getActiveCekAlias() {
    return alias.cek();
  }

  public SecretKey loadKekByAlias(String alias) throws Exception {
    if (!keyStore.containsAlias(alias)) {
      throw new IllegalStateException("Alias `" + alias + "` not found in keystore");
    }

    var key = keyStore.getKey(alias, password);

    if (!(key instanceof SecretKey kek)) {
      throw new IllegalStateException("Alias `" + alias + "` is not a SecretKey");
    }

    if (!"AES".equals(kek.getAlgorithm())) {
      throw new IllegalStateException("Alias `" + alias + "` algorithm must be AES");
    }

    if (kek.getEncoded().length != 32) {
      throw new IllegalStateException("KEK must be 256-bit");
    }

    return kek;
  }
}
