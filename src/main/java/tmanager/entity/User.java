package tmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @NotBlank(message = "Full name cannot be empty or blank")
    private String fullName;
    @NotBlank(message = "Username cannot be empty or blank")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Email must be a valid Gmail address")
    private String email;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+998[0-9]{9}$", message = "Phone number must be a valid Uzbek phone number starting with +998")
    private String phone;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    @ManyToOne(fetch = FetchType.EAGER)
    private Attachment attachment;
}
