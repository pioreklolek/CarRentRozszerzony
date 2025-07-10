package org.example.repository;

import org.example.model.Role;

import java.util.List;

public interface RoleRepository {
    Role save(Role role);
    Role findById(Long id);
    Role findByName(String name);
    List<Role> findAll();
    void delete(Role role);
    void deleteById(Long id);
    boolean exists(String name);

}
