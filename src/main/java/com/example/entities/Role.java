package com.example.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "roles")
@NoArgsConstructor 
@AllArgsConstructor
@Data
@Builder 
public class Role {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)    
    private int id;

    @Enumerated (EnumType.STRING)
    @Column (length = 20)
    private ERole name;

}
