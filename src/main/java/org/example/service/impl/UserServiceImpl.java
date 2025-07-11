package org.example.service.impl;

import org.example.model.Role;
import org.example.model.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.service.RoleService;
import org.example.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;


    public UserServiceImpl(UserRepository repo,PasswordEncoder passwordEncoder,RoleService roleService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }
    @Override
    public User findByLogin(String login) {
        User user = repo.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + login);
        }
        return user;
    }
    @Override
    public User findById(Long id) {
        User user = repo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Nie znaleziono użytkownika id: " + id);
        }
        return user;
    }
    @Override
    public List<User> findAll(){
        return repo.findAll();
    }
    @Override
    public void save(User user){
        repo.save(user);
    }
    @Override
    public User createUser(String login, String password, String roleName, String address, String postalCode, String country) {
        if (repo.findByLogin(login) != null) {
            throw new IllegalArgumentException("Użytkownik o podanym loginie już istnieje!");
        }
        String hashedPassword =  passwordEncoder.encode(password);
        Role role = roleService.getOrCreateRole(roleName.toLowerCase());
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User nUser = new User(login, hashedPassword, roles);
        nUser.setAddress(address);
        nUser.setPostalCode(postalCode);
        nUser.setCountry(country);
        return repo.save(nUser);
    }
    @Override
    public void deleteById(Long id){
        if (repo.findById(id) == null) {
            throw  new IllegalArgumentException("Użykownik o podanym ID:" + id + " nie istnieje!");
        }
        repo.deleteById(id);
    }
    @Override
    public void deleteUserByLogin(String login) {
        User user = repo.findByLogin(login);
        if (user == null) {
            throw  new IllegalArgumentException("Użykownik o podanym loginie:" + login + " nie istnieje!");
        }
        repo.delete(user);
    }
    @Override
    public User updateUser(Long id, User userDetails) {
     Optional<User> optionalUser = Optional.ofNullable(repo.findById(id));
     if (optionalUser.isPresent()) {
         User user = optionalUser.get();
         user.setLogin(userDetails.getLogin());
         if( userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()){
             user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
         }

         if (userDetails.getRoles() != null) {
             user.setRoles(new HashSet<>(userDetails.getRoles()));
         }
         return repo.save(user);
     }
     throw  new IllegalArgumentException("Użytkownik nie znaleziony!");
    }
    @Override
    public List<User> findAllDeletedUsers() {
        return repo.findDeletedUsers();
    }
    @Override
    public List<User> findAllActiveUsers() {
       return repo.findAllActiveUsers();
    }
    @Override
    public String getPrimaryRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "user";
        }
        if(user.hasRole("admin")) return "admin";
        if(user.hasRole("moderator")) return "moderator";
        if(user.hasRole("user")) return "user";

        return user.getRoles().iterator().next().getName();
    }
    @Override
    public String getAllRoles(User user) {
        if(user.getRoles() == null || user.getRoles().isEmpty()) {
            return "user";
        }
        return user.getRoles().stream().map(Role::getName).reduce((a,b) -> a + "," + b).orElse("user");
    }

    @Override
    public User addRoleToUser(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleService.getOrCreateRole(roleName);
        user.addRole(role);
        return repo.save(user);
    }

    @Override
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleService.findByName(roleName);
        if (role != null) {
            user.removeRole(role);
            return repo.save(user);
        }
        return user;
    }

    @Override
    public User updateUserAddress(Long userId, String address, String postalCode, String country) {
        User user = repo.findById(userId);
        if(user == null) {
            throw  new IllegalArgumentException("Uzytkownik nie zostal znaleziony!");
        }
        user.setAddress(address);
        user.setPostalCode(postalCode);
        user.setCountry(country);

        return repo.save(user);
    }
}
