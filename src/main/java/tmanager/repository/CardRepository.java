package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tmanager.entity.Board;
import tmanager.entity.Card;
import tmanager.entity.Task;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    @Query("SELECT t FROM Task t WHERE t.card IN :cards")
    List<Task> findTasksByCards(@Param("cards") List<Card> cards);

    List<Card> findByBoard_Id(UUID boardId);


    @Query("SELECT MAX(c.orders) FROM Card c WHERE c.board.id = :boardId")
    Integer findMaxOrderByBoardId(@Param("boardId") UUID boardId);

    List<Card> findByBoardId(UUID boardId);
}