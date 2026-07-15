package com.project.e_commerce.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "Users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private int userId;
    @Column(name="user_name")
    private String userName;
    @Column(name="user_password")
    private String userPassword;
    @Column(name="user_email")
    private String userEmail;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
