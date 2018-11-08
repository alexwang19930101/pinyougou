package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[])objectMessage.getObject();
            System.out.println("ItemDeleteListener 监听接收到消息...");
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("solr中索引成功删除，goodsIds：\r\n"+Arrays.toString(goodsIds));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
