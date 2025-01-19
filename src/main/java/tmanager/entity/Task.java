package tmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tmanager.entity.enums.TaskStatus;
import tmanager.entity.enums.UserTaskRole;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Task extends BaseEntity {
    private String title;
    private String description;

    @ManyToOne
    private Card card;

    @ManyToOne
    private Attachment attachment;

    @ManyToMany
    private List<User> users;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private TaskStatus status=TaskStatus.NOT_STARTED;

    private LocalDateTime completedAt; // User-specific completion timestamp

    private Boolean started = false; // Whether the user has started the task



}
