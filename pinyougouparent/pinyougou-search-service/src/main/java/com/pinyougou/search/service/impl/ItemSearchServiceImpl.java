package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 8000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    public Map<String, Object> search(Map searchMap) {
        //处理空格
        searchMap.put("keywords",
                ((String)searchMap.get("keywords")).replaceAll(" ",""));

        //Map<String,Object> map = searchList(searchMap);
        //1.获取搜索高亮结果集(可以使用上面方式，但是为了保持风格一致使用了下面方式)
        //searchList(searchMap)中已经将结果放入map.rows中
        Map<String, Object> map = new HashMap<>();
        map.putAll(searchList(searchMap));

        //2.分组信息查询
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3.品牌与规格信息查询
        if (!"".equals(searchMap.get("category"))) {
            map.putAll(searchbrandAndSpec((String) searchMap.get("category")));
        } else {
            if (categoryList != null && categoryList.size() > 0) {
                map.putAll(searchbrandAndSpec((String) categoryList.get(0)));
            }
        }

        return map;
    }

    private Map searchList(Map searchMap) {
        //结果集合
        Map map = new HashMap();
        //高亮查询字段设置
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);
        /*
         * 设置查询字段,默认直接查询所有
         *  1.1 设置item_brand条件
         *  1.2 设置item_category条件
         *  1.3 设置item_spec_*动态字段，*用对应searchMap.spec[i]的key替换
         *  1.4 设置价格区间
         *  1.5 设置分页查询
         *  1.6 设置排序
         */
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            query.addFilterQuery(new SimpleFacetQuery(filterCriteria));
        }

        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            query.addFilterQuery(new SimpleFacetQuery(filterCriteria));
        }

        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                query.addFilterQuery(new SimpleFacetQuery(filterCriteria));
            }
        }

        if (!"".equals(searchMap.get("price"))){
            String[] priceList = ((String) searchMap.get("price")).split("-");
            if (!"0".equals(priceList[0])){
                Criteria filterCriteria = new Criteria("item_price").greaterThan(priceList[0]);
                query.addFilterQuery(new SimpleFacetQuery(filterCriteria));
            }
            if (!"*".equals(priceList[1])){
                Criteria filterCriteria = new Criteria("item_price").lessThan(priceList[1]);
                query.addFilterQuery(new SimpleFacetQuery(filterCriteria));
            }
        }

        Integer pageNo = (Integer) searchMap.get("pageNo");
        pageNo = pageNo==null?1:pageNo;//处理为空
        Integer pageSize = (Integer) searchMap.get("pageSize");
        pageSize = pageSize==null?20:pageSize;

        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        String sort = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if (sortField!=null && sort!=null && !"".equals(sortField) && !"".equals(sort)){
            if (sort.equalsIgnoreCase("ASC")){
                query.addSort(new Sort(Sort.Direction.ASC,"item_"+sortField));
            }else if (sort.equalsIgnoreCase("DESC")){
                query.addSort(new Sort(Sort.Direction.DESC,"item_"+sortField));
            }
        }

        //*************对于TbItem的title字段进行高亮处理****************
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //遍历HighlightPage集合中的数据List<HighlightEntry<T>>
        for (HighlightEntry<TbItem> entry : highlightPage.getHighlighted()) {
            //可以通过高亮如果得到高亮内容与非高亮结果进行处理
            TbItem entity = entry.getEntity();
            //若字段多值，可以遍历entry.getHighlights结果集合List<Highlist>
            // 通过获得对象Highlist的getSnipplets()得到List<String>最终的高亮结果
            //这里的高亮只对item_title字段进行了处理所以可以get(0)获取Highlight对象
            if (entry.getHighlights() != null && entry.getHighlights().size() > 0 &&
                    entry.getHighlights().get(0).getSnipplets() != null &&
                    entry.getHighlights().get(0).getSnipplets().size() > 0) {
                entity.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        //将结果放入map中返回rows是前段取用的搜索结果字段
        //highlightPage.getContent()的每个entity和高亮入口可以得到的entity是统一的
        map.put("rows", highlightPage.getContent());
        map.put("totalPages",highlightPage.getTotalPages());
        map.put("total",highlightPage.getTotalElements());
        return map;
    }

    //分组查询
    private List<String> searchCategoryList(Map searchMap) {
        List<String> catList = new ArrayList<>();

        //建立查询，添加查询条件  where
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //为查询添加分组条件  group by
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //处理结果集合
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据上面的分组字段获得结果（这个方法类似与上面的getContent()，但是对于分组查询不会返回一般的结果集，所以方法有不同）
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //获得分组结果入口  Page<GroupEntry<TbItem>>
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获得分组入口集合  List<GroupEntry<TbItem>>
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> item : content) {
            catList.add(item.getGroupValue());
        }
        return catList;
    }

    //品牌与规格信息查询
    private Map<String, Object> searchbrandAndSpec(String category) {
        Map<String, Object> map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板 ID
        if (typeId != null) {
            //根据模板 ID 查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);

            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        query.addCriteria(new Criteria("item_goodsid").in(goodsIdList));

        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
