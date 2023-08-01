package eu.company.connector.sapbydesignbridge.service.sap;

import digital.vianello.schnecke.exceptions.BadRequestException;
import eu.companys.commons.mqtt.dto.entity.ValidationEntityDTO;
import eu.companys.commons.mqtt.model.ValidationType;
import org.apache.logging.log4j.util.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValidationService {
    private static final String listCheckFieldKey = "listElement";

    public void checkEntity(final Map<?, ?> entity, final Collection<ValidationEntityDTO> validations) throws Exception {
        for (ValidationEntityDTO validation : validations) {
            Object value = entity.get(validation.getProperty());
            checkProperty(value, validation);
            if (value instanceof List) {
                for (Object obj : (List<?>) value)
                    if (obj instanceof Map<?, ?>)
                        checkEntity((Map<?, ?>) value, List.of(validation));
                    else
                        checkEntity(Map.of(listCheckFieldKey, value), List.of(validation));
            } else if (value instanceof Map<?, ?>)
                checkEntity((Map<?, ?>) value, List.of(validation));
        }
    }

    private void checkProperty(Object value, ValidationEntityDTO validationEntityDTO) throws Exception {
        if (Objects.isNull(validationEntityDTO.getType())) return;
        boolean checkFail = false;
        switch (validationEntityDTO.getType()) {
            case EQ:
                checkFail = !Objects.equals(value, validationEntityDTO.getVal());
                break;
            case GT:
                checkFail = (Objects.isNull(value) || (!(value instanceof Integer) && !(value instanceof Double))
                        || !(Double.compare((double) value, (double) validationEntityDTO.getVal()) > 0));
                break;
            case LT:
                checkFail = (Objects.isNull(value) || (!(value instanceof Integer) && !(value instanceof Double))
                        || !(Double.compare((double) value, (double) validationEntityDTO.getVal()) < 0));
                break;
            case GTE:
                checkFail = (Objects.isNull(value) || (!(value instanceof Integer) && !(value instanceof Double))
                        || !(Double.compare((double) value, (double) validationEntityDTO.getVal()) >= 0));
                break;
            case LTE:
                checkFail = (Objects.isNull(value) || (!(value instanceof Integer) && !(value instanceof Double))
                        || !(Double.compare((double) value, (double) validationEntityDTO.getVal()) <= 0));
                break;
            case REGEX:
                checkFail = (Objects.isNull(value) || !(value instanceof String)
                        || !((String) value).matches((String) validationEntityDTO.getVal()));
                break;
            case LENGHT:
                checkFail = (Objects.isNull(value) || (!(value instanceof String) && !(value instanceof Collection))
                        || (value instanceof String && ((String) value).length() != (Integer) validationEntityDTO.getVal())
                        || (value instanceof Collection && ((Collection<?>) value).size() != (Integer) validationEntityDTO.getVal()));
                break;
            case NOT_NULL:
                checkFail = Objects.isNull(value);
                break;
            case NOT_EMPTY:
                checkFail = (Objects.isNull(value) || (!(value instanceof String) && !(value instanceof Collection))
                        || (value instanceof String && Strings.isBlank((String) value))
                        || (value instanceof Collection && ((Collection<?>) value).isEmpty()));
                break;
            case ALLOWED_VALUES:
                checkFail = (Objects.isNull(value) || !((List<?>) validationEntityDTO.getVal()).contains(value));
                break;
        }
        if (checkFail)
            throw new BadRequestException("Check failed, field: '" + validationEntityDTO.getProperty() + "' value: '" + value + "' check constrain: ('" + validationEntityDTO.getType() + "'"
                    + ((validationEntityDTO.getType().equals(ValidationType.NOT_EMPTY) || validationEntityDTO.getType().equals(ValidationType.NOT_NULL)) ? "" : " '" + validationEntityDTO.getVal() + "'") + ")");
    }
}
