package mohammad.development.praxis.repos;

import mohammad.development.praxis.modules.patient.Submission;
import mohammad.development.praxis.modules.patient.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findAllByStatusOrderByCreatedAtDesc(SubmissionStatus status);

    long countByStatus(SubmissionStatus status);

    Optional<Submission> findFirstByStatusOrderByCreatedAtAsc(SubmissionStatus status);
}
