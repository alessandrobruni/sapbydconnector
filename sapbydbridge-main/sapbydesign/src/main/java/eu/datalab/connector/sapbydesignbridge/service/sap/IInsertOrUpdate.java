package eu.company.connector.sapbydesignbridge.service.sap;

import com.sap.cloud.sdk.datamodel.odatav4.core.VdmObject;
import digital.vianello.schnecke.exceptions.ConflictException;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.companys.commons.mqtt.dto.entity.AEntityDTO;
import eu.companys.commons.mqtt.dto.message.PositiveMessageDTO;
import eu.companys.commons.mqtt.exceptions.MessageException;
import eu.companys.commons.mqtt.model.OperationReport;
import eu.companys.commons.mqtt.model.OperationType;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;

import java.util.*;

public interface IInsertOrUpdate<Entity extends VdmObject<?>> {
    String entryKey = "entry";

    String updatableKey = "updatable";

    String externalIdKey = "externalId";


    default <entityDTO extends AEntityDTO> PositiveMessageDTO<?> createOrUpdate(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, OperationReport operationReport, final entityDTO entityDTO) throws Exception {
        // Todo : da riattivare la riga quando sarà sistemato il metodo getRemapService

        //getRemapService().prepareEntity(tenant.getName(), bridgeConfiguration, entityDTO.getFields());
        setEntry(entityDTO);
        getValidationService().checkEntity(entityDTO.getFields(), entityDTO.getValidations());
        operationReport.setExternalId((String) entityDTO.getOptions().get(externalIdKey));
        operationReport.setType(OperationType.UPDATE);
        Collection<?> entities = null;
        try {
            if (Objects.isNull(entityDTO.getEntry()))
                entities = getByFilter(tenant, bridgeConfiguration, entityDTO.getOptions());
            else {
                operationReport.setInternalId(entityDTO.getEntry());
                Entity entity = getByKey(tenant, bridgeConfiguration, entityDTO.getOptions(), entityDTO.getEntry());
                entities = Objects.isNull(entity) ? new ArrayList<>() : new ArrayList<>(List.of(entity));
            }
            if (entities.size() != 1 || (entityDTO.getOptions().containsKey(updatableKey) && !(Boolean) entityDTO.getOptions().get(updatableKey)))
                throw new ConflictException("Cannot update: too many entities found (" + entities.size() + ") or entity cannot be updated!");
            return update(tenant, bridgeConfiguration, entityDTO, getType().cast(entities.iterator().next()));
        } catch (Exception e) {
            if ((e instanceof MessageException && ((MessageException) e).getHttpCode().equals(HttpStatus.NOT_FOUND.value())) || (Objects.nonNull(entities) && entities.size() == 0)) {
                getSerieService().setSerieToEntity(tenant.getName(), bridgeConfiguration, getObjectType(), entityDTO);
                operationReport.setType(OperationType.CREATE);
                return create(tenant, bridgeConfiguration, entityDTO);
            }
            throw e;
        }
    }

    private <entityDTO extends AEntityDTO> void setEntry(final entityDTO entityDTO) {
        Object entry = entityDTO.getFields().remove(entryKey);
        if (Objects.nonNull(entry) && !Strings.isEmpty(entry.toString()))
            entityDTO.setEntry(entry.toString());
    }

    SerieService getSerieService();

    ValidationService getValidationService();

    RemapService getRemapService();

    Class<Entity> getType();

    String getObjectType();

    Entity getByKey(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, final Map<String, Object> options, final String entry) throws Exception;

    Collection<?> getByFilter(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, final Map<String, Object> options) throws Exception;

    <entityDTO extends AEntityDTO> PositiveMessageDTO<?> update(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, final entityDTO entityDTO, final Entity entity) throws Exception;

    <entityDTO extends AEntityDTO> PositiveMessageDTO<?> create(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, final entityDTO entityDTO) throws Exception;


}
