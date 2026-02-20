package com.company.mcp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Channel")
public class Channel{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

    @Column(name ="name",nullable = false)
    @NotBlank(message = "Name can not be blank")
   private String name;

    @NotBlank(message = "Description can not be empty")
    @Column(name ="description",nullable = false)
   private  String description;

    // Add this field to Channel.java
    @ManyToMany(mappedBy = "channels")   // "channels" matches the field name in User.java
    private Set<User> users = new HashSet<>();
    public Channel() {
    }

    public Channel(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return users;
    }
}
