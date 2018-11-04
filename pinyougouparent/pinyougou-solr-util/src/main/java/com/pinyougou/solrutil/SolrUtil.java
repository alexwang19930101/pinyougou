package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData(){
        TbItemExample exam = new TbItemExample();
        TbItemExample.Criteria criteria = exam.createCriteria();
        criteria.andStatusEqualTo("1");

        List<TbItem> itemList = itemMapper.selectByExample(exam);

        System.out.println("===商品列表===");
        for(TbItem item:itemList){
            System.out.println(item.getId()+" "+item.getTitle()+" "+item.getPrice());
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(specMap);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("===结束===");
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "classpath*:**/application*.xml");
        SolrUtil solrUtil = applicationContext.getBean(SolrUtil.class);
        solrUtil.importItemData();
    }
}
