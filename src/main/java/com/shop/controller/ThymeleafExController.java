package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shop.dto.ItemDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
// 클라이언트의 요청에 대해서 어떤 컨트롤러가 처리할지 매핑하는 어노테이션
@RequestMapping(value="/thymeleaf")
public class ThymeleafExController {

    @GetMapping(value ="/ex01")
    // model 객체를 이용해 뷰에 전달한 데이터를 key, value 구조로 넣어줌
    public String ThymeleafExample01(Model model) {
        model.addAttribute("data", "타임리프 예제입니다.");
        // templates 폴더를 기준으로 뷰의 위치와 이름을 반환
        return "thymeleafEx/thymeleafEx01";
    }

    @GetMapping(value = "/ex02")
    public String thymeleafExample02(Model model) {
        ItemDto itemDto = new ItemDto();
        itemDto.setItemDetail("상품 상세 설명");
        itemDto.setItemNm("테스트 상품1");
        itemDto.setPrice(10000);
        itemDto.setRegTime(LocalDateTime.now());

        model.addAttribute("itemDto", itemDto);
        return "thymeleafEx/thymeleafEx02";
    }

    @GetMapping(value = "/ex03")
    public String thymeleafExample03(Model model) {
        List<ItemDto> itemDtoList = new ArrayList<>();

        for(int i=1;i<=10;i++) {

            ItemDto itemDto = new ItemDto();
            itemDto.setItemDetail("상품 상세 설명"+i);
            itemDto.setItemNm("테스트 상품" + i);
            itemDto.setPrice(1000*i);
            itemDto.setRegTime(LocalDateTime.now());

            itemDtoList.add(itemDto);
        }

        model.addAttribute("itemDtoList", itemDtoList);
        return "thymeleafEx/thymeleafEx03";
    }
    @GetMapping(value = "/ex04")
        public String thymeleafExample04(Model model) {

            List<ItemDto> itemDtoList = new ArrayList<>();

            for(int i=1;i<=10;i++) {
                ItemDto itemDto = new ItemDto();
                itemDto.setItemDetail("상품 상세 설명" + i);
                itemDto.setItemNm("테스트 상품" + i);
                itemDto.setPrice(1000 * i);
                itemDto.setRegTime(LocalDateTime.now());

                itemDtoList.add(itemDto);
            }
            model.addAttribute("itemDtoList", itemDtoList);
            return "thymeleafEx/thymeleafEx04";
    }
}
