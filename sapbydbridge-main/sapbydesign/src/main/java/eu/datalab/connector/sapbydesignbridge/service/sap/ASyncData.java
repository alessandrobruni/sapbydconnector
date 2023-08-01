package eu.company.connector.sapbydesignbridge.service.sap;

import com.sap.cloud.sdk.datamodel.odata.helper.VdmObject;
import com.sap.cloud.sdk.datamodel.odatav4.core.ComplexProperty;
import com.sap.cloud.sdk.datamodel.odatav4.core.NavigationProperty;
import com.sap.cloud.sdk.datamodel.odatav4.core.Property;
import com.sap.cloud.sdk.datamodel.odatav4.core.SimpleProperty;
import com.sap.cloud.sdk.datamodel.odatav4.expression.FilterableBoolean;
import digital.vianello.schnecke.exceptions.NotSupportedException;
import eu.company.connector.sapbydesignbridge.mapper.sap.AEntityMapper;
import eu.company.connector.sapbydesignbridge.model.ModuleProperty;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.sap.entity.MaterialServiceEntity;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.ModelType;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ASyncData<Entity extends VdmObject<?>> {
    protected final Class<Entity> type;

    protected ASyncData(Class<Entity> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    protected Property<Entity>[] prepareQuery(Tenant tenant, DataLoaderConfiguration dataLoaderConfiguration,
                                              Set<String> fixFields, Set<DataLoaderConfiguration> fixCollections,
                                              Map<ModelType, ModuleProperty> fixModules,
                                              Map<String, String> remapProperties) throws Exception {
        dataLoaderConfiguration.getProperties().addAll(fixFields);
        setModuleProperty(tenant, dataLoaderConfiguration, fixModules, remapProperties);
        setFixCollections(dataLoaderConfiguration, fixCollections);
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(createQuery(dataLoaderConfiguration, type)))).toArray(new Property[0]);
    }

    @SuppressWarnings("unchecked")
    protected FilterableBoolean<Entity> timeFilter(final LocalDateTime lastSync, final LocalDateTime nowSync) throws Exception {
        return ((SimpleProperty.Date<Entity>) type.getField("CREATE_DATE").get(null)).greaterThan(lastSync.toLocalDate())
                .or((((SimpleProperty.Date<Entity>) type.getField("CREATE_DATE").get(null)).equalTo(lastSync.toLocalDate())
                        .and(((SimpleProperty.Time<Entity>) type.getField("CREATE_TIME").get(null)).greaterThanEqual(lastSync.toLocalTime()))))
                .or((((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).notEqualToNull()
                        .and(((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).greaterThan(lastSync.toLocalDate()))))
                .or((((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).notEqualToNull()
                        .and(((SimpleProperty.Time<Entity>) type.getField("UPDATE_TIME").get(null)).notEqualToNull()
                                .and(((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).equalTo(lastSync.toLocalDate()))
                                .and(((SimpleProperty.Time<Entity>) type.getField("UPDATE_TIME").get(null)).greaterThanEqual(lastSync.toLocalTime())))))
                .and(((SimpleProperty.Date<Entity>) type.getField("CREATE_DATE").get(null)).lessThan(nowSync.toLocalDate())
                        .or((((SimpleProperty.Date<Entity>) type.getField("CREATE_DATE").get(null)).equalTo(nowSync.toLocalDate())
                                .and(((SimpleProperty.Time<Entity>) type.getField("CREATE_TIME").get(null)).lessThanEqual(nowSync.toLocalTime()))))
                        .or((((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).notEqualToNull()
                                .and(((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).lessThan(nowSync.toLocalDate()))))
                        .or((((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).notEqualToNull()
                                .and(((SimpleProperty.Time<Entity>) type.getField("UPDATE_TIME").get(null)).notEqualToNull()
                                        .and(((SimpleProperty.Date<Entity>) type.getField("UPDATE_DATE").get(null)).equalTo(nowSync.toLocalDate()))
                                        .and(((SimpleProperty.Time<Entity>) type.getField("UPDATE_TIME").get(null)).lessThanEqual(nowSync.toLocalTime()))))));
    }

    @SuppressWarnings("unchecked")
    private Property<?>[] createQuery(DataLoaderConfiguration dataLoaderConfiguration, Class<?> type) throws Exception {
        String ALL_FIELDS = "ALL_FIELDS";
        List<Property<?>> columns = new ArrayList<>();
        if (dataLoaderConfiguration.getProperties().contains(ALL_FIELDS))
            columns.add((Property<?>) type.getField(ALL_FIELDS).get(null));
        else
            columns.addAll(dataLoaderConfiguration.getProperties().stream().map(this::setProp).collect(Collectors.toList()));
        if (Objects.nonNull(dataLoaderConfiguration.getElements()))
            for (DataLoaderConfiguration dlc : dataLoaderConfiguration.getElements()) {
                if (ElementLoader.checkValue(dataLoaderConfiguration.getReference())
                        && ElementLoader.QUERY.compareTo(ElementLoader.valueOf(dataLoaderConfiguration.getTarget())) <= 0)
                    continue;
                Property<?>[] query = createQuery(dlc, getClassFromERPElement(dataLoaderConfiguration.getTarget()));
                if (Objects.nonNull(query))
                    Collections.addAll(columns, query);
            }
        if (Strings.isNotEmpty(dataLoaderConfiguration.getReference()) && Objects.nonNull(type)) {
            Object field = type.getField(dataLoaderConfiguration.getReference()).get(null);
            if (field instanceof ComplexProperty.Collection || field instanceof ComplexProperty.Single) {
                if (!dataLoaderConfiguration.getProperties().contains(((Property<?>) (field)).getFieldName()))
                    return new Property[]{(Property<?>) field};
                else return null;
            } else if (field instanceof NavigationProperty.Single)
                return new Property[]{
                        ((NavigationProperty.Single<?, ?>) field)
                                .select(columns.toArray(new Property[0]))};
            else
                return new Property[]{
                        ((NavigationProperty.Collection<?, ?>) field)
                                .select(columns.toArray(new Property[0]))};
        } else return columns.toArray(new Property[0]);
    }

    private Class<?> getClassFromERPElement(String target) throws Exception {
        try {
            switch (ElementLoader.valueOf(target)) {
//                case VAT_GROUP:
//                    return VatGroup.class;
//                case BUSINESS_PARTNER_GROUP:
//                    return BusinessPartnerGroup.class;
//                case BUSINESS_PARTNER:
//                    return BusinessPartner.class;
//                case WAREHOUSE:
//                    return Warehouse.class;
//                case ITEM_GROUP:
//                    return ItemGroups.class;
                case ITEM:// Todo: By now the MQTT will manage only Material
                    return MaterialServiceEntity.class;
//                    return Item.class;
//                case PRICE_LIST:
//                    return PriceList.class;
//                case WAREHOUSE_LOCATION:
//                    return WarehouseLocation.class;
                default:
                    throw new NotSupportedException();
            }
        } catch (Exception ignored) {
            try {
                return Class.forName(getClassName(target));
            } catch (Exception exception) {
                throw new NotSupportedException(exception.getMessage());
            }
        }
    }

    private String getClassName(String string) {
        final String fullPackage = "eu.companys.connector.lib.servicelayer.namespaces.metadatav4.";
        if (string == null || string.length() == 0) return fullPackage + string;
        char[] c = string.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        String result = new String(c);
        if (result.endsWith("s")) {
            StringBuilder sb = new StringBuilder(result);
            sb.deleteCharAt(sb.length() - 1);
            return fullPackage + sb;
        } else return fullPackage + new String(c);
    }

    private SimpleProperty<Entity> setProp(String fieldName) {
        return new SimpleProperty<>() {
            @Nonnull
            @Override
            public java.lang.String getFieldName() {
                return fieldName;
            }
        };
    }

    private void setFixCollections(DataLoaderConfiguration dataLoaderConfiguration, Set<DataLoaderConfiguration> fixCollections) {
        if (Objects.nonNull(fixCollections))
            for (DataLoaderConfiguration fixCollection : fixCollections)
                setFixCollections(dataLoaderConfiguration.getElements(), fixCollection);
    }

    private void setFixCollections(Set<DataLoaderConfiguration> elements, DataLoaderConfiguration fixCollection) {
        if (elements.contains(fixCollection)) {
            for (DataLoaderConfiguration element : elements) {
                if (element.equals(fixCollection) && Objects.nonNull(fixCollection.getProperties())) {
                    element.getProperties().addAll(fixCollection.getProperties());
                    setFixCollections(element, fixCollection.getElements());
                }
            }
        } else elements.add(fixCollection);
    }

    private void setModuleProperty(Tenant tenant, DataLoaderConfiguration dataLoaderConfiguration, Map<ModelType, ModuleProperty> fixModules, Map<String, String> remapProperties) {
        for (ModelType modelType : tenant.getModels())
            if (fixModules.containsKey(modelType)) {
                ModuleProperty moduleProperty = fixModules.get(modelType);
                setCustomFields(moduleProperty.getCustomFields(), dataLoaderConfiguration, remapProperties);
                setProperties(moduleProperty.getProperties(), dataLoaderConfiguration, remapProperties);
            }
    }

    private void setCustomFields(Set<String> customFields, DataLoaderConfiguration dataLoaderConfiguration, Map<String, String> remapProperties) {
        for (String customField : customFields) {
            boolean found = false;
            for (Map.Entry<String, String> remapProperty : remapProperties.entrySet())
                if (AEntityMapper.getCustomFieldKey(remapProperty.getKey()).equalsIgnoreCase(customField)) {
                    dataLoaderConfiguration.getProperties().add(AEntityMapper.getCustomFieldKey(remapProperty.getValue()));
                    found = true;
                    break;
                }
            if (!found)
                dataLoaderConfiguration.getProperties().add(customField);
        }
    }

    private void setProperties(Map<String, String> properties, DataLoaderConfiguration dataLoaderConfiguration, Map<String, String> remapProperties) {
        final String PropertiesKey = "Properties";
        for (Map.Entry<String, String> property : properties.entrySet())
            if (remapProperties.containsKey(property.getKey()))
                dataLoaderConfiguration.getProperties().add(PropertiesKey + remapProperties.get(property.getKey()));
            else
                dataLoaderConfiguration.getProperties().add(PropertiesKey + property.getValue());
    }

}
