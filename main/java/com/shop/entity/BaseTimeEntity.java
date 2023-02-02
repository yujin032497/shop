package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

// 어떤 테이블은 등록자와 수정자를 넣지 않는 테이블이 있을 수 있으므로 BaseTimeEntity만 상속받을 수 있도록 만드는 클래스
@EntityListeners(value = {AuditingEntityListener.class}) // Auditing을 적용하기위 위해서 @EntityListeners 어노테이션을 추가
@MappedSuperclass // 공통 매핑 정보가 필요할 때 사용하는 어노테이션
@Getter @Setter
public class BaseTimeEntity {

    @CreatedDate // 엔티티가 생성되어 저장할 때 시간을 자동으로 저장
    @Column(updatable = false)
    private LocalDateTime regTime;

    @LastModifiedDate // 엔티티의 값을 변경할 때 시간을 자동으로 저장
    private LocalDateTime updateTime;
}
