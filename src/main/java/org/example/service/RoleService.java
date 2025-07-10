package org.example.service;

import org.example.model.Role;

import java.util.List;

public interface RoleService {
    Role createRole(String rolename);
    Role findById(Long id);
    Role findByName(String rolename);
    List<Role> findAll();
    Role updateRole(Long id, String newRole);
    void deleteById(Long id);
    void delete(Role role);
    boolean existByName(String name);
    Role getOrCreateRole(String name);

}
