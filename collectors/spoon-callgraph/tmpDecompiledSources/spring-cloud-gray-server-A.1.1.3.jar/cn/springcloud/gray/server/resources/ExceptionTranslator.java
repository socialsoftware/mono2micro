/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  cn.springcloud.gray.exceptions.NotFoundException
 *  org.springframework.http.HttpStatus
 *  org.springframework.web.bind.annotation.ControllerAdvice
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.ResponseBody
 *  org.springframework.web.bind.annotation.ResponseStatus
 */
package cn.springcloud.gray.server.resources;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.exceptions.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionTranslator {
    private static final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    @ExceptionHandler(value={NotFoundException.class})
    @ResponseStatus(value=HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiRes<Void> processNotFoundException(NotFoundException ex) {
        log.error("{}", (Object)ex.getMessage(), (Object)ex);
        return ApiRes.builder().code("404").message(StringUtils.defaultIfEmpty(ex.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase())).build();
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiRes<Void> processIllegalArgumentException(IllegalArgumentException ex) {
        log.error("{}", (Object)ex.getMessage(), (Object)ex);
        return ApiRes.builder().code(String.valueOf((Object)HttpStatus.BAD_REQUEST)).message(StringUtils.defaultIfEmpty(ex.getMessage(), "instance host or port is empty")).build();
    }

    @ExceptionHandler(value={Exception.class})
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiRes<Void> processException(Exception ex) {
        log.error("{}", (Object)ex.getMessage(), (Object)ex);
        return ApiRes.builder().code(String.valueOf((Object)HttpStatus.BAD_REQUEST)).message(StringUtils.defaultIfEmpty(ex.getMessage(), "error")).build();
    }
}

