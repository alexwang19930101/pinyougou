package com.pinyougou.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentConntroller {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findByContentCategoryId")
    public List<TbContent> findByContentCategoryId(Long categoryId){
       return contentService.findByContentCategoryId(categoryId);
    }
}
