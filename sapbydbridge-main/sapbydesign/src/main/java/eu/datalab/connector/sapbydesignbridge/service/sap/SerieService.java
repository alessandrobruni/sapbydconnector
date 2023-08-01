package eu.company.connector.sapbydesignbridge.service.sap;

import com.sap.cloud.sdk.datamodel.odata.client.expression.ODataResourcePath;
import digital.vianello.schnecke.exceptions.BadRequestException;
import eu.company.connector.sapbydesignbridge.mapper.sap.QueryMapper;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.service.sap.request.RequestService;
import eu.companys.commons.mqtt.dto.entity.AEntityDTO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SerieService {
    private final static String documentKey = "Document";
    private final static String documentSubTypeKey = "DocumentSubType";
    private final static String documentTypeParamsKey = "DocumentTypeParams";

    private final static String serieKey = "Series";

    private final static String serieQueryKey = "serieQuery";

    @Autowired
    private RequestService requestService;

    @Autowired
    private QueryMapper queryMapper;

    /* Todo : implementare in futuro
     */
    public <entityDTO extends AEntityDTO> void setSerieToEntity(final String tenantName, final BridgeConfiguration bridgeConfiguration, String objectType, final entityDTO entityDTO) throws Exception {
        if (!entityDTO.getOptions().containsKey(serieQueryKey)) return;
//        Collection<?> result = getByFilter(tenantName, bridgeConfiguration, objectType, new HashMap<>());
//        filterRegex(result, (String) entityDTO.getFields().get(serieKey), objectType, (Map<?, ?>) entityDTO.getOptions().get(serieQueryKey));
//        if (result.size() != 1)
//            throw new BadRequestException("Could not get the series for object type '" + objectType + "'. Too many result, got " + result.size() + " expected 1");
//        entityDTO.getFields().put(serieKey, ((Map<?, ?>) result.iterator().next()).get(serieKey.toLowerCase()));
    }

    private void filterRegex(Collection<?> result, String serie, String objectType, Map<?, ?> options) throws Exception {
        if (Strings.isEmpty(serie))
            throw new BadRequestException("Could not get the series for object type '" + objectType + "'. Serie name is empty");
        options.forEach((key, value) ->
                result.removeIf(r ->
                        !((Map<?, ?>) r).get(key).toString().matches((value.equals(serieKey) ? serie : value).toString())));
    }

    /*Todo : implement the filetre without SQLQueryResult
    */
    public List<?> getByFilter(final String tenantName, final BridgeConfiguration bridgeConfiguration, String objectType, final Map<String, Object> options) throws Exception {
        Map<String, Map<String, String>> entity = new HashMap<>();
        Map<String, String> value = new HashMap<>();
        value.put(documentKey, objectType);
        if (options.containsKey(getDocumentSubTypeKey()))
            value.put(documentSubTypeKey, (String) options.get(getDocumentSubTypeKey()));
        entity.put(documentTypeParamsKey, value);
//        return ((Collection<?>) requestService.postRequest(tenantName, bridgeConfiguration,
//                ODataResourcePath.of("SeriesService_GetDocumentSeries"), SQLQueryResult.class, entity)).stream()
//                .map(r -> queryMapper.mapEntityToResponse((SQLQueryResult) r, new HashMap<>())).collect(Collectors.toList());
        return null;
    }

    private String getDocumentSubTypeKey() {
        char[] c = documentSubTypeKey.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

}
