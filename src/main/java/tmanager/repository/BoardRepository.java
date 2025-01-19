package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tmanager.entity.Board;

import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID>, JpaSpecificationExecutor<Board> {

}