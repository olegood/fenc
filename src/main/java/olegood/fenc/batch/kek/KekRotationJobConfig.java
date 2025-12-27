package olegood.fenc.batch.kek;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KekRotationJobConfig {

  @Bean
  public Job kekRotationJob(JobRepository jobRepository, Step kekRotationStep) {
    return new JobBuilder("kekRotationJob", jobRepository).start(kekRotationStep).build();
  }
}
