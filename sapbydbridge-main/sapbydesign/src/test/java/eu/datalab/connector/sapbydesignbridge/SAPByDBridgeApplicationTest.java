package eu.company.connector.sapbydesignbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import digital.vianello.schnecke.util.DateUtil;
import eu.company.connector.sapbydesignbridge.config.ServiceConfig;
import eu.company.connector.sapbydesignbridge.mapper.TenantMapper;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.TenantService;
import eu.company.connector.sapbydesignbridge.service.sap.entity.MaterialServiceEntity;
import eu.companys.commons.mqtt.config.Mqtt;
import eu.companys.commons.mqtt.dto.TenantConfigDTO;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.OperationReport;
import eu.companys.commons.mqtt.model.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties({ServiceConfig.class})
@ComponentScan({"com.sap.cloud.sdk", "eu.company.connector.sapbydesignbridge"})
public class SAPByDBridgeApplicationTest {
    @Autowired
    private TenantService tenantService;
    @Autowired
    public TenantMapper tenantMapper;

    @Autowired
    private Mqtt mqtt;
    @Autowired
    private MaterialServiceEntity materialService;
    private final String tenantDev = "fluoritalCollaudo";

    @Value("${service.users[0].bridgeId}")
    private String bridgeId;
    @Value("${service.storage.password}")
    private String password;
    @Autowired
    private ObjectMapper objectMapper;


    /*
    * {"userName":"Administration01","userPassword":"HmnUIMKorLL1AOOrTl1vAaicd9RZtdzF929SqP3_wds",
    * "uri":"https://my354912.sapbydesign.com","authenticationType":"BasicAuthentication",
    * "rowsPerPage":500,"connectionTypes":"HTTPS","servicePath": "sap/byd/odata/cust/v1/vmumaterial/",
    * "thingsToSync":[
    * {"dataLoaderConfiguration":
    *   {"reference":"",
    *   "properties":[],
    *   "target":"ITEM",
    *   "elements":[],
    *   "options":{}},
    *   "lastSyncDate":0,
    *   "minutesSyncInterval":1000}
    * ]}
    * */

    @Test
    public void loadTenant() throws Exception {
        Tenant tenant = tenantService.findByName(tenantDev);
      System.out.println("Ciao");
    }
//    @Test
//    public void testTenant() throws Exception {
//        String bridgeIdStaging = "SAPB1Bridge-staging";//
//        String payload = "{\"tenant\":\"fluoritalProduzione\",\"language\":\"it_IT\",\"timeZone\":\"Europe/Rome\"," +
//                "\"configs\":{\"companyDb\":\"SOGENASRL\",\"trustCertificate\":false,\"targetMessageGroups\":[]," +
//                "\"userPassword\":\"200568x72\",\"countryCode\":\"IT\"," +
//                "\"servicePath\":\"b1s/v2/\",\"proxyURI\":null," +
//                "\"targetMessageUsers\":[]," +
//                "\"userName\":\"manager\"," +
//                "\"URI\":\"https://195.39.195.4:50000\"," +
//                "\"rowsPerPage\":1000," +
//                "\"properties\":{\"itemRetail\":\"8\",\"itemRefundable\":\"9\",\"U_SINUP_PIPPO\":\"U_PIPPO\"}}," +
//                "\"models\":[\"RETAIL\"]," +
//                "\"elements\":[{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"BUSINESS_PARTNER_GROUP\",\"elements\":[],\"options\":{}}},{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"BUSINESS_PARTNER\",\"elements\":[],\"options\":{}}},{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"PRICE_LIST\",\"elements\":[],\"options\":{}}},{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"ITEM_GROUP\",\"elements\":[],\"options\":{}}},{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"ITEM\",\"elements\":[],\"options\":{}}},{\"minutesSyncInterval\":1440,\"options\":{},\"dataLoaderConfiguration\":{\"reference\":null,\"properties\":[],\"target\":\"VAT_GROUP\",\"elements\":[],\"options\":{}}}]}";
//        TenantConfigDTO tenantConfigDTO = objectMapper.readValue(payload, TenantConfigDTO.class);
////        tenantService.createOrUpdate(tenantMapper.mapRequestToEntity(tenantConfigDTO),
////                bridgeIdStaging, tenantMapper.mapRequestToBridgeConfiguration(tenantConfigDTO));
//    }


    @Test
    public void getItem() throws Exception {
        Tenant tenant = tenantService.findByName(tenantDev);
        BridgeConfiguration bridgeConfiguration = tenantService.getBridgeConfiguration(tenant, bridgeId);
        Set<?> entities = null;
        try {
            entities = (Set<?>) materialService.getData(tenant, bridgeConfiguration, 0, LocalDateTime.of(1969, 7, 20, 20, 17, 40), DateUtil.now(),
                    DataLoaderConfiguration.builder()
                            .target(ElementLoader.ITEM.name())
                            .elements(new HashSet<>())
                            .properties(new HashSet<>())
                            .build());

             mqtt.sendMessage(tenant.getName(),
                     OperationReport.builder()
                             .bridgeId(bridgeId)
                             .type(OperationType.SYNC)
                             .elementLoader(ElementLoader.ITEM)
                     .build(), entities);

        } catch (Exception e) {

            e.printStackTrace();;
//            logService.error(tenant, bridgeConfiguration, OperationReport.builder()
//                    .reference(null)
//                    .elementLoader(ElementLoader.ITEM)
//                    .type(OperationType.SYNC)
//                    .build(), e);
        }
        System.out.println(objectMapper.writeValueAsString(entities));
    }


}
