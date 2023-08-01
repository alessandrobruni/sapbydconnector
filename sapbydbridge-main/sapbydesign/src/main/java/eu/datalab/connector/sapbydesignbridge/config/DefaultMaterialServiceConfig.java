package eu.company.connector.sapbydesignbridge.config;

import eu.companys.connector.lib.bydmaterialodata.services.DefaultMaterialService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultMaterialServiceConfig {
    @Bean
    public DefaultMaterialService getMetadata() {
        return new DefaultMaterialService();
    }

}
