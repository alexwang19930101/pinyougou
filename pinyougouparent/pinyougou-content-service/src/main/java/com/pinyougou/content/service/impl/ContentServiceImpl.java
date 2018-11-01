package com.pinyougou.content.service.impl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pinyougou.pojo.TbContentCategory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//先删除旧的redis分组数据
		Long oldCategoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		redisTemplate.boundHashOps("content").delete(oldCategoryId);

		contentMapper.updateByPrimaryKey(content);

		//删除插入后的redis分组数据
		if (!oldCategoryId.equals(content.getCategoryId())) {
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		/*for(Long id:ids){
			contentMapper.deleteByPrimaryKey(id);
		}*/

		Set<Long> categoryIdsSet = new HashSet<>();
		List<TbContent> contentList = contentMapper.batchSelectContentByIds(ids);
		for (TbContent content:contentList){
			Long categoryId = content.getCategoryId();
			if (categoryId != null){
				categoryIdsSet.add(categoryId);
			}
		}

		//批量删除缓存
		redisTemplate.boundHashOps("content").delete(categoryIdsSet.toArray());
		//批量删除数据
		contentMapper.batchDeleteContentByIds(ids);
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
			if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByContentCategoryId(Long categoryId) {
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

		if (contentList == null) {
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("sort_order");
			contentList = contentMapper.selectByExample(example);

			redisTemplate.boundHashOps("content").put(categoryId,contentList);

			System.out.println("从数据中取数据存入缓存");
		}else {
			System.out.println("从缓存取数据");
		}

		return contentList;
	}

}
