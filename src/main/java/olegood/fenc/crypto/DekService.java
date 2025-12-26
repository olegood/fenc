package olegood.fenc.crypto;

import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DekService {

  private static final String SECRET_KEY_ALGORITHM = "AES";

  private final SecureRandom random = new SecureRandom();
  private final CryptoService cryptoService;

  /**
   * Generates a new Data Encryption Key (DEK) securely. The DEK is created as a 256-bit AES key
   * using a cryptographically secure random number generator.
   *
   * @return a newly generated {@link SecretKey} representing the DEK.
   */
  public SecretKey generateDek() {
    return new SecretKeySpec(random.generateSeed(32), SECRET_KEY_ALGORITHM);
  }

  /**
   * Encrypts a Data Encryption Key (DEK) using a Key Encryption Key (KEK) and an initialization
   * vector (IV). The method utilizes AES-GCM mode encryption to produce a secure encrypted
   * representation of the DEK.
   *
   * @param dek the Data Encryption Key to be encrypted, represented as a {@link SecretKey}
   * @param kek the Key Encryption Key used to encrypt the DEK, represented as a {@link SecretKey}
   * @param iv the initialization vector used by the algorithm for encryption, as a byte array
   * @return the encrypted representation of the DEK, as a byte array
   */
  public byte[] encryptDek(SecretKey dek, SecretKey kek, byte[] iv) {
    return cryptoService.encrypt(dek.getEncoded(), kek, iv);
  }

  /**
   * Decrypts an encrypted Data Encryption Key (DEK) using a Key Encryption Key (KEK) and an
   * initialization vector (IV). The method utilizes AES-GCM mode for decryption.
   *
   * @param encryptedDek the encrypted representation of the Data Encryption Key, as a byte array
   * @param kek the Key Encryption Key used to decrypt the DEK, represented as a {@link SecretKey}
   * @param iv the initialization vector (IV) used by the AES-GCM algorithm for decryption, as a
   *     byte array
   * @return the decrypted Data Encryption Key as a {@link SecretKey}
   */
  public SecretKey decryptDek(byte[] encryptedDek, SecretKey kek, byte[] iv) {
    byte[] key = cryptoService.decrypt(encryptedDek, kek, iv);
    return new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
  }

  /**
   * Generates a random initialization vector (IV) for cryptographic operations. The IV is a 12-byte
   * array populated with random values using a cryptographically secure random number generator.
   *
   * @return a new 12-byte array representing the randomly generated initialization vector.
   */
  public byte[] randomIv() {
    var iv = new byte[12];
    random.nextBytes(iv);
    return iv;
  }
}
