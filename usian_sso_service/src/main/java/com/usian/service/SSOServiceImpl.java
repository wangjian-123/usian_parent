package com.usian.service;

import com.usian.mapper.TbUserMapper;
import com.usian.pojo.TbUser;
import com.usian.pojo.TbUserExample;
import com.usian.redis.RedisClient;
import com.usian.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SSOServiceImpl implements SSOService {

    @Autowired
    private TbUserMapper tbUserMapper;

    @Value("${USER_INFO}")
    private String USER_INFO;

    @Value("${SESSION_EXPIRE}")
    private Long SESSION_EXPIRE;

    @Autowired
    private RedisClient redisClient;

    /**
     * 用户注册校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @Override
    public Boolean checkUserInfo(String checkValue, Integer checkFlag) {
        // 1、查询条件根据参数动态生成：1、2分别代表username、phone
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        if(checkFlag==1){
            criteria.andUsernameEqualTo(checkValue);
        }else if(checkFlag==2){
            criteria.andPhoneEqualTo(checkValue);
        }
        // 2、从tb_user表中查询数据
        List<TbUser> tbUsers = tbUserMapper.selectByExample(tbUserExample);
        // 3、判断查询结果，如果查询到数据返回false。
        if(tbUsers!=null && tbUsers.size()>0){
            return false;
        }
        return true;
    }

    /**
     *用户注册
     * @param tbUser
     * @return
     */
    @Override
    public Integer userRegister(TbUser tbUser) {
        //将密码做加密处理。
        tbUser.setPassword(MD5Utils.digest(tbUser.getPassword()));
        //补齐数据
        Date date = new Date();
        tbUser.setCreated(date);
        tbUser.setUpdated(date);
        return tbUserMapper.insertSelective(tbUser);
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Map userLogin(String username, String password) {
        // 1、判断用户名密码是否正确。
        String pwd = MD5Utils.digest(password);
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andPasswordEqualTo(pwd);
        List<TbUser> tbUsers = tbUserMapper.selectByExample(tbUserExample);
        if(tbUsers!=null && tbUsers.size()>0){
            TbUser tbUser = tbUsers.get(0);
            // 2、登录成功后生成token。Token相当于原来的jsessionid，字符串，可以使用uuid。
            String token = UUID.randomUUID().toString();
            // 3、把用户信息保存到redis。Key就是token，value就是TbUser对象转换成json。
            tbUser.setPassword(null);
            redisClient.set(USER_INFO+":"+token,tbUser);
            // 5、设置key的过期时间。模拟Session的过期时间。
            redisClient.expire(USER_INFO+":"+token,SESSION_EXPIRE);
            Map<String, Object> map = new HashMap<>();
            map.put("token",token);
            map.put("userid",tbUser.getId());
            map.put("username",tbUser.getUsername());
            return map;
        }
        return null;
    }

    /**
     * 查询用户登录是否过期
     * @param token
     * @return
     */
    @Override
    public TbUser getUserByToken(String token) {
        TbUser tbUser = (TbUser) redisClient.get(USER_INFO + ":" + token);
        if(tbUser!=null){
            redisClient.expire(USER_INFO + ":" + token,SESSION_EXPIRE);
            return tbUser;
        }
        return null;
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @Override
    public Boolean logOut(String token) {
        return redisClient.del(USER_INFO+":"+token);
    }


}
