package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@Document(collection = "submissions")
public class Submission {

    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private SubmissionStatus status = SubmissionStatus.NEW;

    private String formVersion = "v1";

    private PatientData patientData;
    private MedicalData medical;
    private Consents consents;

    /** optional */
    private Signature signature;

    /** sparsam, möglichst wenig speichern */
    private SubmissionMeta meta;

    private List<SubmissionAttachment> attachments = new ArrayList<>();

}
