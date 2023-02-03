package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Table(name="member")
@Getter @Setter
@ToString
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    // 회원은 이메일을 통해 유일하게 구분해야하기 때문에 동일한 값이 데이터베이스에 들어올 수 없도록 unique
    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    // enum타입을 엔티티의 속성으로 지정 가능
    // String으로 저장하기를 권장
    @Enumerated(EnumType.STRING)
    private Role role;

    // Member 엔티티 생성
    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        // 스프링 시큐리티 설정 클래스에 등록한 Encoder를 통해 비밀번호를 암호화합니다.
        String password = passwordEncoder.encode((memberFormDto.getPassword()));
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }

}
