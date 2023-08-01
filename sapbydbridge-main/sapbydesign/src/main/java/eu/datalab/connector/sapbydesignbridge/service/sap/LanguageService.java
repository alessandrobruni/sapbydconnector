package eu.company.connector.sapbydesignbridge.service.sap;

import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.sap.layer.DefaultMetadataByDService;
import eu.company.connector.sapbydesignbridge.service.sap.request.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LanguageService {

    public static final Integer defaultLanguageCodeSBO = 3;

    public static final String defaultLanguageCodeISO = "en_US";

    @Autowired
    private RequestService requestService;

    public static String sboToIso(Integer code, Map<String, Integer> languageMap) {
        if (Objects.isNull(code)) return defaultLanguageCodeISO;
        String sboLanCode = languageMap.entrySet().stream().filter(e -> Objects.equals(e.getValue(), code)).map(Map.Entry::getKey).findFirst().orElse(null);
        if (Objects.isNull(sboLanCode)) return defaultLanguageCodeISO;
        switch (sboLanCode) {
            case "he":
            case "pl":
            case "de":
            case "it":
            case "hu":
            case "nl":
            case "fi":
            case "el":
            case "pt":
            case "fr":
            case "es":
            case "ru":
            case "sk":
            case "ko":
            case "ja":
            case "tr":
            case "ar":
                return sboLanCode;
            case "gb":
                return "en_" + sboLanCode.toUpperCase();
            case "dk":
                return "da_" + sboLanCode.toUpperCase();
            case "no":
                return "nb_" + sboLanCode.toUpperCase();
            case "tw":
            case "cn":
                return "zh_" + sboLanCode.toUpperCase();
            case "se":
                return "sv_" + sboLanCode.toUpperCase();
            case "co":
                return "es_" + sboLanCode.toUpperCase();
            case "cz":
                return "cs_" + sboLanCode.toUpperCase();
            case "br":
                return "pt_" + sboLanCode.toUpperCase();
            case "ua":
                return "uk_" + sboLanCode.toUpperCase();
            case "en":
            default:
                return defaultLanguageCodeISO;
        }
    }

    /*
    * Todo : manege all languages codes
    * */
    public Map<String, Integer> loadAvailableLanguages(String tenantName, BridgeConfiguration bridgeConfiguration) throws Exception {
//        return ((Collection<?>) requestService.getResponse(tenantName, bridgeConfiguration, DefaultMetadataByDService::getAllUserLanguages)).stream().collect(Collectors.toMap(
//                e -> Objects.requireNonNull(((UserLanguage) e).getLanguageShortName()).toLowerCase(),
//                e -> Objects.requireNonNull(((UserLanguage) e).getCode())
//        ));
        Map<String, Integer> dummyMap = new HashMap<>();
        dummyMap.put("en_US", 3);
        return dummyMap;
    }

    public void setLanguageCode(Tenant tenant, BridgeConfiguration bridgeConfiguration) throws Exception {
        bridgeConfiguration.setLanguage(getSBOLanguageId(tenant.getName(),
                bridgeConfiguration, tenant.getLanguage()).toString());
    }

    public Integer getSBOLanguageId(String tenantName, BridgeConfiguration bridgeConfiguration, String code) throws Exception {
        return isoToSBO(code, loadAvailableLanguages(tenantName, bridgeConfiguration));
    }

    private Integer isoToSBO(String code, Map<String, Integer> languageMap) {
        Integer sapB1LanguageCode = defaultLanguageCodeSBO;
        if (Objects.isNull(code)) return sapB1LanguageCode;
        String[] splitStr = code.toLowerCase().split("_");
        if (splitStr.length == 2 && languageMap.containsKey(splitStr[1]))
            sapB1LanguageCode = languageMap.get(splitStr[1]);
        else if (languageMap.containsKey(splitStr[0])) sapB1LanguageCode = languageMap.get(splitStr[0]);
        return sapB1LanguageCode;
    }
}
