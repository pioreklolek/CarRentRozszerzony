package org.example.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @ManyToMany(fetch = FetchType.EAGER)    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    public User(String login, String password, Set<Role> roles) {
        this.login = login;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>();
        this.deleted = false;
        }
        public User(String login, String password, Role role) {
        this.login = login;
        this.password = password;
        this.roles = new HashSet<>();
        if (role != null) {
            this.roles.add(role);
            }
        this.deleted = false;
        }
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    public boolean hasRole(String roleName) {
       return  roles != null && roles.stream().anyMatch(role -> roleName.equals(role.getName()));
    }
    public boolean isAdmin() {
        return  hasRole("admin");
    }
    public boolean isModerator() {
        return  hasRole("moderator");
    }

    public boolean isUser() {
        return  hasRole("user");
    }
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }
    public void removeRole(Role role) {
        if (role != null){
            this.roles.remove(role);
        }
    }
}