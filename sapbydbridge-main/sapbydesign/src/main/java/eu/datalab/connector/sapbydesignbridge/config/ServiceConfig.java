package eu.company.connector.sapbydesignbridge.config;

import eu.companys.commons.mqtt.model.BridgeType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "service")
public class ServiceConfig {
    private String profile;
    private BridgeType type;
    private Map<String, String> scheduler;
    private StorageConfig storage;
    private DatabaseConfig db;
    private List<EntityConfig> users;


    @Getter
    @Setter
    public static class EntityConfig {
        private String name;
        private String bridgeId;
    }

    @Getter
    @Setter
    public static class DatabaseConfig {
        private String host;
        private Integer port;
        private Integer timeout;
        private String password;
    }
    @Getter
    @Setter
    public static class StorageConfig {
        private String password;
    }
}
