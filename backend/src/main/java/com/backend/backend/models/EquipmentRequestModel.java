package com.backend.backend.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.backend.backend.enums.ERequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="equipment_requests")
public class EquipmentRequestModel {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="requester_id", nullable=false)
    private UserModel requester;

    @Column(nullable=false, length=500)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ERequestStatus status = ERequestStatus.Pending;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="reviewed_by")
    private UserModel reviewedBy;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="assigned_asset_id")
    private AssetModel assignedAsset;

    @CreationTimestamp
    @Column(updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}