//package com.example.elasticdata;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.example.elasticdata.entity.other.Aismsa;
//import com.example.elasticdata.entity.other.Track;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//
//import java.io.*;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@SpringBootTest
//class ElasticdataApplicationTests {
//
//    @Autowired
//    ElasticsearchRestTemplate elasticsearchRestTemplate;
//
//    static ExecutorService executorService = Executors.newFixedThreadPool(1);
//
//
//    public static void main(String[] args) {
//        File file = new File("C:\\Users\\NC053\\Desktop\\test.txt");
//        List<Aismsa> aismsas = new LinkedList<>();
//        try {
//            StringBuffer stringBuffer = new StringBuffer();
//            FileInputStream fileInputStream = new FileInputStream(file);
//            byte[] bytes = new byte[1024];
//            int size = -1;
//            while ((size=fileInputStream.read(bytes))>0){
//                stringBuffer.append(new String(bytes,0,size));
//            }
//            String string = stringBuffer.toString();
//            JSONArray jsonArray = JSON.parseArray(string);
//
//            double max = Double.MIN_VALUE;
//            double min = Double.MAX_VALUE;
//            for (int i=0;i<jsonArray.size();i++){
//                JSONArray jsonObject = jsonArray.getJSONArray(i);
//                double o1 = jsonObject.getDoubleValue(1);
//                double o2 = jsonObject.getDoubleValue(2);
//                double distance = distance(o2, o1);
//                max = Math.min(max, distance);
//                min = Math.min(min, distance);
//            }
////            Arrays.stream(split).forEach(s -> {
////                String[] split1 = s.split("@",-1);
////                Aismsa aismsa = t2(split1);
////                aismsas.add(aismsa);
////            });
//            System.out.println(max);
//            System.out.println(min);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 查询两点的距离
//     * @return
//     */
//    public static double distance(double ejd,double ewd){
//
//        double p1_lat = Math.toRadians(113.73103703982898);
//        double p1_lon = Math.toRadians(22.23720368852767);
//        double p2_lat = Math.toRadians(ejd);
//        double p2_lon = Math.toRadians(ewd);
//
//        double numerator = Math.sqrt(
//                Math.pow(Math.cos(p2_lat) * Math.sin(p2_lon - p1_lon), 2) +
//                        Math.pow(
//                                Math.cos(p1_lat) * Math.sin(p2_lat) -
//                                        Math.sin(p1_lat) * Math.cos(p2_lat) *
//                                                Math.cos(p2_lon - p1_lon), 2));
//
//        double denominator =  Math.sin(p1_lat) * Math.sin(p2_lat) +
//                Math.cos(p1_lat) * Math.cos(p2_lat) *
//                        Math.cos(p2_lon - p1_lon);
//
//        return Math.atan2(numerator, denominator) * 6372800;
//    }
//
////    public static void main(String[] args) {
////        File file = new File("C:\\Users\\NC053\\Desktop\\test.txt");
////        List<Aismsa> aismsas = new LinkedList<>();
////        try {
////            StringBuffer stringBuffer = new StringBuffer();
////            FileInputStream fileInputStream = new FileInputStream(file);
////            byte[] bytes = new byte[1024];
////            int size = -1;
////            while ((size=fileInputStream.read(bytes))>0){
////                stringBuffer.append(new String(bytes,0,size));
////            }
////            String string = stringBuffer.toString();
////            int ships = string.lastIndexOf("ships:");
////            String substring = string.substring(ships+7);
////            String[] split = substring.split("\\|\\|");
////            //MMSI  时间  经度  纬度  船迹向_船首向 船名 * IMO * * 船速 船旗 DWT * 长 宽 呼号 * * 目的港
////            Arrays.stream(split).forEach(s -> {
////                String[] split1 = s.split("@",-1);
////                Aismsa aismsa = t2(split1);
////                aismsas.add(aismsa);
////            });
////            System.out.println(1);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
////    public static void main(String[] args) {
////        File file = new File("C:\\Users\\NC053\\Desktop\\test.txt");
////        List<Aismsa> aismsas = new LinkedList<>();
////        try {
////            StringBuffer stringBuffer = new StringBuffer();
////            FileInputStream fileInputStream = new FileInputStream(file);
////            byte[] bytes = new byte[1024];
////            int size = -1;
////            while ((size=fileInputStream.read(bytes))>0){
////                stringBuffer.append(new String(bytes,0,size));
////            }
////            String string = stringBuffer.toString();
////            int ships = string.lastIndexOf("ships:");
////            String substring = string.substring(ships+7);
////            String[] split = substring.split("\\|\\|");
////            //MMSI  时间  经度  纬度  船迹向_船首向 船名 * IMO * * 船速 船旗 DWT * 长 宽 呼号 * * 目的港
////            Arrays.stream(split).forEach(s -> {
////                String[] split1 = s.split("@",-1);
////                Aismsa aismsa = t2(split1);
////                aismsas.add(aismsa);
////            });
////            System.out.println(1);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
//
//    public static Aismsa t2(String[] split){
//        /**
//         * 返回 Class 对象所表示的类或接口所声明的所有字段，
//         * 包括公共、保护、默认（包）访问和私有字段，但不包括继承的字段。
//         */
//        Field[] f=Aismsa.class.getDeclaredFields();
//
//        Aismsa aismsa=new Aismsa();
//        //给test对象赋值
//        for(int i=0;i<f.length;i++){
//            //获取属相名
//            String attributeName=f[i].getName();
//            //将属性名的首字母变为大写，为执行set/get方法做准备
//            String methodName=attributeName.substring(0,1).toUpperCase()+attributeName.substring(1);
//            try{
//                //获取Test类当前属性的setXXX方法（私有和公有方法）
//                /*Method setMethod=Test.class.getDeclaredMethod("set"+methodName);*/
//                //获取Test类当前属性的setXXX方法（只能获取公有方法）
//                Method setMethod=Aismsa.class.getMethod("set"+methodName,String.class);
//                //执行该set方法
//                setMethod.invoke(aismsa,split[i]);
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        LocalDateTime d = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(aismsa.getSJ())), ZoneId.systemDefault());
//        aismsa.setSJ(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        return aismsa;
//    }
//
//
//    @Test
//    void contextLoads () throws Exception{
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        List<File> result = new ArrayList<>();
//        List<File> fs = new ArrayList<>();
//
//        fs.add(new File("E:\\track\\轨迹数据"));
//        fs.forEach(file -> {
//            File[] files = file.listFiles();
//            result.addAll(Arrays.asList(files));
//        });
//        for (int i=0;i<4;i++)result.remove(0);
//        CountDownLatch countDownLatch = new CountDownLatch(result.size());
//        AtomicInteger atomicInteger = new AtomicInteger();
//        result.forEach(data->{
//
//
//            executorService.execute(()->{
//                System.out.println("当前遍历文件名称: "+data.getName());
//                FileInputStream fileInputStream = null;
//                try {
//                    fileInputStream = new FileInputStream(data);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                Scanner sc = new Scanner(fileInputStream);
//                List<Track> trackList = new LinkedList<>();
//                Track track = null;
//                while (sc.hasNextLine()){
//                    String nextLine = sc.nextLine();
//
//                    try {
//                         track = JSON.parseObject(nextLine, Track.class);
//                        track.setLocation(track.getWD()+","+track.getJD());
//                        if(track.getSJ()!=null&&!track.getSJ().equals("")){
//                            track.setSJ(track.getSJ().replaceAll("/","-"));
//                        }
//                        trackList.add(track);
//                        if(trackList.size()>1000){
//                            elasticsearchRestTemplate.save(trackList);
//                            trackList = new LinkedList<>();
//                            System.out.println("当前遍历文件名称: "+data.getName()+ "遍历1000次数: " +atomicInteger.getAndIncrement());
//                        }
//                    } catch (Exception e) {
//                        System.out.println(track);
//                    }
//
//
//                }
//                if(trackList.size()>0){
//                    elasticsearchRestTemplate.save(trackList);
//                }
//                countDownLatch.countDown();
//            });
//
//        });
//        countDownLatch.await();
//
//
//    }
//
//}
