package olegood.fenc.batch.dek;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DekRotationJobConfig {

  @Bean
  public Job dekRotationJob(JobRepository jobRepository, Step dekRotationStep) {
    return new JobBuilder("dekRotationJob", jobRepository).start(dekRotationStep).build();
  }
}
