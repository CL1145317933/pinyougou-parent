package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.group.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 10000)
    private CartService cartService;

    /**
     * 获取购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();//获取当前登陆的用户名,判断是否有人登录

            //读取本地购物车
            String cartListStr = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");

            if (cartListStr==null || cartListStr.equals("")) {
                cartListStr="[]";
            }
            List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        if (username.equals("anonymousUser")){//未登录
            return cartList_cookie;
        }else{//已登录
            List<Cart> cartList_redis = cartService.findCartFromRedis(username);
            if (cartList_cookie.size()>0){//存在本地购物车
                //合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //清除本地cookie数据
                util.CookieUtil.deleteCookie(request,response,"cartList");
                //将合并的购物车数据存入缓存
                cartService.saveCartToRedis(username,cartList);
            }
            return cartList_redis;
        }

    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//springMVC 4.2以上支持注解
    public Result addGoodsToCartList(Long itemId,Integer num) {
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();//获取当前登陆的用户名,判断是否有人登录

        try {
            //获取购物车列表
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            if (username.equals("anonymousUser")){
                util.CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");

            }else {
                cartService.saveCartToRedis(username,cartList);
            }
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
}
