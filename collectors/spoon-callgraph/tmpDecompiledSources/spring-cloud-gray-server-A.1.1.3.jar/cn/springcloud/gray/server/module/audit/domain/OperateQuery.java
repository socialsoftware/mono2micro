/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.audit.domain;

import java.util.Date;

public class OperateQuery {
    private String operator;
    private String ip;
    private Date operateStartTime;
    private Date operateEndTime;
    private String apiResCode;
    private String uri;
    private String handler;
    private Integer operateState;

    public static OperateQueryBuilder builder() {
        return new OperateQueryBuilder();
    }

    public String getOperator() {
        return this.operator;
    }

    public String getIp() {
        return this.ip;
    }

    public Date getOperateStartTime() {
        return this.operateStartTime;
    }

    public Date getOperateEndTime() {
        return this.operateEndTime;
    }

    public String getApiResCode() {
        return this.apiResCode;
    }

    public String getUri() {
        return this.uri;
    }

    public String getHandler() {
        return this.handler;
    }

    public Integer getOperateState() {
        return this.operateState;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOperateStartTime(Date operateStartTime) {
        this.operateStartTime = operateStartTime;
    }

    public void setOperateEndTime(Date operateEndTime) {
        this.operateEndTime = operateEndTime;
    }

    public void setApiResCode(String apiResCode) {
        this.apiResCode = apiResCode;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public void setOperateState(Integer operateState) {
        this.operateState = operateState;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OperateQuery)) {
            return false;
        }
        OperateQuery other = (OperateQuery)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) {
            return false;
        }
        String this$ip = this.getIp();
        String other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) {
            return false;
        }
        Date this$operateStartTime = this.getOperateStartTime();
        Date other$operateStartTime = other.getOperateStartTime();
        if (this$operateStartTime == null ? other$operateStartTime != null : !((Object)this$operateStartTime).equals(other$operateStartTime)) {
            return false;
        }
        Date this$operateEndTime = this.getOperateEndTime();
        Date other$operateEndTime = other.getOperateEndTime();
        if (this$operateEndTime == null ? other$operateEndTime != null : !((Object)this$operateEndTime).equals(other$operateEndTime)) {
            return false;
        }
        String this$apiResCode = this.getApiResCode();
        String other$apiResCode = other.getApiResCode();
        if (this$apiResCode == null ? other$apiResCode != null : !this$apiResCode.equals(other$apiResCode)) {
            return false;
        }
        String this$uri = this.getUri();
        String other$uri = other.getUri();
        if (this$uri == null ? other$uri != null : !this$uri.equals(other$uri)) {
            return false;
        }
        String this$handler = this.getHandler();
        String other$handler = other.getHandler();
        if (this$handler == null ? other$handler != null : !this$handler.equals(other$handler)) {
            return false;
        }
        Integer this$operateState = this.getOperateState();
        Integer other$operateState = other.getOperateState();
        return !(this$operateState == null ? other$operateState != null : !((Object)this$operateState).equals(other$operateState));
    }

    protected boolean canEqual(Object other) {
        return other instanceof OperateQuery;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        String $ip = this.getIp();
        result = result * 59 + ($ip == null ? 43 : $ip.hashCode());
        Date $operateStartTime = this.getOperateStartTime();
        result = result * 59 + ($operateStartTime == null ? 43 : ((Object)$operateStartTime).hashCode());
        Date $operateEndTime = this.getOperateEndTime();
        result = result * 59 + ($operateEndTime == null ? 43 : ((Object)$operateEndTime).hashCode());
        String $apiResCode = this.getApiResCode();
        result = result * 59 + ($apiResCode == null ? 43 : $apiResCode.hashCode());
        String $uri = this.getUri();
        result = result * 59 + ($uri == null ? 43 : $uri.hashCode());
        String $handler = this.getHandler();
        result = result * 59 + ($handler == null ? 43 : $handler.hashCode());
        Integer $operateState = this.getOperateState();
        result = result * 59 + ($operateState == null ? 43 : ((Object)$operateState).hashCode());
        return result;
    }

    public String toString() {
        return "OperateQuery(operator=" + this.getOperator() + ", ip=" + this.getIp() + ", operateStartTime=" + this.getOperateStartTime() + ", operateEndTime=" + this.getOperateEndTime() + ", apiResCode=" + this.getApiResCode() + ", uri=" + this.getUri() + ", handler=" + this.getHandler() + ", operateState=" + this.getOperateState() + ")";
    }

    public OperateQuery() {
    }

    public OperateQuery(String operator, String ip, Date operateStartTime, Date operateEndTime, String apiResCode, String uri, String handler, Integer operateState) {
        this.operator = operator;
        this.ip = ip;
        this.operateStartTime = operateStartTime;
        this.operateEndTime = operateEndTime;
        this.apiResCode = apiResCode;
        this.uri = uri;
        this.handler = handler;
        this.operateState = operateState;
    }

    public static class OperateQueryBuilder {
        private String operator;
        private String ip;
        private Date operateStartTime;
        private Date operateEndTime;
        private String apiResCode;
        private String uri;
        private String handler;
        private Integer operateState;

        OperateQueryBuilder() {
        }

        public OperateQueryBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public OperateQueryBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public OperateQueryBuilder operateStartTime(Date operateStartTime) {
            this.operateStartTime = operateStartTime;
            return this;
        }

        public OperateQueryBuilder operateEndTime(Date operateEndTime) {
            this.operateEndTime = operateEndTime;
            return this;
        }

        public OperateQueryBuilder apiResCode(String apiResCode) {
            this.apiResCode = apiResCode;
            return this;
        }

        public OperateQueryBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public OperateQueryBuilder handler(String handler) {
            this.handler = handler;
            return this;
        }

        public OperateQueryBuilder operateState(Integer operateState) {
            this.operateState = operateState;
            return this;
        }

        public OperateQuery build() {
            return new OperateQuery(this.operator, this.ip, this.operateStartTime, this.operateEndTime, this.apiResCode, this.uri, this.handler, this.operateState);
        }

        public String toString() {
            return "OperateQuery.OperateQueryBuilder(operator=" + this.operator + ", ip=" + this.ip + ", operateStartTime=" + this.operateStartTime + ", operateEndTime=" + this.operateEndTime + ", apiResCode=" + this.apiResCode + ", uri=" + this.uri + ", handler=" + this.handler + ", operateState=" + this.operateState + ")";
        }
    }

}

