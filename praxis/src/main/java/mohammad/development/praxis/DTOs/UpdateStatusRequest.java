package mohammad.development.praxis.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import mohammad.development.praxis.modules.patient.SubmissionStatus;

@Data
@NoArgsConstructor
public class UpdateStatusRequest {
    private SubmissionStatus status;
}
