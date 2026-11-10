package mohammad.development.praxis.modules.patient;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PatientData {
    private String firstName;
    private String lastName;
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "birthDate must be in format yyyy-MM-dd"
    )
    private String birthDate;

    private String phone;
    private String email;

    private Address address;
}
