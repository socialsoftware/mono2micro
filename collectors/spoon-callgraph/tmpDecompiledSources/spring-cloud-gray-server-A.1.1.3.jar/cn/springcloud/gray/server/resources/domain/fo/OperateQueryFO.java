/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 *  javax.validation.constraints.NotNull
 */
package cn.springcloud.gray.server.resources.domain.fo;

import cn.springcloud.gray.server.module.audit.domain.OperateQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.NotNull;

@ApiModel
public class OperateQueryFO {
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba\u7684id")
    private String operator;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba\u7684ip")
    private String ip;
    @NotNull(message="\u5f00\u59cb\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a")
    @ApiModelProperty
    private Date startTime;
    @NotNull(message="\u7ed3\u675f\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a")
    @ApiModelProperty
    private Date endTime;
    @ApiModelProperty(value="\u7ed3\u679c\u7684code\u7801")
    private String apiResCode;
    @ApiModelProperty(value="\u64cd\u4f5c\u7684Uri")
    private String uri;
    @ApiModelProperty(value="\u64cd\u4f5c\u7684RequestHandler")
    private String handler;
    @ApiModelProperty(value="\u64cd\u4f5c\u7ed3\u8bba, -1:all, 0:failed, 1: scuuessed", example="1", allowableValues="-1,0,1")
    private int operateState;

    public OperateQuery toOperateQuery() {
        return OperateQuery.builder().apiResCode(this.apiResCode).operateEndTime(this.endTime).operateStartTime(this.startTime).ip(this.ip).uri(this.uri).handler(this.handler).operator(this.operator).operateState(this.operateState == -1 ? null : Integer.valueOf(this.operateState)).build();
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getApiResCode() {
        return this.apiResCode;
    }

    public void setApiResCode(String apiResCode) {
        this.apiResCode = apiResCode;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHandler() {
        return this.handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public int getOperateState() {
        return this.operateState;
    }

    public void setOperateState(int operateState) {
        this.operateState = operateState;
    }
}

