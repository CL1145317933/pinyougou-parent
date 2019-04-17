package com.pinyougou.cart.service;

import com.pinyougou.pojo.group.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {


    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 从Redis中获取购物车
     * @param username
     * @return
     */
    List<Cart> findCartFromRedis(String username);

    /**
     * 将购物车添加到缓存
     * @param username
     * @param cartList
     */
    void saveCartToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList_cookie,List<Cart> cartList_redis);
}
