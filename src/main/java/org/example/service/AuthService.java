package org.example.service;

import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Set;

public interface AuthService {



     User login(String login, String password);

     User register(String login, String password, Set<Role> roles);
}