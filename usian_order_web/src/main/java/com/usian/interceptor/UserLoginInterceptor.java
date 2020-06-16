package com.usian.interceptor;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class UserLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    /**
     * 结算前用户校验
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //查看cookie中是否存在
        String token = request.getParameter("token");
        if(StringUtils.isBlank(token)){
            return false;
        }
        //查看redis中否过期
        TbUser tbUser = ssoServiceFeign.getUserByToken(token);
        if(tbUser == null){
            return false;
        }
        return true;
    }
}
