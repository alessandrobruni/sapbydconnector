package eu.company.connector.sapbydesignbridge.mapper.sap;

import java.util.UUID;

public interface ICodeNameMapper {
    default String generateSAPCode() {
        return UUID.randomUUID().toString();
    }

    default String generateSAPName() {
        return UUID.randomUUID().toString();
    }
}
