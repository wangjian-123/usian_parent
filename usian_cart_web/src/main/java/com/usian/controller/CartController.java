package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/frontend/cart")
public class CartController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @Autowired
    private CartServiceFeign cartServiceFeign;

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @Value("${CART_COOKIE_EXPIRE}")
    private Integer CART_COOKIE_EXPIRE;

    /**
     * 加入购物车
     * @param userId
     * @param itemId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/addItem")
    public Result addItem(String userId, Long itemId,
                          @RequestParam(defaultValue = "1") Integer num, HttpServletRequest request,
                          HttpServletResponse response){
        try {
            /***********在用户未登录的状态下**********/
            if(StringUtils.isBlank(userId)){
                //获取购物车
                Map<String, TbItem> cart = getCartFromCookie(request);
                //添加商品到购物车
                addItemToCart(itemId,num,cart);
                //将购物车加入cookie
                addClientCookie(response,cart,request);
            }
            /****在用户已登录的状态****/
            else{
                //获取购物车
                Map<String,TbItem> cart = getCartFromRedis(userId);
                //添加商品到购物车
                addItemToCart(itemId,num,cart);
                //将购物车加入redis
                Boolean aBoolean = addItemToRedis(userId,cart);
                if (aBoolean){
                    return Result.ok();
                }
                return Result.error("error");
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    private Boolean addItemToRedis(String userId, Map<String, TbItem> cart) {
        return cartServiceFeign.insertCart(userId,cart);
    }


    /**
     * 从Redis获取购物车
     * @param userId
     * @return
     */
    private Map<String, TbItem> getCartFromRedis(String userId) {
        Map<String, TbItem> cart = cartServiceFeign.getCartFromRedis(userId);
        if(cart!=null && cart.size()>0){
            return cart;
        }
        return new HashMap<String, TbItem>();
    }

    /**
     * 将购物车加入cookie
     * @param response
     * @param cart
     * @param request
     */
    private void addClientCookie(HttpServletResponse response, Map<String, TbItem> cart,
                                 HttpServletRequest request) {
        String cartJson = JsonUtils.objectToJson(cart);
        CookieUtils.setCookie(request,response,CART_COOKIE_KEY,cartJson,
                CART_COOKIE_EXPIRE,true);

    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @param cart
     */
    private void addItemToCart(Long itemId, Integer num, Map<String, TbItem> cart) {
        //查询购物有无该商品
        TbItem tbItem = cart.get(itemId.toString());
        //如果有修改库存数量
        if(tbItem!=null){
            tbItem.setNum(tbItem.getNum()+num);
        }
        //如果没有在数据库查，设置库存量，
        else{
            tbItem = itemServiceFeign.selectItemInfo(itemId);
            tbItem.setNum(num);
        }
        //添加到购物车
        cart.put(itemId.toString(),tbItem);
    }

    /**
     * 从cookie获取购物车
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cookieValue = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
        if(StringUtils.isNotBlank(cookieValue)){
            Map<String, TbItem> map = JsonUtils.jsonToMap(cookieValue, TbItem.class);
            return map;
        }
        return new HashMap<String,TbItem>();
    }

    /**
     * 查看购物车
     * @param userId
     * @param request
     * @return
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId,HttpServletRequest request){
        try {
            List<TbItem> itemList = new ArrayList<>();
            /***********在用户未登录的状态下**********/
            if(StringUtils.isBlank(userId)){
                //未登录
                Map<String,TbItem> cart = getCartFromCookie(request);
                Set<String> keys = cart.keySet();
                for (String key : keys) {
                    TbItem tbItem = cart.get(key);
                    itemList.add(tbItem);
                }

            }else{
                //登录
            }
            return Result.ok(itemList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 修改购物车
     * @param itemId
     * @param userId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/updateItemNum")
    public Result updateItemNum(Long itemId,String userId,Integer num,
                                HttpServletRequest request,HttpServletResponse response){
        try{
            /***********在用户未登录的状态下**********/
            if(StringUtils.isBlank(userId)){
                Map<String, TbItem> cart = getCartFromCookie(request);
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                addClientCookie(response,cart,request);

            }else{

            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 购物车删除
     * @param userId
     * @param itemId
     * @param request
     * @return
     */
    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(String userId,Long itemId,
                                     HttpServletRequest request,HttpServletResponse response){
        try {
            if(StringUtils.isBlank(userId)){
                Map<String, TbItem> cart = getCartFromCookie(request);
                cart.remove(itemId.toString());
                addClientCookie(response,cart,request);
            }else{

            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }

    }
}
