package tmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Comment extends BaseEntity{
    private String text;
    private LocalDateTime date;
    @ManyToOne
    private User user;
    @ManyToOne
    private Task task;
}
