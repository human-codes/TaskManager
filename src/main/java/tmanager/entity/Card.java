package tmanager.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Card extends BaseEntity{
    private String name;
    @ManyToOne
    private Board board;
    private boolean isFinisher=false;
    private boolean isStarter=false;
    private boolean hasTask=false;
}
