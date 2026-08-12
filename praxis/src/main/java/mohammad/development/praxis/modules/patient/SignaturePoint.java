package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignaturePoint {
    private double x;
    private double y;
    private long t; // timestamp/relative time optional
}
