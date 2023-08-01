package eu.company.connector.sapbydesignbridge.mapper.sap;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class QueryMapper extends AEntityMapper {
    private final static String odataContextKey = "@odata.context";

    // Todo: manage the query
//    public Map<String, Object> mapEntityToResponse(@NotNull final SQLQueryResult sqlQueryResult, final Map<String, Object> customEntities) {
//        Map<String, Object> result = new HashMap<>();
//        sqlQueryResult.getCustomFields().entrySet().stream()
//                .filter(q -> !odataContextKey.equals(q.getKey())).map(q -> parseValue(q.getKey(), q.getValue()))
//                .forEach(result::putAll);
//        result.putAll(customEntities);
//        return result;
//    }


}
