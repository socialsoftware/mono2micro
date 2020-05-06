/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageImpl
 *  org.springframework.data.domain.Pageable
 *  org.springframework.http.HttpHeaders
 *  org.springframework.util.StringUtils
 *  org.springframework.web.util.UriComponentsBuilder
 */
package cn.springcloud.gray.server.utils;

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class PaginationUtils {
    private static final Logger log = LoggerFactory.getLogger(PaginationUtils.class);
    public static final ObjectMapper PAGINATION_OBJECT_MAPPER = new ObjectMapper();

    public static <MODEL, T> Page<MODEL> convert(Pageable pageable, Page<T> p, ModelMapper<MODEL, T> modelMapper) {
        List<MODEL> list = modelMapper.dos2models(p.getContent());
        return new PageImpl(list, pageable, p.getTotalElements());
    }

    public static <MODEL, T> Page<MODEL> convert(Pageable pageable, Page<T> p, List<MODEL> models) {
        return new PageImpl(models, pageable, p.getTotalElements());
    }

    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page) {
        return PaginationUtils.generatePaginationHttpHeaders(page, null);
    }

    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page, String baseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "" + page.getTotalElements());
        if (!StringUtils.isEmpty((Object)baseUrl)) {
            Pagination pagination = new Pagination();
            int lastPage = 0;
            if (page.getTotalPages() > 0) {
                lastPage = page.getTotalPages() - 1;
            }
            if (page.getNumber() + 1 < page.getTotalPages()) {
                pagination.setNext(PaginationUtils.generateUri(baseUrl, page.getNumber() + 1, page.getSize()));
            }
            if (page.getNumber() > 0) {
                pagination.setPrev(PaginationUtils.generateUri(baseUrl, page.getNumber() - 1, page.getSize()));
            }
            pagination.setLast(PaginationUtils.generateUri(baseUrl, lastPage, page.getSize()));
            pagination.setFirst(PaginationUtils.generateUri(baseUrl, 0, page.getSize()));
            try {
                headers.add("X-Pagination", PAGINATION_OBJECT_MAPPER.writeValueAsString(pagination));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return headers;
    }

    public static String generateUri(String baseUrl, int page, int size) {
        return UriComponentsBuilder.fromUriString((String)baseUrl).queryParam("page", new Object[]{page}).queryParam("size", new Object[]{size}).toUriString();
    }

    static class Pagination {
        private String next;
        private String prev;
        private String last;
        private String first;

        Pagination() {
        }

        public String getNext() {
            return this.next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrev() {
            return this.prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }

        public String getLast() {
            return this.last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getFirst() {
            return this.first;
        }

        public void setFirst(String first) {
            this.first = first;
        }
    }

}

