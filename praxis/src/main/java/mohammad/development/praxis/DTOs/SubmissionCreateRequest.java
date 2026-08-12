package mohammad.development.praxis.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import mohammad.development.praxis.modules.patient.*;

@Data
@NoArgsConstructor
public class SubmissionCreateRequest {
    private String formVersion; // optional, default v1
    private PatientData patientData;
    private MedicalData medical;
    private Consents consents;
    private Signature signature;   // optional
    private SubmissionMeta meta;
}
