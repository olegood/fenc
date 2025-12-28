package olegood.fenc.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import lombok.RequiredArgsConstructor;
import olegood.fenc.checksum.ChecksumService;
import olegood.fenc.crypto.CryptoService;
import olegood.fenc.crypto.DekService;
import olegood.fenc.crypto.kek.KekService;
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

  private final ChecksumService checksumService;

  @Override
  public UUID store(UUID documentId, MultipartFile file) throws Exception {
    if (!"application/pdf".equals(file.getContentType())) {
      throw new IllegalArgumentException("File storage accepts `PDF` only");
    }

    var document = documentRepository.findById(documentId).orElseThrow();

    // 1. Generate a fresh DEK (per attachment)
    var dek = dekService.generateDek();

    // 2. Generate independent IVs
    var fileIv = dekService.randomIv(); // for file encryption
    var dekIv = dekService.randomIv(); // for DEK encryption

    // 3. Encrypt file content with DEK + FILE_IV
    var encryptedContent = cryptoService.encrypt(file.getBytes(), dek, fileIv);

    // 4. Persist encrypted file
    var documentDir =
        Path.of("upload/").resolve(document.getOrganizationCode()).resolve(documentId.toString());
    Files.createDirectories(documentDir);
    var location = documentDir.resolve(file.getOriginalFilename() + ".enc");
    Files.write(location, encryptedContent);

    // 5. Encrypt DEK with KEK + DEK_IV
    var encryptedDek = dekService.encryptDek(dek, kekService.getActiveKek(), dekIv);

    // 6. Persist metadata
    var attachment =
        new Attachment()
            .setDocument(document)
            .setFileName(file.getOriginalFilename())
            .setLocation(location.toString())
            .setFileIv(fileIv)
            .setDekIv(dekIv)
            .setEncryptedDek(encryptedDek)
            .setKekVersion(kekService.getActiveAlias())
            .setChecksum(checksumService.compute(encryptedContent));

    attachmentRepository.save(attachment);
    return attachment.getId();
  }

  @Override
  public AttachmentDownload load(UUID attachmentId) {
    var attachment = attachmentRepository.findById(attachmentId).orElseThrow();

    Path location = Path.of(attachment.getLocation());

    try {
      if (checksumService.compute(location) != attachment.getChecksum()) {
        throw new IllegalStateException("Checksum mismatch");
      }

      InputStream encryptedStream = Files.newInputStream(location);
      var dek =
          dekService.decryptDek(
              attachment.getEncryptedDek(), kekService.getActiveKek(), attachment.getDekIv());

      Cipher cipher = cryptoService.initCipher(Cipher.DECRYPT_MODE, dek, attachment.getFileIv());
      InputStream decryptedStream = new CipherInputStream(encryptedStream, cipher);

      return new AttachmentDownload(
          decryptedStream, attachment.getFileName(), Files.size(location));
    } catch (Exception e) {
      throw new IllegalStateException("Download failed", e);
    }
  }
}
