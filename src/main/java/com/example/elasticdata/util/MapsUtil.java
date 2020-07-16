package com.example.elasticdata.util;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>elasticdata</h3>
 * <p>生成切割后的地图方格</p>
 *
 * @author : liliguang
 * @date : 2020-07-04 13:17
 **/
public class MapsUtil {

    /**
     * 将原本广东省的坐标区域按照level字段进行切割
     *
     * @param sjd   开始经度 (对角线)
     * @param swd   开始纬度 (对角线)
     * @param ejd   结束经度 (对角线)
     * @param ewd   结束纬度 (对角线)
     * @param level 切割级别 例如2 则切割成2*2份数据
     * @return 切割后的坐标集合
     */
    public static List<double[]> maps(double sjd, double swd, double ejd, double ewd, int level) {
        List<double[]> maps = new ArrayList<>();
        double avgjd = (ejd - sjd) / level;
        double avgwd = (ewd - swd) / level;
        double tempsjd, tempejd, tempswd, tempewd;
        for (int i = 0; i < level; i++) {
            tempsjd = sjd + avgjd * i;
            tempejd = sjd + avgjd * (i + 1);
            for (int j = 0; j < level; j++) {
                tempswd = swd + avgwd * j;
                tempewd = swd + avgwd * (j + 1);
                maps.add(new double[]{tempsjd, tempswd, tempejd, tempewd});
            }
        }
        return maps;
    }


}
