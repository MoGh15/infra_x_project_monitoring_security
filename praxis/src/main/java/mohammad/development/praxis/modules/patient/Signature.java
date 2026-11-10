package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Signature {
    /** z.B. "image/png" */
    private String contentType;

    /** Base64 ohne Data-URL Prefix (empfohlen) */
    private String base64;

    /** optional: Vector strokes, falls du mit Canvas-Strokes arbeitest */
    private List<SignatureStroke> strokes;
}
