package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter  {

    @Autowired
    MemberService memberService;

    // http 요청에 대한 보안을 설정
    // 페이지 권한 설정, 로그인 페이지 설정, 로그아웃 메소드 등에 대한 설정을 작성
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/members/login") // 로그인 페이지 URL 설정
                .defaultSuccessUrl("/") // 로그인 성공 시 URL 설정
                .usernameParameter("email") // 로그인 시 사용할 파라미터
                .failureUrl("/members/login/error") // 로그인 실패 시 URL
                .and()
                .logout() // 로그아웃 URL
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/"); // 로그아웃 성공 시 이동할 URL

        http.authorizeRequests() // 시큐리티 처리에 HttpServletRequest를 이용한다는 것을 의미
                .mvcMatchers("/", "/members/**",
                        "/item/**", "/images/**").permitAll() // 모든 사용자가 인증 없이 해당 경로에 접근할 수 있도록 설정
                        //메인페이지, 회원관련 URL, 뒤에서 만들 상품 상세 페이지, 상품 이미지를 불러오는 경우
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                // /admin으로 시작하는 경로는 해당 계정이 ADMIN Role일 경우만 접근 가능
                .anyRequest().authenticated(); // 나머지 경로들은 모두 인증을 요구한다.

        http.exceptionHandling() // 인증하지 않은 사용자가 리소스에 접근하였을 경우 수행되는 핸들러
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        // static 디렉터리 하위 파일은 인증을 무시함
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }

    // 비밀번호를 데이터베이스에 그대로 저장했을 경우,
    // 데이터베이스가 해킹당하면 고객의 회원 정보가 그대로 노출됩니다.
    // 이를 해결하기 위해 암호화 하여 저장
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService)
                .passwordEncoder(passwordEncoder());
    }
}
