package org.example.service;

import org.example.model.User;
import java.util.List;


public interface UserService {


     User findByLogin(String login);
     User findById(Long id);

     List<User> findAll();

    void save(User user);
    User createUser(String login, String password, String roleName, String address, String postalCode, String country);

     void deleteById(Long id);
     void deleteUserByLogin(String login);
     User updateUser(Long id, User userDetails);
     List<User> findAllDeletedUsers();
     List<User> findAllActiveUsers();

    String getAllRoles(User user);
    String getPrimaryRole(User user);
    User addRoleToUser(Long userId, String roleName);
    User removeRoleFromUser(Long userId, String roleName);

    User updateUserAddress(Long userId, String address, String postalCode, String country);

}