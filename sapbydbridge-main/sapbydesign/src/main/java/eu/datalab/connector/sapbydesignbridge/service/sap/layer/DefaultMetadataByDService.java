package eu.company.connector.sapbydesignbridge.service.sap.layer;

import javax.annotation.Nonnull;

public class DefaultMetadataByDService {
    @Nonnull
    private final String servicePath;


    private DefaultMetadataByDService(@Nonnull String servicePath) {
        this.servicePath = servicePath;
    }

    public DefaultMetadataByDService withServicePath(@Nonnull String servicePath){
        return new DefaultMetadataByDService(servicePath);
    }
}
