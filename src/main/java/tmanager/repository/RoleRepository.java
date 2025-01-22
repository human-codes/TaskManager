package tmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmanager.entity.Role;
import tmanager.entity.enums.RoleName;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(RoleName name);
}