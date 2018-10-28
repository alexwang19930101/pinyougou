package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {
    public List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);

    /**
     * 增加
     * @param tbBrand
     */
    public void add(TbBrand tbBrand);

    /**
     * 查找对应brand信息
     * @param id 主键id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 更新brand信息
     * @param tbBrand
     */
    public void update(TbBrand tbBrand);

    /**
     * 删除ids对应的信息
     * @param ids id列表
     */
    public void delete(Long[] ids);

    /**
     * 按条件查询商家
     * @param brand 商家信息
     * @param pageNum 查询页码
     * @param pageSize 页码显示数据量
     * @return
     */
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    public List<Map> selectOptionList();
}
