package mohammad.development.praxis.repos;

import mohammad.development.praxis.modules.patient.Submission;
import mohammad.development.praxis.modules.patient.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findAllByStatusOrderByCreatedAtDesc(SubmissionStatus status);
}
