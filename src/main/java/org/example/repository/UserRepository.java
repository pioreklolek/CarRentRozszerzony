package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface UserRepository {
    User save(User user);

    @EntityGraph(attributePaths = "roles")
    User findByLogin(String login); // wszyscsy usnieci tez

    @EntityGraph(attributePaths = "roles")

    List<User> findAll(); // wszyscy usunieci tez

    @EntityGraph(attributePaths = "roles")

    User findById(Long id); // wszysscy usunieci tez

    void delete(User user); //soft

    void deleteById(Long id); // soft

    List<User> findAllActiveUsers();

    List<User> findAllActiveUsersByRolename(String rolename);

    List<User> findDeletedUsers();

}