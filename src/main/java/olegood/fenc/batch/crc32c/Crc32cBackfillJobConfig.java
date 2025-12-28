package olegood.fenc.batch.crc32c;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Crc32cBackfillJobConfig {

  @Bean
  public Job crc32cBackfillJob(JobRepository jobRepository, Step crc32cBackfillStep) {
    return new JobBuilder("crc32cBackfillJob", jobRepository).start(crc32cBackfillStep).build();
  }
}
