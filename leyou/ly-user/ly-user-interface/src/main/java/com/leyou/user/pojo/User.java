package com.leyou.user.pojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Table(name = "tb_user")
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Length(min = 4,max = 10,message = "用户名长度必须在4-10之间")
    private String username;// 用户名

    @NotBlank(message = "密码不能为空")
    @Length(min = 4,max = 15,message = "密码长度必须在4-15之间")
    @JsonIgnore
    private String password;// 密码

    @Pattern(regexp = "^1[35678]\\d{9}$", message = "手机号格式不正确")
    private String phone;// 电话

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值
}