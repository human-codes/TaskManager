package tmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmanager.entity.Role;
import tmanager.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository rolesRepository;

    public List<Role> getSimpleRoles(){
        Role byName = rolesRepository.findByName(tmanager.entity.enums.RoleName.USER);
        return new ArrayList<>(List.of(byName));
    }
}
