package eu.company.connector.sapbydesignbridge.mapper;

import eu.companys.commons.mqtt.dto.DataLoaderConfigurationDTO;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DataLoaderConfigurationMapper {
    public DataLoaderConfiguration mapRequestToEntity(final DataLoaderConfigurationDTO dataLoaderConfigurationDTO) {
        return DataLoaderConfiguration.builder()
                .elements(dataLoaderConfigurationDTO.getElements().stream()
                        .map(this::mapRequestToEntity).collect(Collectors.toSet()))
                .reference(dataLoaderConfigurationDTO.getReference())
                .properties(dataLoaderConfigurationDTO.getProperties())
                .target(dataLoaderConfigurationDTO.getTarget())
                .options(dataLoaderConfigurationDTO.getOptions())
                .build();
    }

    public DataLoaderConfigurationDTO mapEntityToResponse(DataLoaderConfiguration dataLoaderConfiguration) {
        return DataLoaderConfigurationDTO.builder()
                .elements(dataLoaderConfiguration.getElements().stream()
                        .map(this::mapEntityToResponse).collect(Collectors.toSet()))
                .reference(dataLoaderConfiguration.getReference())
                .properties(dataLoaderConfiguration.getProperties())
                .target(dataLoaderConfiguration.getTarget())
                .options(dataLoaderConfiguration.getOptions())
                .build();
    }
}
