package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SignatureStroke {
    private List<SignaturePoint> points;
    private Double width; // optional
}
