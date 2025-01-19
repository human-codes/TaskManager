package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tmanager.entity.Comment;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {
}