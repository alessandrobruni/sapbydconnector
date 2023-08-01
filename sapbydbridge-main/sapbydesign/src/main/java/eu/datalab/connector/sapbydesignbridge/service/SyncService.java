package eu.company.connector.sapbydesignbridge.service;

import digital.vianello.schnecke.exceptions.NotSupportedException;
import digital.vianello.schnecke.util.DateUtil;
import eu.company.connector.sapbydesignbridge.model.BridgeConfiguration;
import eu.company.connector.sapbydesignbridge.model.DataToSync;
import eu.company.connector.sapbydesignbridge.model.Tenant;
import eu.company.connector.sapbydesignbridge.service.sap.ISyncData;
import eu.company.connector.sapbydesignbridge.service.sap.entity.MaterialServiceEntity;
import eu.companys.commons.mqtt.dto.message.PositiveMessageDTO;
import eu.companys.commons.mqtt.model.DataLoaderConfiguration;
import eu.companys.commons.mqtt.model.ElementLoader;
import eu.companys.commons.mqtt.model.OperationReport;
import eu.companys.commons.mqtt.model.OperationType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2
@Service
public class SyncService {

    @Autowired
    private MaterialServiceEntity materialService;
    @Autowired
    private TenantService tenantService;
    @Value("${service.users[0].bridgeId}")
    private String bridgeId;
    private final Map<String, Set<DataLoaderConfiguration>> syncQueues = new ConcurrentHashMap<>();


    public void doSync(Tenant tenant, LocalDateTime nowSync, Collection<DataLoaderConfiguration> dataLoaderConfigurations) {
        try {
            if (Objects.isNull(tenant) || !tenant.getBridgeConfigurations().containsKey(bridgeId))
                return;
            setReferenceEmpty(dataLoaderConfigurations);
            BridgeConfiguration bridgeConfiguration = tenantService.getBridgeConfiguration(tenant, bridgeId);
            Set<DataLoaderConfiguration> toRemove = new HashSet<>();
            for (DataToSync dataToSync : bridgeConfiguration.getThingsToSync()
                    .stream()
                    .sorted(Comparator.comparing(dts -> ElementLoader.valueOf(dts.getDataLoaderConfiguration().getTarget())))
                    .collect(Collectors.toCollection(LinkedHashSet::new))) {
                if (doIt(tenant.getName(), dataToSync, nowSync, dataLoaderConfigurations)) {
                    if (requestData(tenant, bridgeConfiguration, dataToSync, nowSync))
                        tenantService.updateSyncTime(bridgeConfiguration, dataToSync.getDataLoaderConfiguration(), nowSync);
                    toRemove.add(dataToSync.getDataLoaderConfiguration());
                }
            }
            tenantService.saveSyncTime(tenant.getName(), bridgeId, bridgeConfiguration);
            deleteSyncQueues(tenant.getName(), toRemove);
        } catch (Exception e) {
            deleteAllSyncQueues();
            log.error("Fail to sync '{}', error is '{}'", tenant.getName(), e.getMessage());
            e.printStackTrace();
        }
    }

    private void setReferenceEmpty(Collection<DataLoaderConfiguration> dataLoaderConfigurations) {
        dataLoaderConfigurations.forEach(dlc -> {
            if (Objects.isNull(dlc.getReference())) dlc.setReference("");
        });
    }

    private boolean doIt(String tenantName, DataToSync dataToSync, LocalDateTime nowSync, Collection<DataLoaderConfiguration> dataLoaderConfigurations) {
        if (!checkSyncQueues(tenantName, dataToSync.getDataLoaderConfiguration())
                && (dataToSync.getLastSyncDate().isBefore(nowSync.minus(dataToSync.getMinutesSyncInterval(), ChronoUnit.MINUTES))
                || dataLoaderConfigurations.contains(dataToSync.getDataLoaderConfiguration()))) {
            addSyncQueues(tenantName, dataToSync.getDataLoaderConfiguration());
            return true;
        }
        return false;
    }

    private boolean requestData(final Tenant tenant, final BridgeConfiguration bridgeConfiguration, DataToSync dataToSync, final LocalDateTime nowSync) {
        OperationReport operationReport = OperationReport.builder()
                .reference(dataToSync.getDataLoaderConfiguration().getReference())
                .elementLoader(ElementLoader.valueOf(dataToSync.getDataLoaderConfiguration().getTarget()))
                .type(OperationType.SYNC)
                .build();
        try {
            Collection<?> elements = null;
            for (int i = 0; Objects.isNull(elements) || elements.size() == bridgeConfiguration.getRowsPerPage(); i++) {
                elements = getService(ElementLoader.valueOf(dataToSync.getDataLoaderConfiguration().getTarget()))
                        .getData(tenant, bridgeConfiguration, i, parseDate(dataToSync.getLastSyncDate(), tenant.getTimeZone()), parseDate(nowSync, tenant.getTimeZone()), dataToSync.getDataLoaderConfiguration());
                assert elements != null;
                log.debug("Got {} elements from '{}', cycle #{}, {} elements per cycle", elements.size(), dataToSync.getDataLoaderConfiguration().getTarget(), (i + 1), bridgeConfiguration.getRowsPerPage());
//                if (!elements.isEmpty() || i > 0)
//                    logService.info(tenant, bridgeConfiguration, operationReport,
//                            PositiveMessageDTO.builder()
//                                    .date(DateUtil.now())
//                                    .data(new HashSet<>(elements))
//                                    .message("Syncing '" + dataToSync.getDataLoaderConfiguration().getTarget() + "' for tenant '" + tenant.getName() + "'(" + elements.size() + " elements)")
//                                    .build());
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to sync '{}', error is '{}'", dataToSync.getDataLoaderConfiguration().getTarget(), e.getMessage());
//            logService.error(tenant, bridgeConfiguration, operationReport, e);
            return false;
        }
    }

    private synchronized void deleteSyncQueues(String tenantName, Set<DataLoaderConfiguration> dataLoaderConfigurations) {
        if (syncQueues.containsKey(tenantName)) {
            syncQueues.get(tenantName).removeAll(dataLoaderConfigurations);
            if (syncQueues.get(tenantName).isEmpty())
                syncQueues.remove(tenantName);
        }
    }
    private synchronized boolean checkSyncQueues(String tenantName, DataLoaderConfiguration dataLoaderConfiguration) {
        if (syncQueues.containsKey(tenantName))
            return syncQueues.get(tenantName).contains(dataLoaderConfiguration);
        return false;
    }
    private synchronized void deleteAllSyncQueues() {
        syncQueues.clear();
    }

    private synchronized void addSyncQueues(String tenantName, DataLoaderConfiguration dataLoaderConfiguration) {
        if (syncQueues.containsKey(tenantName))
            syncQueues.get(tenantName).add(dataLoaderConfiguration);
        else {
            Set<DataLoaderConfiguration> dlc = ConcurrentHashMap.newKeySet();
            dlc.add(dataLoaderConfiguration);
            syncQueues.put(tenantName, dlc);
        }
    }

    private ISyncData getService(ElementLoader elementLoader) throws NotSupportedException {
        switch (elementLoader) {
//            case PAYMENT_TERM:
//                return paymentTermService;
//            case VAT_GROUP:
//                return vatGroupService;
//            case BUSINESS_PARTNER_GROUP:
//                return businessPartnerGroupService;
//            case BUSINESS_PARTNER:
//                return businessPartnerService;
//            case WAREHOUSE:
//                return warehouseService;
//            case ITEM_GROUP:
//                return itemGroupService;
            case ITEM:
                return materialService;
//            case PRICE_LIST:
//                return priceListService;
//            case QUERY:
//            case UDT:
//            case GENERIC:
//                return customSyncService;
            default:
                throw new NotSupportedException(elementLoader.name());
        }
    }

    private LocalDateTime parseDate(LocalDateTime date, String timeZone) {
        return DateUtil.toZoneTime(date, timeZone).truncatedTo(ChronoUnit.SECONDS);
    }
}
