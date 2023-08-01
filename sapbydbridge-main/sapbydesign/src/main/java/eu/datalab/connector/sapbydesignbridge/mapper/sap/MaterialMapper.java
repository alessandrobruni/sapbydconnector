package eu.company.connector.sapbydesignbridge.mapper.sap;

import com.google.common.base.Strings;
import eu.company.connector.sapbydesignbridge.model.ModuleProperty;
import eu.companys.commons.mqtt.dto.entity.get.ItemGetDTO;
import eu.companys.commons.mqtt.dto.entity.get.VatGroupGetDTO;
import eu.companys.commons.mqtt.dto.entity.set.ItemSetDTO;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ItemStatus;
import eu.companys.commons.mqtt.model.ModelType;
import eu.companys.connector.lib.bydmaterialodata.namespaces.material.Material;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/*
* Mappa Material in oggetti Item che vengono salvati nel
* database del MQTT
*
* */
@Component
public class MaterialMapper extends AEntityMapper implements INoteMapper {

    public final static String unitOfMeasurementEntity = "UnitOfMeasurements";
    public final static String vatGroupEntity = "VatGroups";
    public final static String manufacturerEntity = "Manufacturers";

    private final static String absEntryKey = "absEntry";
    private final static String codeKey = "code";
    private final static String manufacturerNameKey = "manufacturerName";
    private final static Integer manualEntry = -1;

    @Value("${service.users[0].bridgeId}")
    private String bridgeId;


        /*
          Todo:  remap all other objects
            Field recoding of Item (BusinessOne) into equivalent Material (ByDesign).
            ItemCode -> InternalID
            ItemName -> Description
            WareHouseInfo -> not present, possible mapping with Sales?
        * */
    public ItemGetDTO mapEntityToResponse(@NotNull final Material item, final DataLoaderConfiguration element, Map<String, Object> customEntities,
                                          Map<ModelType, ModuleProperty> fixModules, Map<String, String> remapProperties) {
        ItemGetDTO itemGetDTO = ItemGetDTO.builder()
                .entry(item.getInternalID())
                .description(Objects.isNull(item.getDescription()) ? item.getInternalID() : item.getDescription())
//                .status(setItemStatus(item, fixModules, remapProperties))
//                .warehouseCodes(Objects.isNull(item.getItemWarehouseInfoCollection()) ?
//                        new HashSet<>() : item.getItemWarehouseInfoCollection().stream().map(ItemWarehouseInfo::getWarehouseCode).filter(warehouseCode -> !Strings.isNullOrEmpty(warehouseCode)).collect(Collectors.toSet()))
//                .itemGroupCodes(Objects.isNull(item.getItemsGroupCode()) ? new HashSet<>() : Collections.singleton(item.getItemsGroupCode().toString()))
//                .department(getCustomField(item, SinUpRetail.departmentKey, remapProperties))
//                .image(getCustomField(item, SinUpRetail.imageKey, remapProperties))
//                .searchDescription(getCustomField(item, SinUpSearch.searchDescriptionKey, remapProperties))
//                .fields(getCustoms(item, element))
                .build();
//        itemGetDTO.setForeignDescription(Objects.isNull(item.getForeignName()) ? itemGetDTO.getDescription() : item.getForeignName());
//        String shortDescription = getCustomField(item, SinUpRetail.shortDescriptionKey, remapProperties);
//        String longDescription = getCustomField(item, SinUpRetail.longDescriptionKey, remapProperties);
//        itemGetDTO.setLongDescription(Objects.isNull(longDescription) ? itemGetDTO.getDescription() : longDescription);
//        itemGetDTO.setShortDescription(Objects.isNull(shortDescription) ? itemGetDTO.getDescription() : shortDescription);
//        Map<String, Object> bufferCustomEntities = new HashMap<>(customEntities);
//        Collection<?> uomEntity = (Collection<?>) bufferCustomEntities.remove(unitOfMeasurementEntity);
//        Collection<?> mEntity = (Collection<?>) bufferCustomEntities.remove(manufacturerEntity);
//        Collection<?> vgEntity = (Collection<?>) bufferCustomEntities.remove(vatGroupEntity);
//        setItemUOM(itemGetDTO, item, uomEntity);
//        setBarcodes(itemGetDTO, item, uomEntity, itemGetDTO.getSaleUOM());
//        setPrices(itemGetDTO, item, uomEntity, itemGetDTO.getSaleUOM());
//        setManufacturer(itemGetDTO, item, mEntity);
//        setSaleVats(itemGetDTO, item, vgEntity, remapProperties);
//        itemGetDTO.getFields().putAll(bufferCustomEntities);
        return itemGetDTO;
    }

    public Material mapRequestToEntity(final @Valid ItemSetDTO itemSetDTO, final String timeZone) throws Exception {
        return setItemValues(null, itemSetDTO, timeZone);
    }

    public Material mapRequestToEntity(@NotNull final Material item, final @Valid ItemSetDTO itemSetDTO, final String timeZone) throws Exception {
        return setItemValues(item, itemSetDTO, timeZone);
    }

    private Material setItemValues(Material item, final ItemSetDTO itemSetDTO, final String timeZone) throws Exception {
        if (Objects.isNull(item)) {
            item = new Material();
            item.setInternalID(itemSetDTO.getEntry());
        }
        setFieldValues(item, itemSetDTO.getFields(), timeZone);
        //item.setUser_Text(addOrUpdate(bridgeId, "Item", item.getUser_Text()));
        return item;
    }

    // Todo: complete remap of all these fields
//    private void setSaleVats(ItemGetDTO itemGetDTO, Item item, Collection<?> entities, Map<String, String> remapProperties) {
//        Set<ItemGetDTO.VAT> result = new HashSet<>();
//        if (Objects.nonNull(entities) && !entities.isEmpty()) {
//            VatGroupGetDTO vatGroupDefault = (VatGroupGetDTO) entities.stream()
//                    .filter(vg -> ((VatGroupGetDTO) vg).getEntry().equals(item.getSalesVATGroup())).findFirst().orElse(null);
//            VatGroupGetDTO vatGroupReduced = (VatGroupGetDTO) entities.stream()
//                    .filter(vg -> ((VatGroupGetDTO) vg).getEntry().equals(getCustomField(item, SinUpRetail.reducedVatKey, remapProperties))).findFirst().orElse(null);
//            if (Objects.nonNull(vatGroupDefault))
//                result.add(getVatGroup(vatGroupDefault, ItemGetDTO.VATType.DEFAULT));
//            if (Objects.nonNull(vatGroupReduced))
//                result.add(getVatGroup(vatGroupReduced, ItemGetDTO.VATType.REDUCED));
//        }
//        itemGetDTO.setSaleVATs(result);
//    }
//
//    private ItemGetDTO.VAT getVatGroup(VatGroupGetDTO vatGroup, ItemGetDTO.VATType type) {
//        return ItemGetDTO.VAT.builder()
//                .code(vatGroup.getEntry())
//                .type(type)
//                .percentage(vatGroup.getPercentage())
//                .countryCode(vatGroup.getCountryCode())
//                .build();
//    }
//
//    private String getUOM(Integer uomUnit, Collection<?> entities, String defaultUom) {
//        Map<?, ?> uomMap = (!Objects.isNull(entities) && !entities.isEmpty()) ? (Map<?, ?>) entities.stream().filter(e ->
//                Objects.equals(((Double) ((Map<?, ?>) e).get(absEntryKey)).intValue(), Objects.isNull(uomUnit) ? manualEntry : uomUnit)).findFirst().orElse(null) : null;
//        return (Objects.nonNull(uomMap) && ((Double) uomMap.get(absEntryKey)).intValue() != manualEntry) ? (String) uomMap.get(codeKey) : defaultUom;
//    }
//
//    private void setPrices(ItemGetDTO itemGetDTO, Item item, Collection<?> entities, String saleUOM) {
//        String defaultUom = getUOM(item.getPricingUnit(), entities, saleUOM);
//        itemGetDTO.setPrices(Objects.isNull(item.getItemPrices()) ? new HashSet<>() :
//                item.getItemPrices().stream().flatMap(p -> getUOMPrice(p, entities, defaultUom).stream()).collect(Collectors.toSet()));
//    }
//
//    private Set<ItemGetDTO.Price> getUOMPrice(ItemPrice price, Collection<?> entities, String defaultUom) {
//        String id = price.getPriceList() + "";
//        Set<ItemGetDTO.Price> prices = new HashSet<>();
//        prices.add(ItemGetDTO.Price.builder()
//                .id(id)
//                .price(price.getPrice())
//                .uom(defaultUom).build());
//        if (Objects.nonNull(price.getUoMPrices()))
//            prices.addAll(price.getUoMPrices().stream().map(up -> ItemGetDTO.Price.builder()
//                    .id(id)
//                    .price(up.getPrice())
//                    .uom(getUOM(up.getUoMEntry(), entities, defaultUom)).build()).collect(Collectors.toSet()));
//        return prices;
//    }
//
//    private void setBarcodes(ItemGetDTO itemGetDTO, Item item, Collection<?> entities, String saleUOM) {
//        itemGetDTO.setBarCodes(Objects.isNull(item.getItemBarCodeCollection()) ? new HashSet<>() :
//                item.getItemBarCodeCollection().stream().filter(barcode -> !Strings.isNullOrEmpty(barcode.getBarcode()))
//                        .map(bc -> ItemGetDTO.BarCode.builder()
//                                .code(bc.getBarcode())
//                                .useAsDefault(bc.getBarcode().equals(item.getBarCode()))
//                                .uom(getUOM(bc.getUoMEntry(), entities, saleUOM))
//                                .build()).collect(Collectors.toSet()));
//    }
//
//    private ItemStatus[] setItemStatus(Item item, Map<ModelType, ModuleProperty> fixModules, Map<String, String> remapProperties) {
//        Set<ItemStatus> status = new HashSet<>();
//        if (Objects.equals(item.getValid(), BoYesNoEnum.TYES))
//            status.add(ItemStatus.ACTIVE);
//        else
//            status.add(ItemStatus.INACTIVE);
//        if (Objects.equals(item.getPurchaseItem(), BoYesNoEnum.TYES))
//            status.add(ItemStatus.PURCHASE);
//        if (Objects.equals(item.getSalesItem(), BoYesNoEnum.TYES))
//            status.add(ItemStatus.SALE);
//        if (Objects.equals(item.getNoDiscounts(), BoYesNoEnum.TNO))
//            status.add(ItemStatus.DISCONTABLE);
//        if (getProperty(SinUpRetail.itemRefundablePropertyKey, item, fixModules, remapProperties))
//            status.add(ItemStatus.REFUNDABLE);
//        if (getProperty(SinUpRetail.itemRetailPropertyKey, item, fixModules, remapProperties))
//            status.add(ItemStatus.RETAIL);
//        if (Objects.equals(item.getManageBatchNumbers(), BoYesNoEnum.TYES))
//            status.add(ItemStatus.BATCH);
//        if (Objects.equals(item.getManageSerialNumbers(), BoYesNoEnum.TYES))
//            status.add(ItemStatus.SERIAL);
//        if (Objects.equals(item.getInventoryItem(), BoYesNoEnum.TNO))
//            status.add(ItemStatus.NOT_WAREHOUSE);
//        return status.toArray(new ItemStatus[0]);
//    }
//
//    private void setItemUOM(ItemGetDTO itemGetDTO, Item item, Collection<?> entities) {
//        String inventoryUOM = item.getInventoryUOM();
//        String countingUOM = null;
//        String saleUOM = item.getSalesUnit();
//        String purchaseUOM = item.getPurchaseUnit();
//        if (!Objects.isNull(entities) && !entities.isEmpty()) {
//            Map<?, ?> groupUOM = (Map<?, ?>) entities.stream().filter(e -> Objects.equals(((Double) ((Map<?, ?>) e).get(absEntryKey)).intValue(), item.getUoMGroupEntry())).findFirst().orElse(null);
//            if (Objects.nonNull(groupUOM) && !Objects.equals(((Double) groupUOM.get(absEntryKey)).intValue(), manualEntry)) {
//                inventoryUOM = getUOM(item.getInventoryUoMEntry(), entities, null);
//                countingUOM = getUOM(item.getDefaultCountingUoMEntry(), entities, null);
//                saleUOM = getUOM(item.getDefaultSalesUoMEntry(), entities, null);
//                purchaseUOM = getUOM(item.getDefaultPurchasingUoMEntry(), entities, null);
//            }
//        }
//        itemGetDTO.setSaleUOM(Objects.isNull(saleUOM) ? inventoryUOM : saleUOM);
//        itemGetDTO.setInventoryUOM(Objects.isNull(countingUOM) ? inventoryUOM : countingUOM);
//        itemGetDTO.setPurchaseUOM(purchaseUOM);
//    }
//
//    private void setManufacturer(ItemGetDTO itemGetDTO, Item item, Collection<?> entities) {
//        if (Objects.isNull(entities) || entities.isEmpty()) return;
//        Map<?, ?> manufacturerMap = (Map<?, ?>) entities.stream().
//                filter(e -> Objects.equals(((Double) ((Map<?, ?>) e).get(codeKey)).intValue(), item.getManufacturer())).findFirst().orElse(null);
//        if (Objects.nonNull(manufacturerMap))
//            itemGetDTO.setManufacturer(manufacturerMap.get(manufacturerNameKey) + "");
//    }
}
