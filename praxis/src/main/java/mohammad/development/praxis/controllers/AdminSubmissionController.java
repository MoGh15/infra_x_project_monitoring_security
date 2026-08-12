package mohammad.development.praxis.controllers;

import mohammad.development.praxis.DTOs.AdminSubmissionUpdateRequest;
import mohammad.development.praxis.DTOs.UpdateStatusRequest;
import mohammad.development.praxis.modules.patient.Submission;
import mohammad.development.praxis.modules.patient.SubmissionStatus;
import mohammad.development.praxis.modules.patient.SubmissionAttachment;
import mohammad.development.praxis.repos.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.bson.types.ObjectId;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.util.List;

@RestController
@RequestMapping("/api/admin/submissions")
public class AdminSubmissionController {

    private final SubmissionRepository submissionRepository;
    private final GridFsTemplate gridFsTemplate;

    public AdminSubmissionController(SubmissionRepository submissionRepository, GridFsTemplate gridFsTemplate) {
        this.submissionRepository = submissionRepository;
        this.gridFsTemplate = gridFsTemplate;
    }

    // GET /api/admin/submissions?status=NEW
    @GetMapping
    public List<Submission> list(@RequestParam(name = "status", required = false) SubmissionStatus status) {
        if (status == null) {
            // default: neueste zuerst
            return submissionRepository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        }
        return submissionRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    // GET /api/admin/submissions/{id}
    @GetMapping("/{id}")
    public Submission get(@PathVariable String id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }

    @PatchMapping("/{id}")
    public Submission update(@PathVariable String id,
                                    @RequestBody AdminSubmissionUpdateRequest req) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        s.setPatientData(req.getPatientData());
        s.setConsents(req.getConsents());
        s.setMedical(req.getMedical());
        return submissionRepository.save(s);
    }

    // PATCH /api/admin/submissions/{id}/status
    @PatchMapping("/{id}/status")
    public Submission updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest req) {
        if (req.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        s.setStatus(req.getStatus());
        return submissionRepository.save(s);
    }

    // GET /api/admin/submissions/{id}/attachments/{attachmentId}
    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String id, @PathVariable String attachmentId) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        SubmissionAttachment attachment = (s.getAttachments() == null ? List.<SubmissionAttachment>of() : s.getAttachments())
                .stream()
                .filter(a -> attachmentId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(attachmentId))));
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found");
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        String contentType = attachment.getContentType() != null
                ? attachment.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + safeFileName(attachment.getFileName()) + "\"")
                .contentLength(attachment.getSize())
                .body(resource);
    }

    private String safeFileName(String name) {
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[\\r\\n\\\"]", "_");
    }
}
