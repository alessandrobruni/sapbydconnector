package eu.company.connector.sapbydesignbridge.model;

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import eu.companys.commons.mqtt.model.ModelType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@With
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Tenant")
@TypeAlias("Tenant")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Implementation of a test or production Tenant  (ex: Test, Fluorital, Aminsushi, etc...)
 * Each tenant will have a BridgeConfiguration  with indication of
 * host url, technical User, authorization Type, connection Type
 */
public class Tenant implements Serializable {


    @Id
    @EqualsAndHashCode.Include
    /* i.e. FluoritalColl, FluoritalProd ... */
    private String name;

    private String language;

    private String timeZone;

    @Builder.Default
    private Set<ModelType> models = new HashSet<>();

    @Builder.Default
    private Map<String, String> bridgeConfigurations = new HashMap<>();
}
