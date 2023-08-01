package eu.company.connector.sapbydesignbridge.service.sap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface IStringFilter {
    String queryFilterKey = "queryFilter";

    String filterParamsKey = "fParams";

    String lastSyncFilterKey = "lastSyncFilter";

    String lastSyncDateKey = "lastSyncDate";

    String lastSyncTimeKey = "lastSyncTime";

    String nowSyncTimeKey = "nowSyncTime";

    String nowSyncDateKey = "nowSyncDate";

    default String getFilter(Map<?, ?> options, LocalDateTime lastSync, LocalDateTime nowSync) {
        String strFilter = getStrFilter(options, lastSync, nowSync);
        if (strFilter.isEmpty()) return "";
        for (Map.Entry<String, String> entry : prepareFilterMap(lastSync, nowSync, options).entrySet())
            strFilter = strFilter.replace(":" + entry.getKey(), entry.getValue());
        return strFilter;
    }

    default String getFilter(Map<?, ?> options) {
        return getFilter(options, null, null);
    }

    default Map<String, String> prepareFilterMap(LocalDateTime lastSync, LocalDateTime nowSync, Map<?, ?> options) {
        Map<String, String> result = new HashMap<>();
        if (Objects.nonNull(lastSync) && Objects.nonNull(nowSync) && options.containsKey(lastSyncFilterKey) && (Boolean) options.get(lastSyncFilterKey)) {
            result.put(lastSyncDateKey, serializeParamValue(lastSync.toLocalDate().toString()));
            result.put(lastSyncTimeKey,
                    serializeParamValue(Integer.parseInt(
                            lastSync.toLocalTime().toString().replace(":", ""))));

            result.put(nowSyncDateKey, serializeParamValue(nowSync.toLocalDate().toString()));
            result.put(nowSyncTimeKey,
                    serializeParamValue(Integer.parseInt(
                            nowSync.toLocalTime().toString().replace(":", ""))));
        }
        if (options.containsKey(filterParamsKey))
            ((Map<?, ?>) options.get(filterParamsKey))
                    .forEach((key, value) -> result.put(key.toString(), serializeParamValue(value)));
        return result;
    }

    private String serializeParamValue(Object value) {
        if (value instanceof Boolean)
            return "'" + ((Boolean)value).toString() + "'";
    //        return "'" + ((Boolean) value ? BoYesNoEnum.TYES.getName() : BoYesNoEnum.TNO.getName()) + "'";
        return (value instanceof String) ? "'" + value + "'" : value.toString();
    }

    private String getStrFilter(Map<?, ?> options, LocalDateTime lastSync, LocalDateTime nowSync) {
        if (!options.containsKey(queryFilterKey)
                && (!options.containsKey(lastSyncFilterKey) || !(Boolean) options.get(lastSyncFilterKey) || Objects.isNull(lastSync) || Objects.isNull(nowSync)))
            return "";
        StringBuilder filterStr = new StringBuilder();
        filterStr.append("$filter=");
        final String lastSyncFilter = "(CreateDate gt :" + lastSyncDateKey
                + " or (CreateDate eq :" + lastSyncDateKey + " and CreateTS ge :" + lastSyncTimeKey + ")"
                + " or (UpdateDate ne null and UpdateDate gt :" + lastSyncDateKey + ")"
                + " or (UpdateDate ne null and UpdateTS ne null and UpdateDate eq :" + lastSyncDateKey + " and UpdateTS ge :" + lastSyncTimeKey + "))"
                + " and (CreateDate lt :" + nowSyncDateKey
                + " or (CreateDate eq :" + nowSyncDateKey + " and CreateTS le :" + nowSyncTimeKey + ")"
                + " or (UpdateDate ne null and UpdateDate lt :" + nowSyncDateKey + ")"
                + " or (UpdateDate ne null and UpdateTS ne null and UpdateDate eq :" + nowSyncDateKey + " and UpdateTS le :" + nowSyncTimeKey + "))"
                + "&$orderby=CreateDate asc, CreateTS asc";
        if (options.containsKey(queryFilterKey))
            filterStr.append(options.get(queryFilterKey)).append(options.containsKey(lastSyncFilterKey) ? " and " : "");
        if (Objects.nonNull(lastSync) && Objects.nonNull(nowSync) && options.containsKey(lastSyncFilterKey))
            filterStr.append(lastSyncFilter);
        return filterStr.toString();
    }

}
