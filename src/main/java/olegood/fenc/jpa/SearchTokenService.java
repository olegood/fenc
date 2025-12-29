package olegood.fenc.jpa;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchTokenService {

  private static final String ALGORITHM = "HmacSHA256";

  private final Mac mac;

  public SearchTokenService(@Value("${crypto.search-key}") String secret) {
    try {
      mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public String tokenize(String value) {
    if (value == null) {
      return null;
    }

    byte[] raw = mac.doFinal(value.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(raw);
  }
}
