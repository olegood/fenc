package olegood.fenc.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32C;
import org.springframework.stereotype.Service;

@Service
public class ChecksumService {

  public long compute(byte[] bytes) {
    var checksum = new CRC32C();
    checksum.update(bytes);
    return checksum.getValue();
  }

  public long compute(Path path) throws IOException {
    var crc32c = new CRC32C();
    try (var in = Files.newInputStream(path)) {
      updateChecksum(crc32c, in);
    }
    return crc32c.getValue();
  }

  private static final int BUFFER_SIZE = 8192;

  private void updateChecksum(CRC32C crc32c, InputStream in) throws IOException {
    var buffer = new byte[BUFFER_SIZE];
    int read;
    while ((read = in.read(buffer)) != -1) {
      crc32c.update(buffer, 0, read);
    }
  }
}
