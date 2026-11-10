package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MedicalData {

    private List<String> allergies;
    private List<String> medications;
    private List<String> preExistingConditions;
    private List<SymptomDetail> symptoms;
}
