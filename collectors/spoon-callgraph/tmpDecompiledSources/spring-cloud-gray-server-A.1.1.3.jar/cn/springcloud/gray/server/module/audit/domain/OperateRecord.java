/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.audit.domain;

import java.util.Date;

public class OperateRecord {
    public static final int OPERATE_STATE_SCUUESSED = 1;
    public static final int OPERATE_STATE_FAILED = 0;
    private String ip = "";
    private String operator = "";
    private Date operateTime;
    private String uri = "";
    private String httpMethod = "";
    private String queryString = "";
    private String handler = "";
    private String headlerArgs = "";
    private String apiResCode = "";
    private int operateState;

    public static OperateRecordBuilder builder() {
        return new OperateRecordBuilder();
    }

    public String getIp() {
        return this.ip;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public String getUri() {
        return this.uri;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getHandler() {
        return this.handler;
    }

    public String getHeadlerArgs() {
        return this.headlerArgs;
    }

    public String getApiResCode() {
        return this.apiResCode;
    }

    public int getOperateState() {
        return this.operateState;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public void setHeadlerArgs(String headlerArgs) {
        this.headlerArgs = headlerArgs;
    }

    public void setApiResCode(String apiResCode) {
        this.apiResCode = apiResCode;
    }

    public void setOperateState(int operateState) {
        this.operateState = operateState;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OperateRecord)) {
            return false;
        }
        OperateRecord other = (OperateRecord)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$ip = this.getIp();
        String other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) {
            return false;
        }
        Date this$operateTime = this.getOperateTime();
        Date other$operateTime = other.getOperateTime();
        if (this$operateTime == null ? other$operateTime != null : !((Object)this$operateTime).equals(other$operateTime)) {
            return false;
        }
        String this$uri = this.getUri();
        String other$uri = other.getUri();
        if (this$uri == null ? other$uri != null : !this$uri.equals(other$uri)) {
            return false;
        }
        String this$httpMethod = this.getHttpMethod();
        String other$httpMethod = other.getHttpMethod();
        if (this$httpMethod == null ? other$httpMethod != null : !this$httpMethod.equals(other$httpMethod)) {
            return false;
        }
        String this$queryString = this.getQueryString();
        String other$queryString = other.getQueryString();
        if (this$queryString == null ? other$queryString != null : !this$queryString.equals(other$queryString)) {
            return false;
        }
        String this$handler = this.getHandler();
        String other$handler = other.getHandler();
        if (this$handler == null ? other$handler != null : !this$handler.equals(other$handler)) {
            return false;
        }
        String this$headlerArgs = this.getHeadlerArgs();
        String other$headlerArgs = other.getHeadlerArgs();
        if (this$headlerArgs == null ? other$headlerArgs != null : !this$headlerArgs.equals(other$headlerArgs)) {
            return false;
        }
        String this$apiResCode = this.getApiResCode();
        String other$apiResCode = other.getApiResCode();
        if (this$apiResCode == null ? other$apiResCode != null : !this$apiResCode.equals(other$apiResCode)) {
            return false;
        }
        return this.getOperateState() == other.getOperateState();
    }

    protected boolean canEqual(Object other) {
        return other instanceof OperateRecord;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $ip = this.getIp();
        result = result * 59 + ($ip == null ? 43 : $ip.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        Date $operateTime = this.getOperateTime();
        result = result * 59 + ($operateTime == null ? 43 : ((Object)$operateTime).hashCode());
        String $uri = this.getUri();
        result = result * 59 + ($uri == null ? 43 : $uri.hashCode());
        String $httpMethod = this.getHttpMethod();
        result = result * 59 + ($httpMethod == null ? 43 : $httpMethod.hashCode());
        String $queryString = this.getQueryString();
        result = result * 59 + ($queryString == null ? 43 : $queryString.hashCode());
        String $handler = this.getHandler();
        result = result * 59 + ($handler == null ? 43 : $handler.hashCode());
        String $headlerArgs = this.getHeadlerArgs();
        result = result * 59 + ($headlerArgs == null ? 43 : $headlerArgs.hashCode());
        String $apiResCode = this.getApiResCode();
        result = result * 59 + ($apiResCode == null ? 43 : $apiResCode.hashCode());
        result = result * 59 + this.getOperateState();
        return result;
    }

    public String toString() {
        return "OperateRecord(ip=" + this.getIp() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ", uri=" + this.getUri() + ", httpMethod=" + this.getHttpMethod() + ", queryString=" + this.getQueryString() + ", handler=" + this.getHandler() + ", headlerArgs=" + this.getHeadlerArgs() + ", apiResCode=" + this.getApiResCode() + ", operateState=" + this.getOperateState() + ")";
    }

    public OperateRecord() {
    }

    public OperateRecord(String ip, String operator, Date operateTime, String uri, String httpMethod, String queryString, String handler, String headlerArgs, String apiResCode, int operateState) {
        this.ip = ip;
        this.operator = operator;
        this.operateTime = operateTime;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.queryString = queryString;
        this.handler = handler;
        this.headlerArgs = headlerArgs;
        this.apiResCode = apiResCode;
        this.operateState = operateState;
    }

    public static class OperateRecordBuilder {
        private String ip;
        private String operator;
        private Date operateTime;
        private String uri;
        private String httpMethod;
        private String queryString;
        private String handler;
        private String headlerArgs;
        private String apiResCode;
        private int operateState;

        OperateRecordBuilder() {
        }

        public OperateRecordBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public OperateRecordBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public OperateRecordBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public OperateRecordBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public OperateRecordBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public OperateRecordBuilder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public OperateRecordBuilder handler(String handler) {
            this.handler = handler;
            return this;
        }

        public OperateRecordBuilder headlerArgs(String headlerArgs) {
            this.headlerArgs = headlerArgs;
            return this;
        }

        public OperateRecordBuilder apiResCode(String apiResCode) {
            this.apiResCode = apiResCode;
            return this;
        }

        public OperateRecordBuilder operateState(int operateState) {
            this.operateState = operateState;
            return this;
        }

        public OperateRecord build() {
            return new OperateRecord(this.ip, this.operator, this.operateTime, this.uri, this.httpMethod, this.queryString, this.handler, this.headlerArgs, this.apiResCode, this.operateState);
        }

        public String toString() {
            return "OperateRecord.OperateRecordBuilder(ip=" + this.ip + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ", uri=" + this.uri + ", httpMethod=" + this.httpMethod + ", queryString=" + this.queryString + ", handler=" + this.handler + ", headlerArgs=" + this.headlerArgs + ", apiResCode=" + this.apiResCode + ", operateState=" + this.operateState + ")";
        }
    }

}

