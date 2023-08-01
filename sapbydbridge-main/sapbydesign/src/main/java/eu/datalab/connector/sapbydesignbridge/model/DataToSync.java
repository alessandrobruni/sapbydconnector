package eu.company.connector.sapbydesignbridge.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import digital.vianello.schnecke.dto.deserializer.LocalDateTimeDeserializer;
import digital.vianello.schnecke.dto.serializer.LocalDateTimeSerializer;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DataToSync {
    @EqualsAndHashCode.Include
    private DataLoaderConfiguration dataLoaderConfiguration;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastSyncDate;
    private Integer minutesSyncInterval;
}
