package eu.company.connector.sapbydesignbridge.service.sap.layer;

import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.logging.impl.Log4JLogger;
import org.springframework.cache.support.NullValue;


import java.util.HashMap;
import java.util.Map;

/*
 * This is a simple mock of a DestinationAccessor.
 *
 */
@Log4j2
public class DestinationByDHolder {

/* * The class is to manage different Tenants and create only one HttpDestination
 * corresponding to each Tenants's DestinationProperties; it is useful to use a Map to store
 * the instances of HttpDestination already created for each DestinationProperties.
 */
    private static final Map<String, HttpDestination> allDestinations = new HashMap<>();

    public static HttpDestination getHttpDestination(final Tenant tenant, BridgeConfiguration bridgeConfiguration) throws RuntimeException{
        try {
            // se è gia reguistrato lo recupera

            String key = tenant.getName();
            return getHttpDestination(key,bridgeConfiguration.getDestination());
        } catch (Exception ignored) {
            // altrimenti lo crea
              return getHttpDestination(bridgeConfiguration.getTenant(), bridgeConfiguration.getDestination());
        }
    }

    public static HttpDestination getHttpDestination(final String tenantName, DestinationProperties destinationProperties) throws  RuntimeException {

        String key = tenantName;

        if( key == null) throw new RuntimeException("Tenant name cannot be null to retrive the correct HttpDestination") ;

        synchronized (allDestinations) {

            HttpDestination httpDestination = allDestinations.get(key);
            if(httpDestination == null){
                log.info("++++++++++++++++++++++++++++++++  HttpDestination -> mapping " + key);
                httpDestination = new DefaultHttpDestination(destinationProperties);
                allDestinations.put(key,httpDestination);

            }
            return httpDestination;
        }
    }
}