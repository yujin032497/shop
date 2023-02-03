package com.shop.exception;

// 재고의 수가 적을 때 발생시킬 exception
public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }

}
