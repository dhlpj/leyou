package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jack
 * @create 2019-02-14 11:41
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";

    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return 可用为true，否则false
     */
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        //判断数据类型
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user)==0;
    }

    /**
     * 发送验证码
     * @param phone
     */
    public void sendCode(String phone) {
        String code = NumberUtils.generateCode(6);
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("sms.verify.code",msg);
        //保存验证码到redis中
        String key = KEY_PREFIX+phone;
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    @Transactional
    public void register(User user, String code) {
        if(code==null){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if(cacheCode==null||!cacheCode.equals(code)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //生成盐值
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        //根据用户名查询用户
        User user = new User();
        user.setUsername(username);
        User record = userMapper.selectOne(user);
        if (record==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        String salt = record.getSalt();
        //验证
        if (!StringUtils.equals(record.getPassword(),CodecUtils.md5Hex(password,salt))){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return record;
    }
}
