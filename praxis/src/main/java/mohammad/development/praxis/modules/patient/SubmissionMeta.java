package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionMeta {
    private String tabletId;
    private String language;

    /** optional */
    private String userAgent;

    /**
     * IP, nur wenn wirklich nötig – und wenn, dann am besten gehasht/gekürzt.
     * (Oder komplett weglassen)
     */
    private String ip;
}
