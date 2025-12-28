package olegood.fenc.web;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/jobs")
public class Crc32cBackfillJobController {

  private final JobOperator jobOperator;
  private final Job crc32cBackfillJob;

  @PreAuthorize("hasRole('KEY_MANAGEMENT_ADMIN')")
  @PostMapping("/crc32cBackfillJob/launch")
  public ResponseEntity<?> launch() {

    var params =
        new JobParametersBuilder().addLong("runId", System.currentTimeMillis()).toJobParameters();

    try {
      JobExecution execution = jobOperator.start(crc32cBackfillJob, params);

      return ResponseEntity.accepted()
          .body(
              Map.of(
                  "jobName", crc32cBackfillJob.getName(),
                  "jobExecutionId", execution.getId(),
                  "status", execution.getStatus().toString()));

    } catch (JobExecutionAlreadyRunningException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("CRC32C backfill job is already running");

    } catch (JobInstanceAlreadyCompleteException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("CRC32C backfill job already completed for given parameters");

    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body("Unable to start CRC32C backfill job: " + e.getMessage());
    }
  }
}
