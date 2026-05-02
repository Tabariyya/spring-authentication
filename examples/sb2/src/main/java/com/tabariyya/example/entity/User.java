package com.tabariyya.example.entity;

import com.tabariyya.authentication.models.BaseUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDateTime createdAt;

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    @Override public String getUserName() { return userName; }
    @Override public void setUserName(String userName) { this.userName = userName; }
    @Override public String getPassword() { return password; }
    @Override public void setPassword(String password) { this.password = password; }
    @Override public String getEmail() { return email; }
    @Override public void setEmail(String email) { this.email = email; }
    @Override public LocalDateTime getCreatedAt() { return createdAt; }
    @Override public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
