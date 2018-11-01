package com.pinyougou.manager.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UpLoadFileController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        String fileName = file.getOriginalFilename();
        String extName = fileName.substring(fileName.lastIndexOf(",")+1);

        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            String filePath = fastDFSClient.uploadFile(file.getBytes(), extName, null);

            String fileURL = FILE_SERVER_URL+filePath;
            return new Result(true,fileURL);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"文件上传失败");
        }
    }

    @RequestMapping("/kindedit_upload")
    public Map kindeditUpload(@RequestParam(value = "imgFile") MultipartFile file){
        String fileName = file.getOriginalFilename();
        String extName = fileName.substring(fileName.lastIndexOf(",")+1);

        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            String filePath = fastDFSClient.uploadFile(file.getBytes(), extName, null);

            String fileURL = FILE_SERVER_URL+filePath;

            Map<String,Object> map = new HashMap<>();
            map.put("error",0);
            map.put("url",fileURL);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,Object> map = new HashMap<>();
            map.put("error",1);
            map.put("message","上传失败");
            return map;
        }
    }
}
