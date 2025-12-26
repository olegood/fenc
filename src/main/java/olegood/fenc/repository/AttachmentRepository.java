package olegood.fenc.repository;

import java.util.UUID;
import olegood.fenc.domain.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    Page<Attachment> findByKekVersion(String kekVersion, Pageable pageable);

    Page<Attachment> findByDekCompromisedTrue(Pageable pageable);
}
