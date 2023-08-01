package eu.company.connector.sapbydesignbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import digital.vianello.schnecke.exceptions.BadRequestException;
import digital.vianello.schnecke.exceptions.NotFoundException;
import digital.vianello.schnecke.util.CustomBeanUtils;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.DataToSync;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.repository.TenantRepository;
import eu.company.connector.sapbydesignbridge.service.sap.LanguageService;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class TenantService {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private ObjectMapper objectMapper;

    public Tenant findByName(String name) throws NotFoundException {
        return tenantRepository.findById(name).orElseThrow(() -> new NotFoundException(Tenant.class, name));
    }

    public Set<Tenant> findAll() {
        Set<Tenant> result = new HashSet<>();
        tenantRepository.findAll().forEach(result::add);
        return result;
    }
    public void updateSyncTime(final BridgeConfiguration bridgeConfiguration, final DataLoaderConfiguration dataLoaderConfiguration, final LocalDateTime nowSync) {
        DataToSync dataToSync = bridgeConfiguration.getThingsToSync()
                .stream().filter(e -> e.getDataLoaderConfiguration().equals(dataLoaderConfiguration)).findFirst().orElse(null);
        if (Objects.nonNull(dataToSync)) dataToSync.setLastSyncDate(nowSync);
    }

    public BridgeConfiguration getBridgeConfiguration(Tenant tenant, String bridgeId) throws Exception {
        if (!tenant.getBridgeConfigurations().containsKey(bridgeId)) return null;
        return objectMapper.readValue(tenant.getBridgeConfigurations().get(bridgeId), BridgeConfiguration.class);
    }
    public void saveSyncTime(final String tenantName, final String bridgeId, final BridgeConfiguration bridgeConfigurationSource) throws Exception {
        Tenant target = findByName(tenantName);
        BridgeConfiguration bridgeConfigurationTarget = getBridgeConfiguration(target, bridgeId);
        for (DataToSync dataToSyncTarget : bridgeConfigurationTarget.getThingsToSync()) {
            DataToSync dataToSyncSource = bridgeConfigurationSource.getThingsToSync()
                    .stream().filter(e -> e.getDataLoaderConfiguration().equals(dataToSyncTarget.getDataLoaderConfiguration())).findFirst().orElse(null);
            if (Objects.nonNull(dataToSyncSource)) dataToSyncTarget.setLastSyncDate(dataToSyncSource.getLastSyncDate());
        }
        updateTenant(target, bridgeId, bridgeConfigurationTarget);
    }

    public Tenant updateTenant(Tenant tenant, String bridgeId, BridgeConfiguration bridgeConfiguration) throws Exception {
        bridgeConfiguration.getThingsToSync()
                .forEach(tts -> {
                    if (Objects.isNull(tts.getDataLoaderConfiguration().getReference()))
                        tts.getDataLoaderConfiguration().setReference("");
                });
        tenant.getBridgeConfigurations().put(bridgeId, objectMapper.writeValueAsString(bridgeConfiguration));
        return tenantRepository.save(tenant);
    }

    public Tenant createOrUpdate(Tenant source, String bridgeId, BridgeConfiguration bridgeConfigurationSource) throws Exception {
        Tenant target;
        BridgeConfiguration bridgeConfigurationTarget;
        try {
            target = findByName(source.getName());
            target.setLanguage(source.getLanguage());
            target.setTimeZone(source.getTimeZone());
            target.getModels().addAll(source.getModels());
            bridgeConfigurationTarget = getBridgeConfiguration(target, bridgeId);
            if (Objects.nonNull(bridgeConfigurationTarget)) {
                CustomBeanUtils.copyPropertiesIgnoringNull(bridgeConfigurationSource, bridgeConfigurationTarget);
                bridgeConfigurationTarget.getProperties().putAll(bridgeConfigurationSource.getProperties());
                addOrUpdateThingsToSyncTarget(bridgeConfigurationTarget.getThingsToSync(), bridgeConfigurationSource.getThingsToSync());
                if (!bridgeConfigurationSource.getTargetMessageUsers().isEmpty())
                    bridgeConfigurationTarget.setTargetMessageUsers(bridgeConfigurationSource.getTargetMessageUsers());
                if (!bridgeConfigurationSource.getTargetMessageGroups().isEmpty())
                    bridgeConfigurationTarget.setTargetMessageGroups(bridgeConfigurationSource.getTargetMessageGroups());
            } else bridgeConfigurationTarget = bridgeConfigurationSource;
        } catch (NotFoundException ignored) {
            target = source;
            bridgeConfigurationTarget = bridgeConfigurationSource;
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        bridgeConfigurationTarget.setLanguage(LanguageService.defaultLanguageCodeSBO.toString());
        languageService.setLanguageCode(target, bridgeConfigurationTarget);
        return updateTenant(target, bridgeId, bridgeConfigurationTarget);
    }

    private void addOrUpdateThingsToSyncTarget(Set<DataToSync> thingsToSyncTarget, Set<DataToSync> thingsToSyncSource) {
        if (thingsToSyncSource.isEmpty()) return;
        for (DataToSync dtsSource : thingsToSyncSource) {
            DataToSync dtsTarget = thingsToSyncTarget.stream()
                    .filter(c -> c.getDataLoaderConfiguration().equals(dtsSource.getDataLoaderConfiguration())).findFirst().orElse(null);
            if (Objects.isNull(dtsTarget)) thingsToSyncTarget.add(dtsSource);
            else {
                updateDataLoaderConfiguration(dtsTarget.getDataLoaderConfiguration(), dtsSource.getDataLoaderConfiguration());
                if (dtsTarget.getMinutesSyncInterval() > dtsSource.getMinutesSyncInterval())
                    dtsTarget.setMinutesSyncInterval(dtsSource.getMinutesSyncInterval());
            }
        }
    }

    private void updateDataLoaderConfiguration(DataLoaderConfiguration target, DataLoaderConfiguration source) {
        if (Objects.isNull(source)) return;
        target.getProperties().addAll(source.getProperties());
        target.getOptions().putAll(source.getOptions());
        for (DataLoaderConfiguration dlcSource : source.getElements()) {
            DataLoaderConfiguration dlcTarget = target.getElements().stream().filter(e -> e.equals(dlcSource)).findFirst().orElse(null);
            if (Objects.isNull(dlcTarget)) target.getElements().add(dlcSource);
            else updateDataLoaderConfiguration(dlcTarget, dlcSource);
        }
    }



}
