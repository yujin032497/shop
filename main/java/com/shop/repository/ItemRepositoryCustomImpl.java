package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.QItemImg;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

// 인터페이스를 구현하는 것은 ItemRepositoryCustom 인터페이스를 구현하는 ItemRepositoryCustomImpl 클래스를 작성
// "Impl"을 붙여주어야 정상적으로 동작
// BooleanExpression 이라는 where절에서 사용할 수 있는 값을 지원
// BooleanExpression을 반환하는 메소드를 만들고 해당 조건들을 다른 쿼리를 생성할 때 사용할 수 있기 때문에 중복 코드를 줄일 수 있다는 장점
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private JPAQueryFactory queryFactory; // 동적으로 쿼리를 생성하기 위해서 JPAQueryFactory 클래스를 사용합니다.

    public ItemRepositoryCustomImpl(EntityManager em) { // JPAQueryFactory의 생성자로 EntityManager 객체를 넣어줍니다.
        this.queryFactory = new JPAQueryFactory(em);
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus ==
                null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
        // 상품 판매 상태 조건이 전체(null)일 경우는 null을 리턴합니다.
        // 결과값이 null이면 where절에서는 해당 조건은 무시됩니다.
        // 상품 판매 조건이 null이 아니라 판매중 or 품절 상태라면 해당 조건의 상품만 조회
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null) { // 상품 등록일 전체
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) { // 최근 하루 동안 등록된 상품
            dateTime = dateTime.minusDays(1);
        } else if(StringUtils.equals("iw", searchDateType)) { // 최근 일주일 동안 등록된 상품
            dateTime = dateTime.minusWeeks(1);
        } else if(StringUtils.equals("1m", searchDateType)) { // 최근 한달 동안 등록된 상품
            dateTime = dateTime.minusMonths(1);
        } else if(StringUtils.equals("6m", searchDateType)) { // 최근 6개월 동안 등록된 상품
            dateTime =dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        // searchBy의 값에 따라서 상품명에 검색어를 포함하고 있는 상품 또는 상품 생성자의 아이디에
        // 검색어를 포함하고 있는 상품을 조회하도록 조건값을 반환
        if(StringUtils.equals("itemNm", searchBy)) {
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("creaetBy", searchBy)) {
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QueryResults<Item> results = queryFactory // 이제 queryFacotry을 이용해서 쿼리를 생성
                .selectFrom(QItem.item) // 상품데이터를 조회하기 위해서 QItem의 item을 지정
                .where(regDtsAfter(itemSearchDto.getSearchDateType()), // BooleanExpression 반환하는 조건문들을 넣어줍니다.
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()), // ','단위로 넣어줄 경우 and 조건으로 인식
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()) // offet: 데이터를 가지고 올 시작 인덱스를 지정
                .limit(pageable.getPageSize()) // limit: 한 번에 가지고 올 최대 개수를 지정
                .fetchResults(); // 조회한 리스트 및 전체 개수를 포함하는 QueryResults을 반환

        List<Item> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total); // 조회한 데이터를 Page 클래스의 구현체인 PageImpl 객체를 반환
    }

    private BooleanExpression itemNmLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)
                .join(itemImg.item, item) // 내부조인
                .where(itemImg.repimgYn.eq("Y")) // 대표이미지만 불러옴
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable, total);
    }
}
