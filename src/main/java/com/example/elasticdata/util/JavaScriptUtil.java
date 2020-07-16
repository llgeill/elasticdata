package com.example.elasticdata.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;

/**
 * <h3>elasticdata</h3>
 * <p></p>
 *
 * @author : liliguang
 * @date : 2020-06-22 11:58
 **/
public class JavaScriptUtil {

    public static Invocable inv = null;

    static{
        try {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            ScriptEngine engine = engineManager.getEngineByName("JavaScript"); // 得到脚本引擎
            String reader = null;
            //获取文件所在的相对路径
            //String text = System.getProperty("user.dir");
            //reader = text + "\\src\\main\\resources\\test.js";
            Resource resource = new DefaultResourceLoader().getResource("classpath:static/nbjs/maphao.min.js");
            //FileReader fReader = new FileReader(file);
            InputStreamReader fReader = new InputStreamReader(resource.getInputStream());
            engine.eval(fReader);
            inv = (Invocable) engine;
            fReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 南宝加密
     * @param param
     * @return
     * @throws ScriptException
     * @throws FileNotFoundException
     * @throws NoSuchMethodException
     */
    public static String maphaoEncrypt(String param){
        try {
            //调用js中的方法
            Object test2 = inv.invokeFunction("maphaoEncrypt", param);
            String url = test2.toString();
            return url;
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 南宝解密
     * @param content
     * @return
     * @throws ScriptException
     * @throws FileNotFoundException
     * @throws NoSuchMethodException
     */
    public static String maphaoDecrypt(String content){
        try {
            //调用js中的方法
            Object test2 = inv.invokeFunction("maphaoDecrypt", content);
            String url = test2.toString();
            return url;
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
