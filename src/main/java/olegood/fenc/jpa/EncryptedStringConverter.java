package olegood.fenc.jpa;

import com.google.common.primitives.Bytes;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import olegood.fenc.crypto.CryptoService;
import olegood.fenc.crypto.DekService;
import olegood.fenc.crypto.kek.KeyStoreService;

@RequiredArgsConstructor
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

  private final KeyStoreService keyStoreService;
  private final CryptoService cryptoService;
  private final DekService dekService;

  @Override
  public String convertToDatabaseColumn(String attribute) {
    return attribute == null ? null : encrypt(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return decrypt(dbData);
  }

  public String encrypt(String plainText) {
    try {
      String keyAlias = keyStoreService.getActiveCekAlias();
      SecretKey cek = keyStoreService.getActiveCek();

      byte[] iv = dekService.randomIv();

      Cipher cipher = cryptoService.initCipher(Cipher.ENCRYPT_MODE, cek, iv);

      byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      String payload = Base64.getEncoder().encodeToString(Bytes.concat(iv, encrypted));

      return keyAlias + ":" + payload;
    } catch (Exception e) {
      throw new IllegalStateException("Encryption failed", e);
    }
  }

  public String decrypt(String cipherText) {
    try {
      String[] parts = cipherText.split(":", 2);
      String keyAlias = parts[0];
      byte[] decoded = Base64.getDecoder().decode(parts[1]);

      SecretKey cek = keyStoreService.loadKekByAlias(keyAlias);

      byte[] iv = Arrays.copyOfRange(decoded, 0, 12);
      byte[] payload = Arrays.copyOfRange(decoded, 12, decoded.length);

      Cipher cipher = cryptoService.initCipher(Cipher.DECRYPT_MODE, cek, iv);
      return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Decryption failed", e);
    }
  }
}
