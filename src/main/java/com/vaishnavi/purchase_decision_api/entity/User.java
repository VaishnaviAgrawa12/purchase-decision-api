package com.vaishnavi.purchase_decision_api.entity;

import jakarta.persistence.*;
import lombok.Value;

import java.util.UUID;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID UserId;

    @Column(nullable = false, unique = true)

}
