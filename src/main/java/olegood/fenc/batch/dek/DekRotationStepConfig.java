package olegood.fenc.batch.dek;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;

import olegood.fenc.checksum.ChecksumService;
import olegood.fenc.crypto.CryptoService;
import olegood.fenc.crypto.DekService;
import olegood.fenc.crypto.kek.KeyStoreService;
import olegood.fenc.domain.Attachment;
import olegood.fenc.repository.AttachmentRepository;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DekRotationStepConfig {

  private static final int chunkSize = 10;

  @Bean
  public Step dekRotationStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ItemReader<Attachment> compromisedAttachmentReader,
      ItemProcessor<Attachment, Attachment> dekRotationProcessor,
      ItemWriter<Attachment> attachmentAfterDekRotationWriter) {

    return new StepBuilder("dekRotationStep", jobRepository)
        .<Attachment, Attachment>chunk(chunkSize)
        .transactionManager(transactionManager)
        .reader(compromisedAttachmentReader)
        .processor(dekRotationProcessor)
        .writer(attachmentAfterDekRotationWriter)
        .build();
  }

  @Bean
  public RepositoryItemReader<Attachment> compromisedAttachmentReader(
          AttachmentRepository repository, ChecksumService checksumService) {
    return new RepositoryItemReaderBuilder<Attachment>()
        .name("compromisedAttachmentReader")
        .repository(repository)
        .methodName("findByDekCompromisedTrue")
        .pageSize(chunkSize)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  public ItemProcessor<Attachment, Attachment> dekRotationProcessor(
          CryptoService crypto, KeyStoreService keyStoreService, DekService dekService, ChecksumService checksumService) {

    SecretKey activeKek = keyStoreService.getActiveKek();

    return attachment -> {

      // 1. Decrypt old DEK
      // backward compatibility: as previously uploaded files used only 'iv' (file_iv) value
      byte[] iv = Optional.ofNullable(attachment.getDekIv()).orElse(attachment.getFileIv());

      var oldDek = dekService.decryptDek(attachment.getEncryptedDek(), activeKek, iv);
      crypto.decrypt(attachment.getEncryptedDek(), activeKek, iv);

      // 2. Decrypt file
      Path path = Path.of(attachment.getLocation());
      byte[] encryptedFile = Files.readAllBytes(path);

      byte[] plaintext = crypto.decrypt(encryptedFile, oldDek, attachment.getFileIv());

      // 3. Generate new DEK and IVs
      SecretKey newDek = dekService.generateDek();
      byte[] newFileIv = dekService.randomIv();
      byte[] newDekIv = dekService.randomIv();

      // 4. Re-encrypt file
      byte[] reEncryptedFile = crypto.encrypt(plaintext, newDek, newFileIv);

      Files.write(path, reEncryptedFile, StandardOpenOption.TRUNCATE_EXISTING);

      // 5. Encrypt new DEK with active KEK
      var encryptedNewDek = dekService.encryptDek(newDek, activeKek, newDekIv);

      // 6. Update metadata
      attachment.setEncryptedDek(encryptedNewDek);
      attachment.setChecksum(checksumService.compute(path));
      attachment.setFileIv(newFileIv);
      attachment.setDekIv(newDekIv);
      attachment.setDekCompromised(false);

      return attachment;
    };
  }

  @Bean
  public ItemWriter<Attachment> attachmentAfterDekRotationWriter(AttachmentRepository repository) {
    return repository::saveAll;
  }
}
