package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Jack
 * @create 2018-11-25 19:10
 * 统一的异常处理
 */
@ControllerAdvice//底层采用的是aop实现的
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class)//异常通知
    public ResponseEntity<ExceptionResult> handleException(LyException exception) {
        return ResponseEntity.status(exception.getExceptionEnum().getCode()).
                body(new ExceptionResult(exception.getExceptionEnum()));
    }
}
