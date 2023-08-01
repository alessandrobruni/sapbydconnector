package eu.company.connector.sapbydesignbridge.mapper;

import eu.company.connector.sapbydesignbridge.model.DataToSync;
import eu.company.connector.sapbydesignbridge.service.sap.ISyncData;
import eu.companys.commons.mqtt.dto.DataToSyncDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataToSyncMapper {
    @Autowired
    private DataLoaderConfigurationMapper dataLoaderConfigurationMapper;

    public DataToSync mapRequestToEntity(final DataToSyncDTO dataToSyncDTO) {
        return DataToSync.builder()
                .dataLoaderConfiguration(dataLoaderConfigurationMapper
                        .mapRequestToEntity(dataToSyncDTO.getDataLoaderConfiguration()))
                .minutesSyncInterval(dataToSyncDTO.getMinutesSyncInterval())
                .lastSyncDate(ISyncData.resetDateTime)
                .build();
    }

    public DataToSyncDTO mapEntityToResponse(final DataToSync dataToSync) {
        return DataToSyncDTO.builder()
                .dataLoaderConfiguration(dataLoaderConfigurationMapper
                        .mapEntityToResponse(dataToSync.getDataLoaderConfiguration()))
                .minutesSyncInterval(dataToSync.getMinutesSyncInterval())
                .build();
    }
}
