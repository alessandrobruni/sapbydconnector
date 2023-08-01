package eu.company.connector.sapbydesignbridge;

import digital.vianello.schnecke.util.DateUtil;
import eu.company.connector.sapbydesignbridge.config.ServiceConfig;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.OperationReport;
import eu.companys.commons.mqtt.model.OperationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableConfigurationProperties({ServiceConfig.class})
@ComponentScan({"com.sap.cloud.sdk", "eu.company.connector.sapbydesignbridge"})

public class SAPByDBridgeApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(SAPByDBridgeApplication.class)
                .build().run(args);
    }


}


