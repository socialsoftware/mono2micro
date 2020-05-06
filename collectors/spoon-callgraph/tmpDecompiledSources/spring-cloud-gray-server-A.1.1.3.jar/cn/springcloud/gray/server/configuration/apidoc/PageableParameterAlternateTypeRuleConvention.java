/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 *  org.springframework.data.domain.Pageable
 *  springfox.documentation.schema.AlternateTypeRule
 *  springfox.documentation.schema.AlternateTypeRuleConvention
 */
package cn.springcloud.gray.server.configuration.apidoc;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;

public class PageableParameterAlternateTypeRuleConvention
implements AlternateTypeRuleConvention {
    private TypeResolver resolver;

    public PageableParameterAlternateTypeRuleConvention(TypeResolver resolver) {
        this.resolver = resolver;
    }

    public List<AlternateTypeRule> rules() {
        return new ArrayList<AlternateTypeRule>(Arrays.asList(new AlternateTypeRule[]{new AlternateTypeRule(this.resolver.resolve((Type)((Object)Pageable.class), new Type[0]), this.resolver.resolve((Type)((Object)Page.class), new Type[0]))}));
    }

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @ApiModel
    public static class Page {
        @ApiModelProperty(value="\u7b2cpage\u9875,\u4ece0\u5f00\u59cb\u8ba1\u6570", example="0")
        private Integer page = 0;
        @ApiModelProperty(value="\u6bcf\u9875\u6570\u636e\u6570\u91cf", example="10")
        private Integer size = 10;
        @ApiModelProperty(value="\u6309\u5c5e\u6027\u6392\u5e8f,\u683c\u5f0f:\u5c5e\u6027(,asc|desc)")
        private List<String> sort;

        public Integer getPage() {
            return this.page;
        }

        public Integer getSize() {
            return this.size;
        }

        public List<String> getSort() {
            return this.sort;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public void setSort(List<String> sort) {
            this.sort = sort;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Page)) {
                return false;
            }
            Page other = (Page)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Integer this$page = this.getPage();
            Integer other$page = other.getPage();
            if (this$page == null ? other$page != null : !((Object)this$page).equals(other$page)) {
                return false;
            }
            Integer this$size = this.getSize();
            Integer other$size = other.getSize();
            if (this$size == null ? other$size != null : !((Object)this$size).equals(other$size)) {
                return false;
            }
            List<String> this$sort = this.getSort();
            List<String> other$sort = other.getSort();
            return !(this$sort == null ? other$sort != null : !((Object)this$sort).equals(other$sort));
        }

        protected boolean canEqual(Object other) {
            return other instanceof Page;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Integer $page = this.getPage();
            result = result * 59 + ($page == null ? 43 : ((Object)$page).hashCode());
            Integer $size = this.getSize();
            result = result * 59 + ($size == null ? 43 : ((Object)$size).hashCode());
            List<String> $sort = this.getSort();
            result = result * 59 + ($sort == null ? 43 : ((Object)$sort).hashCode());
            return result;
        }

        public String toString() {
            return "PageableParameterAlternateTypeRuleConvention.Page(page=" + this.getPage() + ", size=" + this.getSize() + ", sort=" + this.getSort() + ")";
        }
    }

}

