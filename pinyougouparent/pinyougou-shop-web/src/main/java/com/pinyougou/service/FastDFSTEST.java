package com.pinyougou.service;

import org.csource.fastdfs.*;
import org.junit.Test;

public class FastDFSTEST {
    @Test
    public void testFileUpdload() throws Exception {
        // 1、加载配置文件，配置文件中的内容就是 tracker 服务的地址。
        // 这里需要写绝对路径：
        //this.getClass().getResource("/").getPath();
        ClientGlobal.init("E:\\ideaworkspace\\pinyougou\\pinyougouparent\\pinyougou-shop-web\\src\\main\\resources\\config\\fdfs_client.conf");
        // 2、创建一个 TrackerClient 对象。直接 new 一个。
        TrackerClient trackerClient = new TrackerClient();
        // 3、使用 TrackerClient 对象创建连接，获得一个 TrackerServer 对象。
        TrackerServer trackerServer = trackerClient.getConnection();
        // 4、创建一个 StorageServer 的引用，值为 null
        StorageServer storageServer = null;
        // 5、创建一个 StorageClient 对象，需要两个参数 TrackerServer 对象、StorageServer的引用
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        // 6、使用 StorageClient 对象上传图片。
        String[] strings = storageClient.upload_file("C:\\Pic\\1.jpg", "jpg", null);
        //扩展名不带“.”
        // 7、返回数组。包含组名和图片的路径。
        for (String str:strings){
            System.out.println(str);
        }
    }
}
