package eu.company.connector.sapbydesignbridge.model;
import lombok.*;
@With
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderConfig {

    public static final String KEY = "HeaderConfig";

    private String token;

    private Integer rowsPerPage;
}
