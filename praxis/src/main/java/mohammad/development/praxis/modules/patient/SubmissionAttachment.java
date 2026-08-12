package mohammad.development.praxis.modules.patient;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class SubmissionAttachment {
	private String id;
	private String fileName;
	private String contentType;
	private long size;
	private Instant uploadedAt;
}
