package eu.company.connector.sapbydesignbridge.mapper.sap;

import com.sap.cloud.sdk.datamodel.odata.helper.VdmObject;
import digital.vianello.schnecke.exceptions.BadRequestException;
import digital.vianello.schnecke.util.DateUtil;
import eu.company.connector.sapbydesignbridge.model.ModuleProperty;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.ModelType;
import io.vavr.control.Option;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.constraints.NotNull;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public abstract class AEntityMapper {

    static final public String equalsKey = "equals";

    static final public String removeKey = "remove";

    static final public String removeAllKey = "removeAll";

    static public String getCustomFieldKey(String key) {
        if (!key.startsWith("U_"))
            return "U_" + key;
        return key;
    }

    <FieldType> FieldType getCustomField(VdmObject<?> entity, String key, Map<String, String> remapProperties) {
        try {
            String uKey = getCustomFieldKey(key);
            for (Map.Entry<String, String> remapProperty : remapProperties.entrySet())
                if (getCustomFieldKey(remapProperty.getKey()).equalsIgnoreCase(uKey))
                    return entity.getCustomField(getCustomFieldKey(remapProperty.getValue()));
            return entity.getCustomField(uKey);
        } catch (Exception ignored) {
            return null;
        }
    }

    Boolean getProperty(String propertyKey, VdmObject<?> entity, Map<ModelType, ModuleProperty> fixModules, Map<String, String> remapProperties) {
        try {
            String propertyNumber;
            if (remapProperties.containsKey(propertyKey))
                propertyNumber = remapProperties.get(propertyKey);
            else
                propertyNumber = fixModules.entrySet().stream().filter(entry -> entry.getValue().getProperties().containsKey(propertyKey))
                        .findFirst().orElseThrow().getValue().getProperties().get(propertyKey);
            if (Objects.isNull(propertyNumber)) return false;
            Method method = Arrays.stream(entity.getClass().getDeclaredMethods()).filter(p -> p.getName().toLowerCase()
                    .startsWith(("getProperties" + propertyNumber).toLowerCase())).findFirst().orElse(null);
            // NB In ByDesign, there is no BoYesNoEnum in the VEdm mapping of the package.
            //return !Objects.isNull(method) && BoYesNoEnum.TYES.equals(method.invoke(entity));
            return !Objects.isNull(method);
        } catch (Exception ignored) {
            return false;
        }
    }

    Map<String, Object> getCustoms(VdmObject<?> entity, DataLoaderConfiguration element) {
        Map<String, Object> result = getFieldValues(entity, element.getProperties());
        try {
            for (DataLoaderConfiguration dataLoaderConfiguration : element.getElements()) {
                if (ElementLoader.checkValue(dataLoaderConfiguration.getReference())
                        && ElementLoader.QUERY.compareTo(ElementLoader.valueOf(dataLoaderConfiguration.getTarget())) <= 0)
                    continue;
                String fieldNameProperty = "fieldName";
                String columnName = (String) Objects.requireNonNull(new BeanWrapperImpl(
                        entity.getClass().getField(dataLoaderConfiguration.getReference()).get(null))
                        .getPropertyValue(fieldNameProperty));
                Method method = Arrays.stream(entity.getClass().getDeclaredMethods()).filter(p -> p.getName().toLowerCase()
                        .startsWith("get" + columnName.toLowerCase())).findFirst().orElse(null);
                if (Objects.nonNull(method)) {
                    Object obj = method.invoke(entity);
                    obj = obj instanceof Option ? ((Option<?>) obj).getOrNull() : obj;
                    if (Objects.nonNull(obj)) {
                        if (obj instanceof Collection<?>) {
                            List<Map<String, Object>> objList = new ArrayList<>();
                            for (Object o : ((Collection<?>) obj))
                                objList.add(getCustoms((VdmObject<?>) o, dataLoaderConfiguration));
                            result.put(columnName, objList);
                        } else
                            result.put(columnName, getCustoms((VdmObject<?>) obj, dataLoaderConfiguration));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    void setFieldValues(final VdmObject<?> source, @NotNull final Map<?, ?> target, String timeZone) throws Exception {
        BeanWrapper srcSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = srcSource.getPropertyDescriptors();
        for (Map.Entry<?, ?> entry : target.entrySet()) {
            PropertyDescriptor pd = Arrays.stream(pds).filter(p -> p.getName().equalsIgnoreCase((String) entry.getKey())).findFirst().orElse(null);
            if (Objects.nonNull(pd)) {
                if (Collection.class.isAssignableFrom(Objects.requireNonNull(srcSource.getPropertyType(pd.getName()))))
                    addEntityToList(srcSource, pd, (List<?>) entry.getValue(), timeZone);
                else if (entry.getValue() instanceof Map<?, ?>)
                    addEntityToMap(srcSource, pd, (Map<?, ?>) entry.getValue(), timeZone);
                else {
                    var value = getValue(entry.getValue(), pd.getPropertyType(), pd.getName(), timeZone);
                    if (Objects.nonNull(value))
                        srcSource.setPropertyValue(pd.getName(), pd.getPropertyType().cast(value));
                }
            } else
                source.setCustomField(getCustomFieldKey((String) entry.getKey()), entry.getValue());
        }
    }

    Map<String, Object> parseValue(String columnName, Object value) {
        String cName = lowercaseFirst(columnName);
//        if (value instanceof BoYesNoEnum)
//            return Map.of(cName, value.equals(BoYesNoEnum.TYES));
        return Map.of(cName, Objects.isNull(value) ? "" : value);
    }

    private Map<String, Object> getFieldValues(final VdmObject<?> entity, @NotNull final Set<String> columnNames) {
        Map<String, Object> returnMap = new HashMap<>();
        BeanWrapper srcSource = new BeanWrapperImpl(entity);
        PropertyDescriptor[] pds = srcSource.getPropertyDescriptors();
        for (String columnName : columnNames) {
            PropertyDescriptor pd = Arrays.stream(pds).filter(p -> p.getName().equalsIgnoreCase(columnName)).findFirst().orElse(null);
            if (Objects.nonNull(pd))
                returnMap.putAll(parseValue(columnName, srcSource.getPropertyValue(pd.getName())));
            else if (entity.hasCustomField(columnName))
                returnMap.put(columnName, entity.getCustomField(columnName));
        }
        return returnMap;
    }

    private String lowercaseFirst(String string) {
        if (string == null || string.length() == 0) return string;
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private void addEntityToList(BeanWrapper srcSource, PropertyDescriptor pd, List<?> entities, String timeZone) throws Exception {
        List<?> actual = (List<?>) srcSource.getPropertyValue(pd.getName());
        if (Objects.isNull(actual))
            actual = new ArrayList<>();
        Object[] mod = prepareList(srcSource, pd, actual, entities, timeZone);
        Object[] array = new Object[actual.size() + mod.length];
        for (int i = 0; i < actual.size(); i++)
            array[i] = actual.get(i);
        System.arraycopy(mod, 0, array, actual.size(), mod.length);
        srcSource.setPropertyValue(pd.getName(), pd.getPropertyType().cast(Arrays.asList(array)));
    }

    private Object[] prepareList(BeanWrapper srcSource, PropertyDescriptor pd, List<?> actual, List<?> entities, String timeZone) throws Exception {
        Class<?> entityClass = Objects.requireNonNull(srcSource.getPropertyTypeDescriptor(pd.getName())).getResolvableType().getGeneric(0).resolve();
        Object[] buffer = new Object[entities.size()];
        boolean removeAll = false;
        int resultSize = 0;
        if (!(entities.iterator().next() instanceof Map<?, ?>))
            for (Object entity : entities) {
                var value = getValue(entity, entityClass, pd.getName(), timeZone);
                if (!actual.contains(value)) buffer[resultSize++] = value;
            }
        else
            for (Object entityMap : entities) {
                if (removeAll = removeAllFromList((Map<?, ?>) entityMap)) break;
                var entity = getEntity(entityClass, (Map<?, ?>) entityMap, actual, timeZone);
                if (removeEntityFromList((Map<?, ?>) entityMap)) continue;
                setFieldValues((VdmObject<?>) entity, (Map<?, ?>) entityMap, timeZone);
                buffer[resultSize++] = entity;
            }
        if (removeAll) {
            actual.clear();
            return new Object[0];
        }
        Object[] result = new Object[resultSize];
        System.arraycopy(buffer, 0, result, 0, resultSize);
        return result;
    }

    private boolean removeEntityFromList(Map<?, ?> entityMap) {
        return removeFromList(entityMap, removeKey);
    }

    private boolean removeAllFromList(Map<?, ?> entityMap) {
        return removeFromList(entityMap, removeAllKey);
    }

    private boolean removeFromList(Map<?, ?> entityMap, String removeTypeKey) {
        if (entityMap.containsKey(removeTypeKey)) {
            boolean remove = (boolean) entityMap.get(removeTypeKey);
            entityMap.remove(removeTypeKey);
            return remove;
        }
        return false;
    }

    private Object getEntity(Class<?> entityClass, Map<?, ?> entityMap, List<?> actual, String timeZone) throws Exception {
        List<?> equalFields = (List<?>) entityMap.get(equalsKey);
        entityMap.remove(equalsKey);
        Object found = null;
        if (Objects.nonNull(equalFields))
            for (Object o : actual)
                if (equalList(entityMap, o, equalFields, timeZone)) {
                    found = o;
                    break;
                }
        if (Objects.nonNull(found)) {
            actual.remove(found);
            return found;
        } else return entityClass.getDeclaredConstructor().newInstance();
    }

    private boolean equalList(Map<?, ?> entityMap, Object actual, List<?> equalFields, String timeZone) throws Exception {
        BeanWrapper srcSource = new BeanWrapperImpl(actual);
        PropertyDescriptor[] pds = srcSource.getPropertyDescriptors();
        for (Object fields : equalFields) {
            PropertyDescriptor pd = Arrays.stream(pds).filter(p -> p.getName().equalsIgnoreCase((String) fields)).findFirst().orElse(null);
            if (Objects.isNull(pd)
                    || !Objects.equals(getValue(entityMap.get(fields), pd.getPropertyType(), pd.getName(), timeZone), srcSource.getPropertyValue(pd.getName())))
                return false;
        }
        return true;
    }

    private void addEntityToMap(BeanWrapper srcSource, PropertyDescriptor pd, Map<?, ?> entity, String timeZone) throws Exception {
        var actual = srcSource.getPropertyValue(pd.getName());
        if (Objects.isNull(actual))
            actual = pd.getPropertyType().getDeclaredConstructor().newInstance();
        setFieldValues((VdmObject<?>) actual, entity, timeZone);
        srcSource.setPropertyValue(pd.getName(), pd.getPropertyType().cast(actual));
    }

    private Object getValue(Object value, Class<?> type, String fieldNAme, String timeZone) throws Exception {
        try {
            if (Objects.isNull(value)) return null;
//            if (type.equals(BoYesNoEnum.class) && value instanceof Boolean)
//                return ((Boolean) value) ? BoYesNoEnum.TYES : BoYesNoEnum.TNO;
            else if (type.isEnum() && value instanceof String)
                return type.getDeclaredMethod("valueOf", String.class).invoke(null, ((String) value).toUpperCase());
            else if (type.equals(LocalDate.class) && value instanceof String)
                return LocalDate.parse((String) value);
            else if (type.equals(LocalDate.class) && value instanceof LocalDateTime)
                return DateUtil.toZoneTime((LocalDateTime) value, timeZone).toLocalDate();
            else if (type.equals(LocalTime.class) && value instanceof String)
                return LocalTime.parse((String) value);
            else if (type.equals(LocalTime.class) && value instanceof LocalDateTime)
                return DateUtil.toZoneTime((LocalDateTime) value, timeZone).toLocalTime();
            else if (type.equals(Double.class) && value instanceof Integer)
                return ((Integer) value).doubleValue();
            else if (type.equals(Double.class) && value instanceof String)
                return Double.valueOf((String) value);
            else if (type.equals(Integer.class) && value instanceof Double)
                return ((Double) value).intValue();
            else if (type.equals(Integer.class) && value instanceof String)
                return Integer.valueOf((String) value);
            else if (type.equals(String.class) && !(value instanceof String))
                return value.toString();
            return value;
        } catch (Exception e) {
            throw new BadRequestException("Invalid! Field '" + fieldNAme + "', expected type: '" + type.getSimpleName() + "', received value: '" + value + "'");
        }
    }
}
