package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "cart")
@Getter @Setter
@ToString
public class Cart extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Ctrl + 어노테이션 클릭 시 어노테이션에 따른 즉시로딩, 지연로딩 확인 가능
    @OneToOne(fetch = FetchType.LAZY) // 회원 엔티티와 일대일 매핑
    @JoinColumn(name="member_id") // 매핑할 외래키 지정
    private Member member;
}
