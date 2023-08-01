package eu.company.connector.sapbydesignbridge.mapper.sap;

import org.apache.logging.log4j.util.Strings;

public interface INoteMapper {
    default String addOrUpdate(String bridgeId, String element, String oldValue) {
        if (Strings.isEmpty(oldValue))
            return getString(bridgeId, element);
        else if (!oldValue.contains(bridgeId))
            return oldValue + "\n" + getString(bridgeId, element);
        else return oldValue;
    }

    private String getString(String bridgeId, String element) {
        return element + " managed by company bridge (" + bridgeId + ")";
    }
}
