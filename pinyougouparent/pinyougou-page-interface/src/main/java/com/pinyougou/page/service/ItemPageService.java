package com.pinyougou.page.service;

public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean genItemHtml(Long goodsId);

    //删除goodsIds关联的所有静态页面
    public boolean deleteItemHtml(Long[] ids);
}
