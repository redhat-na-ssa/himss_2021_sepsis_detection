package com.redhat.cajun.navy.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration(exclude = { KafkaAutoConfiguration.class })
public class ProcessServiceApplication {

    private final static Logger log = LoggerFactory.getLogger(ProcessServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ProcessServiceApplication.class);
        application.setRegisterShutdownHook(false);
        ConfigurableApplicationContext context = application.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutdownhook called");
            context.close();
        }));
    }

    @Bean
    CommandLineRunner deployAndValidate() {
        return new CommandLineRunner() {

            @Autowired
            private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

            @Override
            public void run(String... strings) throws Exception {
                kafkaListenerEndpointRegistry.start();
            }
        };
    }
}
