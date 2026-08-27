package com.sodimac.batch.fiscal.download;

import com.sodimac.batch.fiscal.download.service.impl.CreditNoteDownloadBatchService;
import com.sodimac.batch.fiscal.download.service.impl.InvoiceDownloadBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BatchFiscalDownloadApplication {

    private static final Logger log = LoggerFactory.getLogger(BatchFiscalDownloadApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BatchFiscalDownloadApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    // batch.autorun=false evita que los tests (@SpringBootTest) disparen el batch al bootear el contexto
    @Bean
    @ConditionalOnProperty(name = "batch.autorun", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner run(InvoiceDownloadBatchService invoiceService,
                                  CreditNoteDownloadBatchService creditNoteService) {
        return args -> {
            log.info("=== Inicio batch-fiscal-download ===");

            invoiceService.ejecutar();
            creditNoteService.ejecutar();

            log.info("=== Fin batch-fiscal-download ===");
        };
    }
}
