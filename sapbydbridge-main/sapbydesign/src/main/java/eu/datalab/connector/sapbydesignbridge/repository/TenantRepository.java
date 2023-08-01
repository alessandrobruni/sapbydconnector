package eu.company.connector.sapbydesignbridge.repository;

import eu.company.connector.sapbydesignbridge.model.Tenant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {

}
