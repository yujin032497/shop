package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.shop.repository.OrderRepository;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    public Item createItem() {
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() { // 부모 엔티티가 삭제되면 연관된 자식 엔티티도 함께 삭제 (order 삭제하면 orderItem도 삭제)

        Order order = new Order();

        for(int i=0; i<3; i++) {
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem); // 아직 영속성 컨텍스트에 저장되지 않은 orderItem 엔티티를 Order에 담아줌
        }

        orderRepository.saveAndFlush(order); // order 엔티티를 저장하면서 강제로 flush를 호출하여
                                             // 영속성 컨텍스트에 있는 객체들을 데이터베이스에 반영
        em.clear(); // 영속성 컨텍스트의 상태를 초기화

        Order savedOrder = orderRepository.findById(order.getId()) // 초기화 후 주문 엔티티를 조회
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size()); // itemOrder 엔티티 3개가 실제로 데이터베이스에 저장되는지 확인
                                                                    // 위 for문이 3번 돌아감
    }

    public Order createOrder() { // 주문 데이터를 생성해서 저장하는 메소드
        Order order = new Order();

        for(int i=0; i<3; i++) {
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);

        order.setMember(member);
        orderRepository.save(order);
        return order;
    }

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest() {
        Order order = this.createOrder();
        order.getOrderItems().remove(0); // order 엔티티에서 관리하고 있는 orderItem 리스트의 0번째 인덱스의 요소를 제거
        em.flush(); // orderItem을 삭제하는 쿼리문 출력
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    // 즉시 로딩은 한꺼번에 조회하므로 실무에서 사용하기 힘들다 => 연관된 매핑에 관련된 모든 걸 실행하므로 파악 어려움
    public void lazyLoadingTest() {
        Order order = this.createOrder(); // 기존에 만들었던 주문 생성 메소드를 이용하여 주문 데이터를 저장
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItemId) // 영속성 컨텍스트 상태 초기화 후
                // order 엔티티에 저장했던 주문 상품 아이디를 이용하여 orderItem을 데이터베이스에서 다시 조회
                .orElseThrow(EntityNotFoundException::new);

        // orderItem 엔티티에 있는 order 객체의 클래스를 출력
        System.out.println("Order class : " +
                orderItem.getOrder().getClass());
        System.out.println("==============================================");
        orderItem.getOrder().getOrderDate();
        System.out.println("==============================================");
    }
}
