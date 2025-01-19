package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tmanager.entity.Attachment;
import tmanager.entity.AttachmentContent;

import java.util.Optional;
import java.util.UUID;

public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, UUID>, JpaSpecificationExecutor<AttachmentContent> {
    AttachmentContent findByAttachmentId(UUID attachmentId);
}