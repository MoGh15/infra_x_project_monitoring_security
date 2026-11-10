package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Consents {
    private boolean gdprAccepted;            // DSGVO
    private boolean dataSharingAccepted;     // Datenweitergabe (optional)
    private Instant acceptedAt;              // Zeitpunkt der Zustimmung
}
