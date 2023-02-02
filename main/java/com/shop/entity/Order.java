package com.shop.entity;

import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.asm.Advice;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 한 명의 회원은 여러 번 주문을 할 수 있으므로 주문 엔티티 기준에서 다대일 단방향 매핑

    private LocalDateTime orderDate; // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문상태

    // 무조건 양방향으로 연관 관계를 매핑하면 해당 엔티티는 엄청나게 많은 테이블과 연관 관계를 맺게 되고
    // 엔티티 클래스 자체가 복잡해지기 때문에 연관 관계 단방향 매핑으로 설계 후 나중에 필요할 경우 양방향 매핑 권장
    // 다대다 매핑은 일대다, 다대일 관계로 풀어야함 (실무에서 사용 X)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY) // 주문 상품 엔티티와 일대 다 매핑을 합니다.
                                   // 외래키(order_id)가 order_item 테이블에 있으므로 연관 관계의 주인은 OrderItem 엔티티
                                   // Order 엔티티가 주인이 아니므로 mapperBy 속성으로 연관관계의 주인을 설정
                                   // 속성 값으로 order 를 적은 이유는 OrderItem에 있는 Order에 의해 관리된다는 의미로 해석
                                   // 즉, 연관 관계의 주인의 필드인 order를 mapperedBy의 값으로 세팅

    // 부모 엔티티의 영속성 상태 변화를 자식 엔티티에 모두 전이하는 Cascade TypeALL 옵션 설정
    // 부모 엔티티와 연관 관계가 끊어진 자식 엔티티 => 고아 객체
    // 고아 객체 제거 기능은 참조하는 곳이 하나일 때만 사용해야함 => 다른 곳에서도 참조하고 있는 엔티티인데 삭제하면 문제가 생김
    // OneToOne, OneToMany 어노테이션에서 옵션으로 사용하면 된다. => orphanRemoval = true
    private List<OrderItem> orderItems = new ArrayList<>(); // 하나의 주문이 여러 개의 주문 상품을 갖으므로 List 자료형을 사용해 매핑

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem); // 주문 상품 정보들을 담아준다.
        orderItem.setOrder(this); // 양방향 참조 관계이므로 orderItem 객체에도 order 객체를 세팅
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member);
        // 상품 페이지에서 1개의 상품을 주문하였지만,
        // 장바구니 페이지에서는 한 번에 여러 개의 상품을 주문할 수 있다.
        // 따라서, 여러 개의 주문 상품을 담ㅇ르 수 있도록 리스트 형태로 파라미터 값을 받으며 주문 객체에 orderItem 객체를 추가
        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }

        return totalPrice;
    }
}
