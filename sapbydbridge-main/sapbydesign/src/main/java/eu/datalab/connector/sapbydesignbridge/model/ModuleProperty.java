package eu.company.connector.sapbydesignbridge.model;

import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@With
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProperty {
    private String entityName;

    @Builder.Default
    private Set<String> customFields = new HashSet<>();
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();
}

