package eu.company.connector.sapbydesignbridge.service.sap.entity;


import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import com.sap.cloud.sdk.datamodel.odata.helper.ModificationResponse;

import eu.company.connector.sapbydesignbridge.mapper.sap.MaterialMapper;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.ModuleProperty;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.sap.*;
import eu.company.connector.sapbydesignbridge.service.sap.layer.DestinationByDHolder;
import eu.company.connector.sapbydesignbridge.service.sap.request.RequestService;
import eu.companys.commons.mqtt.dto.entity.get.ItemGetDTO;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.ModelType;
import eu.companys.connector.lib.bydmaterialodata.namespaces.material.Material;
import eu.companys.connector.lib.bydmaterialodata.namespaces.material.Sales;
import eu.companys.connector.lib.bydmaterialodata.services.DefaultMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MaterialServiceEntity extends ASyncData<Material> implements ISyncData /*, IInsertOrUpdate<Material>*//*, IStringFilter*/ {

    @Autowired
    private RequestService requestService;
    @Autowired
    private MaterialMapper itemMapper;
    //private ItemMapper itemMapper;

    @Autowired
    private RemapService remapService;
    @Autowired
    public MaterialServiceEntity() {
        super(Material.class);
    }
    protected MaterialServiceEntity(Class<Material> type) {
        super(type);
    }
    private final static Set<String> fixFields = new HashSet<>(Arrays.asList(
            Material.INTERNAL_ID.getFieldName(),
            Material.DESCRIPTION.getFieldName(),
            Material.BASE_MEASURE_UNIT_CODE.getFieldName(),
            Material.BASE_MEASURE_UNIT_CODE_TEXT.getFieldName(),
            Material.CREATION_DATE_TIME.getFieldName(),
            Material.DESCRIPTION_LANGUAGE_CODE.getFieldName(),
            Material.DESCRIPTION_LANGUAGE_CODE_TEXT.getFieldName(),
            Material.IDENTIFIED_STOCK_TYPE_CODE.getFieldName(),
            Material.IDENTIFIED_STOCK_TYPE_CODE_TEXT.getFieldName(),
            Material.LAST_CHANGE_DATE_TIME.getFieldName(),
            Material.OBJECT_ID.getFieldName(),
            Material.PLANNING_MEASURE_UNIT_CODE.getFieldName(),
            Material.PLANNING_MEASURE_UNIT_CODE_TEXT.getFieldName(),
            Material.SERIAL_NUMBER_PROFILE_CODE.getFieldName(),
            Material.SERIAL_NUMBER_PROFILE_CODE_TEXT.getFieldName()
    ));

    /*
    *  Todo:  They will need to be implemented in a major phase
    * */
    private final static Set<DataLoaderConfiguration> fixCollections = new HashSet<>(Arrays.asList(
//            DataLoaderConfiguration.builder()
//                    .reference("ITEM_WAREHOUSE_INFO_COLLECTION").properties(new HashSet<>(Collections.singleton(ItemWarehouseInfo.WAREHOUSE_CODE.getFieldName()))).build(),
//            DataLoaderConfiguration.builder()
//                    .reference("ITEM_BAR_CODE_COLLECTION").properties(new HashSet<>(
//                            Arrays.asList(ItemBarCode.BARCODE.getFieldName(), ItemBarCode.UO_M_ENTRY.getFieldName()))).build(),
//            DataLoaderConfiguration.builder()
//                    .target("ItemPrice")
//                    .reference("ITEM_PRICES").properties(new HashSet<>(
//                            Arrays.asList(ItemPrice.PRICE.getFieldName(), ItemPrice.PRICE_LIST.getFieldName())))
//                    .elements(new HashSet<>(Collections.singleton(DataLoaderConfiguration.builder()
//                            .reference("UO_M_PRICES").properties(new HashSet<>(
//                                    Arrays.asList(UoMPrice.PRICE.getFieldName(), UoMPrice.UO_M_ENTRY.getFieldName())))
//                            .build()))).build()
    ));

    /*
    * Todo : They will need to be implemented in a next phase
    * */
    private final static Map<ModelType, ModuleProperty> fixModules = new HashMap<>() {{
//        put(ModelType.RETAIL, SinUpRetail.fixModuleItem);
//        put(ModelType.SEARCH, SinUpSearch.fixModuleItem);
    }};

    private final static Set<DataLoaderConfiguration> fixCustomEntities = new HashSet<>(Arrays.asList(
            DataLoaderConfiguration.builder()
                    .target(ElementLoader.GENERIC.name())
                    .reference(MaterialMapper.unitOfMeasurementEntity)
                    .build(),
            DataLoaderConfiguration.builder()
                    .target(ElementLoader.GENERIC.name())
                    .reference(MaterialMapper.manufacturerEntity)
                    .build()));




    /*
    This method retrieves the Materials to send them to MQTT.
    The section where remapping of objects associated with the main element is performed is commented out
    (commented because version V2 does not allow the use of specialized V4 classes for SQL management, namely: SQLQuery, SQLQueryResult, SQLView).
    * * */
    @Override
    public Set<?> getData(Tenant tenant, BridgeConfiguration bridgeConfiguration, int pageNumber, LocalDateTime lastSync, LocalDateTime nowSync, DataLoaderConfiguration element) throws Exception {

        // Todo: They will need to be implemented in a major phase
//
//        Map<String, Object> customEntities = remapService.getCustomEntity(tenant.getName(), bridgeConfiguration,
//                Stream.concat(fixCustomEntities.stream(), element.getElements().stream()).collect(Collectors.toSet()));
//        customEntities.put(ItemMapper.vatGroupEntity, vatGroupService.getData(tenant, bridgeConfiguration, 0, null, null,
//                DataLoaderConfiguration.builder().target(ElementLoader.VAT_GROUP.name()).build()));

        // Todo : They will need to be implemented in a major phase

//        return ((Collection<?>) requestService.getResponse(tenant.getName(), bridgeConfiguration, (metadata) -> metadata.getAllItems()
//                .select(prepareQuery(tenant, element, fixFields, fixCollections, fixModules, bridgeConfiguration.getProperties()))
//                .filter(timeFilter(lastSync, nowSync))
//                .top(bridgeConfiguration.getRowsPerPage())
//                .skip(pageNumber * bridgeConfiguration.getRowsPerPage())
//                .orderBy(Item.CREATE_DATE.asc(), Item.CREATE_TIME.asc())))
//                .stream().map(i -> itemMapper.mapEntityToResponse((Item) i, element, customEntities, fixModules, bridgeConfiguration.getProperties())).collect(Collectors.toSet());


        // Chimata semplice, facendo restituire tutto senza al momento impostare query
        Collection<Material> materialCollection = getMaterials(tenant,bridgeConfiguration);
        var itemGetDTOStream = materialCollection
                .stream()
                .map(i ->
                        itemMapper.mapEntityToResponse((Material) i, element, null, fixModules, bridgeConfiguration.getProperties()));
         Collection<?> collect = itemGetDTOStream.collect(Collectors.toSet());
        return (Set<?>) collect;

    }

    @Autowired
    private DefaultMaterialService defaultMaterialService;



    public Collection<Material> getMaterials(final Tenant tenant, final BridgeConfiguration bridgeConfiguration) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        List<Material> materials = null;
        materials = defaultMaterialService.withServicePath(bridgeConfiguration.getServicePath())
                .getAllMaterialCollection()
                .executeRequest(destination);
        return materials;
    }



    public Material getMaterialByProductID(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, String productID) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        List<Material> material = null;
        material = defaultMaterialService.withServicePath(bridgeConfiguration.getServicePath())
                .materialQueryByDescription("1", "0", productID, "*")
                .executeRequest(destination);
        return material.isEmpty() ? null : material.get(0);
    }

    public Material getMaterialByDescription(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, String productDescription) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        List<Material> material = null;
        material = defaultMaterialService.withServicePath(bridgeConfiguration.getServicePath())
                .materialQueryByDescription("1", "0", "*", productDescription)
                .executeRequest(destination);
        return material.isEmpty() ? null : material.get(0);
    }


    public Material createMaterial(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, Material material) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        ModificationResponse<Material> materialresp = null;
        materialresp = new DefaultMaterialService().withServicePath(bridgeConfiguration.getServicePath())
                .createMaterialCollection(material)
                .executeRequest(destination);
        Material materialCreated = null;
        for (Iterator<Material> iterator = materialresp.getResponseEntity().iterator(); iterator.hasNext(); ) {
            materialCreated = iterator.next();
        }
        return materialCreated;
    }

    public Material updateMaterial(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, Material material) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        ModificationResponse<Material> materialResp = null;
        materialResp = defaultMaterialService.withServicePath(bridgeConfiguration.getServicePath())
                .updateMaterialCollection(material)
                .executeRequest(destination);
        Material materialUpdated = materialResp.getModifiedEntity();
        return materialUpdated;

    }

    public void deleteMaterial(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, Material material) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);
        ModificationResponse<Material> materialresp = null;
        materialresp = new DefaultMaterialService().withServicePath(bridgeConfiguration.getServicePath())
                .deleteMaterialCollection(material)
                .executeRequest(destination);
    }

    @Deprecated
    public void materialSalesQueryByElements(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, Map<String, Object> options, String entry) throws Exception {

        HttpDestination destination = getHttpDestination(tenant, bridgeConfiguration);

        final List<Sales> businessPartners =
                defaultMaterialService.withServicePath(bridgeConfiguration.getServicePath())
                        .materialSalesQueryByElements("1000", "1", "01", "01", "0001", "")
                        .executeRequest(destination);

    }

    /*
    * https://sap.github.io/cloud-sdk/docs/java/features/connectivity/destination-service#http-destinations
    * Create a destination at runtime and register it so that it will be available via DestinationAccessor.getDestination()
    *
    *
    * Call erro due to:
    * https://stackoverflow.com/questions/71180569/could-not-get-httpclient-cache-no-threadcontext-available-for-thread-id-1
    *
    * Todo : dioa  ettere design of httpDestination
    *   recode with a method like this:
    *   MessageException refreshToken(DefaultHttpDestination defaultHttpDestination, BridgeConfiguration bridgeConfiguration)
    *
    * */
    private HttpDestination getHttpDestination(Tenant tenant, BridgeConfiguration bridgeConfiguration) {
        //return DestinationByDHolder.getHttpDestination(tenant, bridgeConfiguration);

        try {

            HttpDestination httpDestination = DestinationAccessor.getDestination(bridgeConfiguration.getTenant()).asHttp();
            return httpDestination;

        } catch (Exception notFound){


            HttpDestinationProperties httpDestinationProperties = bridgeConfiguration.getHttpDestination();

            DefaultHttpDestination customHttpDestination =  DefaultHttpDestination
                    .builder( httpDestinationProperties)
                    .name( tenant.getName())
                    .build();
            //

            DefaultDestinationLoader customLoader = new DefaultDestinationLoader()
                    .registerDestination(customHttpDestination);
            DestinationAccessor.appendDestinationLoader(customLoader);

            HttpDestination httpDestination = DestinationAccessor.getDestination(tenant.getName()).asHttp();

            //Todo: delete on major versions
            bridgeConfiguration.setTenant(tenant.getName());

            return httpDestination;


        }
    }


}