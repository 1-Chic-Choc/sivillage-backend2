package com.chicchoc.sivillage.global.auth.domain;

import com.chicchoc.sivillage.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "email_verification")
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Column(name = "is_used", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isUsed;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public EmailVerification(Long id, String email, String verificationCode, Boolean isUsed, LocalDateTime expiresAt) {
        this.id = id;
        this.email = email;
        this.verificationCode = verificationCode;
        this.isUsed = isUsed != null ? isUsed : false;
        this.expiresAt = expiresAt;
    }
}
