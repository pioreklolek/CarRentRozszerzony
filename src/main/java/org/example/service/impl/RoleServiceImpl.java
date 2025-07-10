package org.example.service.impl;

import org.example.model.Role;
import org.example.repository.RoleRepository;
import org.example.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repo;

    public RoleServiceImpl(RoleRepository repo) {
        this.repo = repo;
    }

    @Override
    public Role createRole(String rolename) {
        if (repo.exists(rolename)) {
            throw new IllegalArgumentException("Rola o nazwie " + rolename + "juz istnieje!");
        }
        Role role = new Role(rolename);
        return repo.save(role);
    }

    @Override
    public Role findById(Long id) {
        Role role = repo.findById(id);
        if (role == null) {
            throw new IllegalArgumentException("Nie znaleziono roli o ID: " + id);
        }
        return role;
    }

    @Override
    public Role findByName(String rolename) {
        Role role = repo.findByName(rolename);
        if (role == null) {
            throw new IllegalArgumentException("Nie znaleziono roli o nazwie: " + rolename);
        }
        return role;    }

    @Override
    public List<Role> findAll() {
       return repo.findAll();
    }

    @Override
    public Role updateRole(Long id, String newRole) {
        Role role = findById(id);
        if(repo.exists(newRole) && !role.getName().equals(newRole)) {
            throw new IllegalArgumentException("Rola o nazwie " + newRole + "juz istnieje!");
        }
        role.setName(newRole);
        return repo.save(role);
    }

    @Override
    public void deleteById(Long id) {
    Role role = findById(id);
    repo.deleteById(id);
    }

    @Override
    public void delete(Role role) {
        repo.delete(role);
    }

    @Override
    public boolean existByName(String name) {
        return repo.exists(name);
    }

    @Override
    public Role getOrCreateRole(String name) {
        Role existRole = repo.findByName(name);
        if(existRole != null) {
            return existRole;
        }
        return createRole(name);
    }
}
