package com.chen.spring.boot.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chendurex
 * @description
 * @date 2018-03-18 12:33
 */
@Data
@ApiModel(value = "返回结果")
public class ResResult<T> {
    @ApiModelProperty(value = "返回状态(1成功，-1失败)", required = true)
    private final int status;
    @ApiModelProperty(value = "返回说明，如果失败，则会有说明信息", required = true)
    private final String desc;
    @ApiModelProperty(value = "返回数据", required = true)
    private final T data;

    public ResResult(int status, String desc, T data) {
        this.status = status;
        this.desc = desc;
        this.data = data;
    }

}
