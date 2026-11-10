package mohammad.development.praxis.modules.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "form_definitions")
public class FormDefinition {

    @Id
    private String id;

    @Indexed(unique = true)
    private String version; // z.B. "v1"

    /** JSON Schema als Map (flexibel) */
    private Map<String, Object> schema;

    /** UI-Konfiguration fürs Frontend (z.B. Stepper, Labels, Reihenfolge, etc.) */
    private Map<String, Object> uiConfig;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
