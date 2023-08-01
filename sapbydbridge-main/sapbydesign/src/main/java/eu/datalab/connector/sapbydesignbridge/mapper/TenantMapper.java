package eu.company.connector.sapbydesignbridge.mapper;

import digital.vianello.schnecke.util.AES;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.companys.commons.mqtt.dto.TenantConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TenantMapper {
    private static final String companyDbKey = "companyDb";
    private static final String proxyURIKey = "proxyURI";
    private static final String trustCertificateKey = "trustCertificate";
    private static final String userNameKey = "userName";
    private static final String passwordKey = "userPassword";
    private static final String servicePathKey = "servicePath";
    private static final String URIKey = "URI";
    private static final String targetMessageUsersKey = "targetMessageUsers";
    private static final String targetMessageGroupsKey = "targetMessageGroups";
    private static final String rowsPerPageKey = "rowsPerPage";

    private static final String propertiesKey = "properties";

    private static final String countryCodeKey = "countryCode";

    private static final String automaticIntrastatRelevantKey = "automaticIntrastatRelevant";

    @Autowired
    private DataToSyncMapper dataToSyncMapper;

    @Value("${service.storage.password}")
    private String password;

    public Tenant mapRequestToEntity(final TenantConfigDTO tenantConfigDTO) {
        return Tenant.builder()
                .name(tenantConfigDTO.getTenant())
                .language(tenantConfigDTO.getLanguage())
                .timeZone(tenantConfigDTO.getTimeZone())
                .models(Objects.isNull(tenantConfigDTO.getModels()) ?
                        new HashSet<>() : tenantConfigDTO.getModels()).build();
    }

    public BridgeConfiguration mapRequestToBridgeConfiguration(final TenantConfigDTO tenantConfigDTO) throws Exception {
        return BridgeConfiguration.builder()
                .companyDb(Objects.isNull(tenantConfigDTO.getConfigs().get(companyDbKey)) ? null : tenantConfigDTO.getConfigs().get(companyDbKey).toString())
                .proxyURI(Objects.isNull(tenantConfigDTO.getConfigs().get(proxyURIKey)) ? null : tenantConfigDTO.getConfigs().get(proxyURIKey).toString())
                .trustCertificate(Objects.isNull(tenantConfigDTO.getConfigs().get(trustCertificateKey)) ? null : (Boolean) tenantConfigDTO.getConfigs().get(trustCertificateKey))
                .userName(Objects.isNull(tenantConfigDTO.getConfigs().get(userNameKey)) ? null : tenantConfigDTO.getConfigs().get(userNameKey).toString())
                .userPassword(Objects.isNull(tenantConfigDTO.getConfigs().get(passwordKey)) ? null
                        : AES.encrypt(tenantConfigDTO.getConfigs().get(passwordKey).toString(), password))
                .servicePath(Objects.isNull(tenantConfigDTO.getConfigs().get(servicePathKey)) ? null : tenantConfigDTO.getConfigs().get(servicePathKey).toString())
                .URI(Objects.isNull(tenantConfigDTO.getConfigs().get(URIKey)) ? null : tenantConfigDTO.getConfigs().get(URIKey).toString())
                .targetMessageUsers(Objects.isNull(tenantConfigDTO.getConfigs().get(targetMessageUsersKey)) ? new HashSet<>()
                        : ((Collection<?>) tenantConfigDTO.getConfigs().get(targetMessageUsersKey)).stream().map(Object::toString).collect(Collectors.toSet()))
                .targetMessageGroups(Objects.isNull(tenantConfigDTO.getConfigs().get(targetMessageGroupsKey)) ? new HashSet<>()
                        : ((Collection<?>) tenantConfigDTO.getConfigs().get(targetMessageGroupsKey)).stream().map(Object::toString).collect(Collectors.toSet()))
                .properties(Objects.isNull(tenantConfigDTO.getConfigs().get(propertiesKey)) ? new HashMap<>()
                        : ((Map<?, ?>) tenantConfigDTO.getConfigs().get(propertiesKey)).entrySet().stream()
                        .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue())))
                .rowsPerPage(Objects.isNull(tenantConfigDTO.getConfigs().get(rowsPerPageKey)) ? null : (Integer) tenantConfigDTO.getConfigs().get(rowsPerPageKey))
                // Todo : implement  automaticIntrastatRelevant
                //.automaticIntrastatRelevant(Objects.isNull(tenantConfigDTO.getConfigs().get(automaticIntrastatRelevantKey)) ? null : (Boolean) tenantConfigDTO.getConfigs().get(automaticIntrastatRelevantKey))
                .countryCode(Objects.isNull(tenantConfigDTO.getConfigs().get(countryCodeKey)) ? null : (String) tenantConfigDTO.getConfigs().get(countryCodeKey))
                .thingsToSync(tenantConfigDTO.getElements().stream().map(el -> dataToSyncMapper.mapRequestToEntity(el)).collect(Collectors.toSet())).build();
    }
}
