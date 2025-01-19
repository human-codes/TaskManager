package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tmanager.entity.Attachment;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID>, JpaSpecificationExecutor<Attachment> {
}