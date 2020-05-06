/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.dao.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="gray_operate_record", indexes={@Index(columnList="operator"), @Index(columnList="operateTime")})
public class OperateRecordDO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(length=32)
    private String ip;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;
    @Column(length=256)
    private String uri;
    @Column(length=16)
    private String httpMethod;
    @Column(length=512)
    private String queryString;
    @Column(length=256)
    private String handler;
    @Column(length=1024)
    private String headlerArgs;
    @Column(length=16)
    private String apiResCode;
    @Column(length=2)
    private int operateState;

    public Long getId() {
        return this.id;
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

    public void setId(Long id) {
        this.id = id;
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
        if (!(o instanceof OperateRecordDO)) {
            return false;
        }
        OperateRecordDO other = (OperateRecordDO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
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
        return other instanceof OperateRecordDO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
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
        return "OperateRecordDO(id=" + this.getId() + ", ip=" + this.getIp() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ", uri=" + this.getUri() + ", httpMethod=" + this.getHttpMethod() + ", queryString=" + this.getQueryString() + ", handler=" + this.getHandler() + ", headlerArgs=" + this.getHeadlerArgs() + ", apiResCode=" + this.getApiResCode() + ", operateState=" + this.getOperateState() + ")";
    }
}

