package com.pinyougou.page.service;

public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean getItemHtml(Long goodsId);

    boolean  deleteItemHtml(Long[] ids);

}
