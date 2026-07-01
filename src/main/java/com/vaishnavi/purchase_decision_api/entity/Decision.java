package com.vaishnavi.purchase_decision_api.entity;

import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "decisions")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID decisionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false )
    private User user;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @Enumerated(EnumType.STRING)
    private PurchaseType purchaseType;

    private String reason;

    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    @Column(nullable = false)
    private int affordScore;

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;




}
