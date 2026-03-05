package com.sodimac.batch.process;

import com.sodimac.batch.process.service.impl.CreditNoteDownloadBatchService;
import com.sodimac.batch.process.service.impl.InvoiceDownloadBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BatchProcessApplication {

    private static final Logger log = LoggerFactory.getLogger(BatchProcessApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BatchProcessApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    @Bean
    public CommandLineRunner run(InvoiceDownloadBatchService invoiceService,
                                  CreditNoteDownloadBatchService creditNoteService) {
        return args -> {
            log.info("=== Inicio batch-process ===");

            invoiceService.ejecutar();
            creditNoteService.ejecutar();

            log.info("=== Fin batch-process ===");
        };
    }
}
