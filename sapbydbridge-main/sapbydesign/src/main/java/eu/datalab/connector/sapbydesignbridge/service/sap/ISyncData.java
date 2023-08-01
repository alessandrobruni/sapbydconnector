package eu.company.connector.sapbydesignbridge.service.sap;

import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ISyncData {
    LocalDateTime resetDateTime = LocalDateTime.of(1969, 7, 20, 20, 17, 40); // Man on the moon (UTC)

    Collection<?> getData(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, final int pageNumber, final LocalDateTime lastSync, final LocalDateTime nowSync, final DataLoaderConfiguration element) throws Exception;

}
