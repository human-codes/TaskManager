package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tmanager.entity.Task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t WHERE t.card.board.id = :boardId")
    List<Task> findTasksByBoardId(@Param("boardId") UUID boardId);

    @Query("SELECT t FROM Task t WHERE " +
            "(t.card.board.id = :boardId OR :boardId IS NULL) AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR :searchKeyword IS NULL)")
    List<Task> findTasksByBoardAndSearch(@Param("boardId") UUID boardId, @Param("searchKeyword") String searchKeyword);

    @Query(value = """
        SELECT 
            u.attachment_id AS ATTACHMENT_ID, 
            u.username AS userName, 
            COUNT(t.id) AS expiredTaskCount
        FROM 
            users u
        JOIN 
            task_users tu ON u.id = tu.users_id
        JOIN 
            task t ON t.id = tu.task_id
        WHERE 
            t.status = 'EXPIRED'
        GROUP BY 
            u.id, u.username
        ORDER BY 
            expiredTaskCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> findExpiredTaskCountByUser();

    @Query(value = """
        SELECT 
            u.attachment_id AS ATTACHMENT_ID,
            u.username AS userName,
            COUNT(t.id) AS totalTaskCount,
            SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS inProgressTaskCount,
            SUM(CASE WHEN t.status = 'NOT_STARTED' THEN 1 ELSE 0 END) AS notStartedTaskCount,
            SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedTaskCount,
            SUM(CASE WHEN t.status = 'EXPIRED' THEN 1 ELSE 0 END) AS expiredTaskCount,
            SUM(CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END) AS noDeadlineTaskCount
        FROM 
            users u
        LEFT JOIN 
            task_users tu ON u.id = tu.users_id
        LEFT JOIN 
            task t ON t.id = tu.task_id
        GROUP BY 
            u.id, u.username
        ORDER BY 
            totalTaskCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> findTaskStatisticsByUser();

    List<Task> findByCardId(UUID cardId);
}
