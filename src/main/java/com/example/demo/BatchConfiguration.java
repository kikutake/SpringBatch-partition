package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

	final int _thread = 5;
	
	  @Bean
	  Job sampleJob(JobRepository jobRepository, PlatformTransactionManager txManager) {
		  
		  //
	//	  Partitioner partitioner = new CustomPartitioner()
		  
		  
		/*
		StepBuilder sb = new StepBuilder("sampleStep", jobRepository);
		TaskletStepBuilder tsb = sb.tasklet(new HelloWorldTasklet(), txManager);
		Step step = tsb.build();
		
		Step step2 = tsb.build();
		
		*/
		
/*	    Step step = new StepBuilder("sampleStep", jobRepository).tasklet((contribution, chunkContext) -> {
	          System.out.println("sample job step");
	          return RepeatStatus.FINISHED;
	        }, txManager)
	          .build();
*/	    

		
		/*
		JobBuilder sampleJobBuilder = new JobBuilder("sampleJob", jobRepository);
	    SimpleJobBuilder simpleJobBuilder= sampleJobBuilder.start(step).next(step2);
	    Job tmpjob = simpleJobBuilder.build();
	    return tmpjob;

	  */
		  
        return new JobBuilder("doPartitionJob", jobRepository)
                .start(masterStep(jobRepository, txManager)) // calling 1st step
                //.next(nextStep) // to call next steps
                .build();

	  }
 
	  
	  
	// defining master step - it will first create the partitions and then send the process to slave step simultaneously
	    @Bean
	    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
	    	//ここでmasterStepはInvestTRST_UPD_Partitionerを使うこと、
	    	//masterStepのpartitionHandlerは、slaveStep1メソッドを使うpatritionHandlerであることを関連付ける。
	    	//これによりspringflameworkから、masterStepが実行される際にInvestTRST_UPD_Partitionerに分割数が渡され、分割され、
	    	//slaveStepと紐づけられたHelloWorldTaskletが実行される。
	    	return new StepBuilder("masterStep", jobRepository)
	                .partitioner("partitioner", new InvestTRST_UPD_Partitioner())
	                .partitionHandler(partitionHandler(jobRepository, transactionManager))
	                .build();
	    }


	@Bean
	  public Step slaveStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
	      return new StepBuilder("slaveStep1", jobRepository).tasklet(new HelloWorldTasklet(), transactionManager)
	          .build();
	  }
	  
    public PartitionHandler partitionHandler(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(_thread);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep1(jobRepository, transactionManager));

        return taskExecutorPartitionHandler;
    }	  
	  
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //MaxThread数を設定できるプロパティっぽい。
        taskExecutor.setMaxPoolSize(_thread);
        taskExecutor.setCorePoolSize(_thread);
        taskExecutor.setQueueCapacity(_thread);

        return taskExecutor;
    }
	  
	  
	  
	  	  
}
