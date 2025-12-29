package olegood.fenc.repository;

import java.util.Optional;
import java.util.UUID;
import olegood.fenc.domain.Clerk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClerkRepository extends JpaRepository<Clerk, UUID> {

  Optional<Clerk> findBySearchEmail(String searchEmail);

  Optional<Clerk> findBySearchSsn(String searchSsn);

  @Query("select c from Clerk c where c.email not like :alias or c.ssn not like :alias")
  Page<Clerk> findNotMatchingNewAlias(String alias, Pageable pageable);
}
