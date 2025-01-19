package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tmanager.entity.Card;
import tmanager.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    @Query("SELECT t FROM Task t WHERE t.card.board.id = :boardId")
    List<Task> findTasksByBoardId(@Param("boardId") UUID boardId);

    @Query("SELECT t FROM Task t WHERE " +
            "(t.card.board.id = :boardId OR :boardId IS NULL) AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR :searchKeyword IS NULL)")
    List<Task> findTasksByBoardAndSearch(UUID boardId, String searchKeyword);
}