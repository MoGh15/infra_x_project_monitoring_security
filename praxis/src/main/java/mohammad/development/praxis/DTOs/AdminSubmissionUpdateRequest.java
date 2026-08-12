package mohammad.development.praxis.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import mohammad.development.praxis.modules.patient.Consents;
import mohammad.development.praxis.modules.patient.MedicalData;
import mohammad.development.praxis.modules.patient.PatientData;

@Data
@NoArgsConstructor
public class AdminSubmissionUpdateRequest {
    private PatientData patientData;
    private MedicalData medical;
    private Consents consents;
}
