package olegood.fenc.web;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import olegood.fenc.batch.KekRotationJobRequest;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/jobs")
public class KekRotationJobController {

  private final JobOperator jobOperator;
  private final Job kekRotationJob;

  @PostMapping("/kekRotationJob/launch")
  public ResponseEntity<?> launch(@RequestBody KekRotationJobRequest request) {

    validate(request);

    var params =
        new JobParametersBuilder()
            .addString("oldKekAlias", request.oldKekAlias())
            .addString("newKekAlias", request.newKekAlias())
            .addLong("runId", System.currentTimeMillis())
            .toJobParameters();

    try {
      var execution = jobOperator.start(kekRotationJob, params);
      return ResponseEntity.accepted()
          .body(
              Map.of(
                  "jobName", kekRotationJob.getName(),
                  "jobExecutionId", execution.getId(),
                  "status", execution.getStatus().toString()));
    } catch (JobExecutionAlreadyRunningException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("KEK rotation job is already running");

    } catch (JobInstanceAlreadyCompleteException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("KEK rotation job already completed for given parameters");

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Invalid job parameters: " + e.getMessage());
    }
  }

  private void validate(KekRotationJobRequest request) {

    if (request.oldKekAlias() == null || request.oldKekAlias().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "oldKekAlias is required");
    }

    if (request.newKekAlias() == null || request.newKekAlias().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newKekAlias is required");
    }

    if (request.oldKekAlias().equals(request.newKekAlias())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "oldKekAlias and newKekAlias must differ");
    }
  }
}
