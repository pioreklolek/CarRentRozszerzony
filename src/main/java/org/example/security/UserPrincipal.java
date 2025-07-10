package org.example.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private Long id;
    private String login;

    @JsonIgnore
    private String password;

    private boolean deleted;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(long id, String login, String password, boolean deleted,  Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.deleted = deleted;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        // DEBUG: Sprawdź jakie role są pobierane z bazy
        System.out.println("DEBUG: Creating UserPrincipal for user: " + user.getLogin());
        System.out.println("DEBUG: User roles count: " + user.getRoles().size());

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName();
                    System.out.println("DEBUG: Processing role: '" + roleName + "'");
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList());

        System.out.println("DEBUG: Final authorities: " + authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        System.out.println("DEBUG: Roles loaded from DB: " + user.getRoles().stream().map(r -> r.getName()).toList());
        return new UserPrincipal(
                user.getId(), user.getLogin(), user.getPassword(), user.getDeleted(), authorities
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !deleted;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
