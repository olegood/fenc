package olegood.fenc.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

  /**
   * Encrypts the given plaintext using AES-GCM mode with the specified encryption key and
   * initialization vector.
   *
   * @param plaintext the plaintext data to be encrypted, as a byte array
   * @param key the secret key used for encryption, of type {@link SecretKey}
   * @param iv the initialization vector (IV) used by the AES-GCM algorithm, as a byte array
   * @return the encrypted ciphertext as a byte array
   */
  public byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) {
    return getBytes(Cipher.ENCRYPT_MODE, key, iv, plaintext);
  }

  /**
   * Decrypts the given ciphertext using AES-GCM mode with the specified decryption key and
   * initialization vector.
   *
   * @param ciphertext the encrypted data to be decrypted, as a byte array
   * @param key the secret key used for decryption, of type {@link SecretKey}
   * @param iv the initialization vector (IV) used by the AES-GCM algorithm, as a byte array
   * @return the decrypted plaintext as a byte array
   */
  public byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv) {
    return getBytes(Cipher.DECRYPT_MODE, key, iv, ciphertext);
  }

  private byte[] getBytes(int operationMode, SecretKey key, byte[] iv, byte[] source) {
    try {
      var cipher = initCipher(operationMode, key, iv);
      return cipher.doFinal(source);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Initializes a {@link Cipher} instance for cryptographic operations using AES-GCM mode with no
   * padding. The method sets up the cipher with the specified operation mode, encryption key, and
   * initialization vector (IV).
   *
   * @param operationMode the operation mode for the cipher, either {@link Cipher#ENCRYPT_MODE} or
   *     {@link Cipher#DECRYPT_MODE}.
   * @param key the secret key to be used for encryption or decryption, of type {@link SecretKey}.
   * @param iv the initialization vector (IV) to be used by the AES-GCM algorithm, as a byte array.
   * @return a configured {@link Cipher} instance ready for encryption or decryption.
   * @throws GeneralSecurityException if an error occurs while initializing the cipher.
   */
  public Cipher initCipher(int operationMode, SecretKey key, byte[] iv)
      throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(operationMode, key, new GCMParameterSpec(128, iv));
    return cipher;
  }
}
