package eu.company.connector.sapbydesignbridge.model;


import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationProperties;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestinationProperties;
import digital.vianello.schnecke.util.AES;
import lombok.*;
import java.util.*;

@Getter
@Setter
@With
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BridgeConfiguration {
    @NonNull
    private String tenant;
    private Boolean trustCertificate;
    @EqualsAndHashCode.Include
    private String companyDb;

    //for DestinationProperties
    @EqualsAndHashCode.Include
    private String URI;// corrisponde ad URL in DestinationProperties
    private String servicePath;
    private String userName;
    private String userPassword;
    private String connectionType;
    private String authenticationType;
    private String responseType;

    // attributi per TenantService
    private String proxyURI;
    private String language;
    @Builder.Default
    private Integer rowsPerPage = 1000;
    @Builder.Default
    private String countryCode = "IT";
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();
    @Builder.Default
    private Set<String> targetMessageUsers = new HashSet<>();
    @Builder.Default
    private Set<String> targetMessageGroups = new HashSet<>();
    @Builder.Default
    private Set<DataToSync> thingsToSync = new HashSet<>();


    @Builder.Default
    private Map<String, String> args = new HashMap<>();
    @Builder.Default
    private Map<String, Object> remapProperties = new HashMap<>();
    @Builder.Default
    private Map<String, Object> propertyFilters = new HashMap<>();



    public Integer getRowsPerPage() {
        return Objects.isNull(rowsPerPage) ? 1000 : rowsPerPage;
    }

    public DestinationProperties getDestination() {

        DestinationProperties destinationProperties = (DestinationProperties)  DefaultDestination.builder()
                .property("Name", getServicePath())
                .property("URL", getURI()) // DestinationPropertiesBuilder accetta URL -> coincide nel ns caso can URI
                .property("Type", getConnectionType())
                .property("Authentication", getAuthenticationType())
                .property("User", getUserName())
                .property("Password", getUserPassword())
                .build();

        return destinationProperties;
    }

    public HttpDestinationProperties getHttpDestination() {

        HttpDestinationProperties destinationProperties = (HttpDestinationProperties)  DefaultDestination.builder()
                .property("Name", getServicePath())
                .property("URL", getURI())
                .property("Type", getConnectionType())
                .property("Authentication", getAuthenticationType())
                .property("User", getUserName())
                //property("Password", AES.decrypt(bridgeConfiguration.getUserPassword(), keyDecrypt))
                .property("Password", "Welcome1")
                .build().asHttp();
        return destinationProperties;
    }


}