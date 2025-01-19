package tmanager.entity;

import jakarta.persistence.Entity;
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
public class AttachmentContent extends BaseEntity{

    @ManyToOne
    private Attachment attachment;
    private byte[] fileData;
}
