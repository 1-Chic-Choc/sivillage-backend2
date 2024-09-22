package com.chicchoc.sivillage.domain.cart.domain;

import com.chicchoc.sivillage.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "cart")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @Comment("사용자 Uuid")
    @Column(name = "user_uuid", nullable = false, length = 21)
    private String userUuid;

    @Comment("장바구니 Uuid")
    @Column(name = "cart_uuid", nullable = false, length = 21)
    private String cartUuid;

    @Comment("장바구니 이름")
    @Column(name = "cart_name", nullable = false, length = 50)
    private String cartName;
}
