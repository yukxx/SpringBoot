package com.yukx.spring_boot.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author yukx
 * @Date 2021/6/23
 **/

@ApiModel("用户实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @ApiModelProperty(value = "姓名", required = true)
    private String name;
    @ApiModelProperty(value = "性别", required = true)
    private String sex;
    @ApiModelProperty(value = "岁数", required = true)
    private Integer age;
    @ApiModelProperty(value = "生日")
    private Date birthday;
}
