package com.shanjupay.common.util;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.google.gson.Gson;
import com.qiniu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * 七牛云服务测试工具类
 */
public class QiniuUtils{

    private static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);
    /**
     * 上传文件的工具方法
     * @param accessKey
     * @param secretKey
     * @param bucket
     * @param bytes
     * @param fileName 外部传进来，七牛云上的文件名和此保持一致
     */
    public static void upload2qiniu(String accessKey, String secretKey, String bucket, byte[] bytes, String fileName)
            throws RuntimeException{
        //构造一个带指定 Region 对象的配置类:华南
        Configuration cfg = new Configuration(Region.region2());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        String key = fileName;
        try {
            //认证
            Auth auth = Auth.create(accessKey, secretKey);
            //认证通过后得到令牌
            String upToken = auth.uploadToken(bucket);
            try {
                //上传文件，参数：字节数组，key(文件名)，令牌
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
                try {
                    LOGGER.error(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                throw new RuntimeException(r.bodyString());
            }
        } catch (Exception ex) {
            //ignore
            LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static void testUpload(){
        //构造一个带指定 Region 对象的配置类:华南
        Configuration cfg = new Configuration(Region.region2());
        //...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        String accessKey = "DcXzC1Db8JQ2D3miSLeHiQyUGn-BjdEj6_Q-Uxpm";
        String secretKey = "7djXClVVO3HK190H0FGJwMjZ7rSWH2Yom6BEkPID";
        String bucket = "shanjupa";

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = UUID.randomUUID().toString() + ".png";
        FileInputStream fileInputStream = null;
        try {
            //得到本地文件的字节数组
            String filePath = "C:\\Users\\陈德川\\OneDrive\\桌面\\Snipaste_2021-11-28_16-06-09.png";
            fileInputStream = new FileInputStream(new File(filePath));
            //得到本地文件字节数组
            byte[] bytes = IOUtils.toByteArray(fileInputStream);
            //byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
            //认证
            Auth auth = Auth.create(accessKey, secretKey);
            //认证通过后得到令牌
            String upToken = auth.uploadToken(bucket);

            try {
                //上传文件，参数：字节数组，key(文件名)，令牌
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
                try {
                    LOGGER.error(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                throw new RuntimeException(r.bodyString());
            }
        } catch (Exception ex) {
            //ignore
            LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
        }

    }

    private static void testDownload() throws UnsupportedEncodingException {
        String fileName = "a5ca5fad-0244-4790-abb9-c411175334dd.png";
        String domainOfBucket = "http://r39tyfhnj.hn-bkt.clouddn.com";
        String encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);

        String accessKey = "DcXzC1Db8JQ2D3miSLeHiQyUGn-BjdEj6_Q-Uxpm";
        String secretKey = "7djXClVVO3HK190H0FGJwMjZ7rSWH2Yom6BEkPID";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);

    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        //测试上传
        //QiniuUtils.testUpload();
        //测试下载
        QiniuUtils.testDownload();
    }
}