package olegood.fenc.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import olegood.fenc.crypto.CryptoService;
import olegood.fenc.crypto.DekService;
import olegood.fenc.crypto.KekService;
import olegood.fenc.domain.Attachment;
import olegood.fenc.repository.AttachmentRepository;
import olegood.fenc.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@RequiredArgsConstructor
@Service
public class FileStorageImpl implements FileStorage {

  private final DocumentRepository documentRepository;
  private final AttachmentRepository attachmentRepository;

  private final CryptoService cryptoService;
  private final DekService dekService;
  private final KekService kekService;

  @Override
  public UUID store(UUID documentId, MultipartFile file) throws Exception {
    if (!"application/pdf".equals(file.getContentType())) {
      throw new IllegalArgumentException("File storage accepts `PDF` only");
    }

    var document = documentRepository.findById(documentId).orElseThrow();

    var dek = dekService.generateDek();
    var iv = dekService.randomIv();

    var encryptedContent = cryptoService.encrypt(file.getBytes(), dek, iv);

    var documentDir =
        Path.of("upload/").resolve(document.getOrganizationCode()).resolve(documentId.toString());
    Files.createDirectories(documentDir);
    var location = documentDir.resolve(file.getOriginalFilename() + ".enc");

    Files.write(location, encryptedContent);

    var encryptedDek = cryptoService.encrypt(dek.getEncoded(), kekService.getActiveKek(), iv);

    var attachment =
        new Attachment()
            .setDocument(document)
            .setFileName(file.getOriginalFilename())
            .setLocation(location.toString())
            .setEncryptedDek(encryptedDek)
            .setKekVersion(kekService.getActiveAlias())
            .setIv(iv);

    attachmentRepository.save(attachment);
    return attachment.getId();
  }

  @Override
  public AttachmentDownload load(UUID attachmentId) throws Exception {
    var attachment = attachmentRepository.findById(attachmentId).orElseThrow();

    Path location = Path.of(attachment.getLocation());

    try {
      InputStream encryptedStream = Files.newInputStream(location);

      byte[] rawDek =
          cryptoService.decrypt(
              attachment.getEncryptedDek(), kekService.getActiveKek(), attachment.getIv());

      var dek = new SecretKeySpec(rawDek, "AES");

      Cipher cipher = cryptoService.initCipher(Cipher.DECRYPT_MODE, dek, attachment.getIv());
      InputStream decryptedStream = new CipherInputStream(encryptedStream, cipher);

      return new AttachmentDownload(
          decryptedStream, attachment.getFileName(), Files.size(location));
    } catch (Exception e) {
      throw new IllegalStateException("Download failed", e);
    }
  }
}
