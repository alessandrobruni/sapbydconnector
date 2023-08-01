package eu.company.connector.sapbydesignbridge;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.sap.entity.MaterialServiceEntity;
import eu.companys.connector.lib.bydmaterialodata.namespaces.material.Material;
import eu.companys.connector.lib.bydmaterialodata.services.DefaultMaterialService;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@Log4j2
public class TestMaterials {

    public Tenant tenant;
    public BridgeConfiguration bridgeConfiguration;

    public MaterialServiceEntity materialServiceEntity ;


    @Test
    public  void loadBridge() throws ODataException {

//        tenant = new Tenant("fluoritalCollaudo", null, null, null,null);
//        bridgeConfiguration = BridgeConfiguration.builder()
//                .tenant(tenant.getName())
//                .URL("https://my354912.sapbydesign.com")
//                .servicePath("sap/byd/odata/cust/v1/vmumaterial/")
//                .connectionType("HTTPS")
//                .authenticationType("BasicAuthentication")
//                .userName("Administration01")
//                .userPassword("Welcome1")
//                .build();
//        log.info("Tenant e Bridge istanziati");

        // crea entity con contruttore il servizio ODATA
        //da capire se va fatta  in maniera alternativa con la notazione Spring
        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());
    }

    @Test
    public void testMaterialsAll(){
        log.info("++++++++++++++++++++++++++++++++ get MATERIAL ALL  ++++++++++++++++++++++++++++++++  ");

        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());

        try {
            loadBridge();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }

        Collection<Material> materials;
        try {
             materials = materialServiceEntity.getMaterials(tenant,bridgeConfiguration);

            if (materials != null && !materials.isEmpty()) {
                log.info("++++++++++++++++++++++++++++++++ primi 10 ");
                materials.stream()
                        .limit(10)
                        .forEach(material -> System.out.println(material));
            }
            } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Test
    public void testMaterialsProductID(){
        log.info("++++++++++++++++++++++++++++++++  MATERIAL BY PRODUCT ID ++++++++++++++++++++++++++++++++  ");

        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());

        try {
            loadBridge();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }

        Material material;
        String IDMaterial= "P100129";
        try {

            material = materialServiceEntity.getMaterialByProductID(tenant,bridgeConfiguration,IDMaterial);

            log.info("++++++++++++++++++++++++++++++++ materiale con chiave  " + IDMaterial);
            if (material != null ) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                log.info( objectMapper.writeValueAsString(material));

            }else{
                log.info("non trovato");
            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
    @Test
    public void testMaterialsDescription(){
        log.info("++++++++++++++++++++++++++++++++  MATERIAL BY DESCRIPTION  ++++++++++++++++++++++++++++++++  ");

        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());

        try {
            loadBridge();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }

        Material material;
        String descrizione= "Heater 5*";
        try {

            material = materialServiceEntity.getMaterialByDescription(tenant,bridgeConfiguration,descrizione);

            log.info("++++++++++++++++++++++++++++++++ materiale con descrizione  " + descrizione);
            if (material != null ) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                log.info( objectMapper.writeValueAsString(material));

            }else{
                log.info("non trovato");
            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Test
    public void testCreateMaterial(){
        log.info("++++++++++++++++++++++++++++++++ CREATE MATERIAL  ++++++++++++++++++++++++++++++++  ");

        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());

        try {
            loadBridge();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }

        try {

            //dati di un prodotto
            //{"InternalID":"Test129","Description":"Test Cylinder 77",
            // "DescriptionLanguageCode":"EN","DescriptionLanguageCodeText":"English",
            // "BaseMeasureUnitCode":"EA","BaseMeasureUnitCodeText":"Each",
            // "IdentifiedStockTypeCode":"01","IdentifiedStockTypeCodeText":"Batch",
            // "SerialNumberProfileCode":"1003","SerialNumberProfileCodeText":"No Serial Number Assignment",
            // "CreationDateTime":"/Date(1289232292711)/",
            // "LastChangeDateTime":"/Date(1563781029205)/",
            // "PlanningMeasureUnitCode":"EA","PlanningMeasureUnitCodeText":"Each",
            // "ValuationLevelTypeCode":"1","ValuationLevelTypeCodeText":"Business Residence"}"
            Material materialNew = new Material();
            materialNew.setInternalID("Test129_7");
            materialNew.setDescription("Test Cylinder 776");
            materialNew.setBaseMeasureUnitCode("EA");
            materialNew.setBaseMeasureUnitCodeText("Each");
            materialNew.setIdentifiedStockTypeCode("01");
            materialNew.setIdentifiedStockTypeCodeText("Batch");
            materialNew.setSerialNumberProfileCode("1003");
            materialNew.setSerialNumberProfileCodeText("No Serial Number Assignment");
            materialNew.setPlanningMeasureUnitCode("EA");
            materialNew.setPlanningMeasureUnitCodeText("Each");

            Material materialCreated =  materialServiceEntity.createMaterial(tenant,bridgeConfiguration,materialNew);

            if(materialCreated!=null) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                log.info("++++++++++++++++++++++++++++++++ creato materiale con ID  " + materialCreated.getInternalID());
                log.info("++++++++++++++++++++++++++++++++ " + objectMapper.writeValueAsString(materialCreated));

            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
            if(ignored instanceof ODataException){
                log.info(ignored.getCause());
                log.info(ignored.getMessage());
            }
        }
    }
    @Test
    public void testUpdateMaterial(){
        log.info("++++++++++++++++++++++++++++++++ UPDATE MATERIAL  ++++++++++++++++++++++++++++++++  ");

        //materialServiceEntity = new MaterialServiceEntity(new DefaultMaterialService());

        try {
            loadBridge();
        } catch (ODataException e) {
            throw new RuntimeException(e);
        }

        try {

            //recupera un prodotto
            //VERIFICA PERCHé SI INCHIODA SE SI PASSA PRIMA DAL RECUPERO DI UN MATERIAL
            //Material materialRetrive = materialServiceEntity.getMaterialByProductID(tenant,bridgeConfiguration,"Test129_6");
            //ObjectMapper objectMapper = new ObjectMapper();
            //Material materialUpdate =objectMapper.readValue(objectMapper.writeValueAsString(materialRetrive), Material.class);

            Material materialUpdate = new Material() ;
            materialUpdate.setObjectID("8D72A05E815B1EEE8994E9D552DC25F1");//8D72A05E815B1EEE8994E9D552DC25F1
            materialUpdate.setDescription("Modified22 Test Cylinder 77");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            log.info("++++++++++++++++++++++++++++++++ " + objectMapper.writeValueAsString(materialUpdate));

            Material materialUpdated =  materialServiceEntity.updateMaterial(tenant,bridgeConfiguration,materialUpdate);

            if(materialUpdated!=null) {

                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                log.info("++++++++++++++++++++++++++++++++ creato materiale con ID  " + materialUpdated.getInternalID());
                log.info("++++++++++++++++++++++++++++++++ " + objectMapper.writeValueAsString(materialUpdated));

            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
            if(ignored instanceof ODataException){
                log.info(ignored.getCause());
                log.info(ignored.getMessage());

            }
        }
    }

}
