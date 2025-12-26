package olegood.fenc.repository;

import java.util.UUID;
import olegood.fenc.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {}
