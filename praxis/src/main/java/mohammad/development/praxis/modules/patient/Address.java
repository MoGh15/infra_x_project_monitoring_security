package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Address {
    private String street;
    private String houseNumber;
    private String zip;
    private String city;
}
