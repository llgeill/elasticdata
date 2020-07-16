package com.example.elasticdata.entity.other;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Objects;

/**
 * <h3>elasticdata</h3>
 * <p>港口</p>
 *
 * @author : liliguang
 * @date : 2020-06-18 09:18
 **/
@Data
@Document(indexName = "port")
//@Document(indexName = "coordinate")
public class Port {

    public Port(String chName, String location) {
        this.chName = chName;
        this.location = location;
    }

    public Port() {
    }

    @Field
    private String id;
    @Field
    private String pyName;
    @Field
    private String chName;
    @Field
    private String location;
    @Field
    private String boundary;




    /**
     * 查询两点的距离
     * @param point
     * @return
     */
    public double distance(Port point){
        if(point==null){
            System.out.println(1);
        }
        String[] thisPos = location.split(",");
        double p1_lat = Math.toRadians(Double.parseDouble(thisPos[1]));
        double p1_lon = Math.toRadians(Double.parseDouble(thisPos[0]));
        String[] split = point.getLocation().split(",");
        double p2_lat = Math.toRadians(Double.parseDouble(split[1]));
        double p2_lon = Math.toRadians(Double.parseDouble(split[0]));

        double numerator = Math.sqrt(
                Math.pow(Math.cos(p2_lat) * Math.sin(p2_lon - p1_lon), 2) +
                        Math.pow(
                                Math.cos(p1_lat) * Math.sin(p2_lat) -
                                        Math.sin(p1_lat) * Math.cos(p2_lat) *
                                                Math.cos(p2_lon - p1_lon), 2));

        double denominator =  Math.sin(p1_lat) * Math.sin(p2_lat) +
                Math.cos(p1_lat) * Math.cos(p2_lat) *
                        Math.cos(p2_lon - p1_lon);

        return Math.atan2(numerator, denominator) * 6372800;
    }

    public Port clone(Integer index){
       Port port = new Port();
       port.setId(this.id+"-"+index);
       port.setPyName(this.pyName+"-"+index);
       port.setBoundary(this.boundary);
       port.setLocation(this.location);
       port.setChName(this.chName+"-"+index);
       return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Port port = (Port) o;
        return Objects.equals(location, port.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return "Port{" +
                "id='" + id + '\'' +
                ", pyName='" + pyName + '\'' +
                ", chName='" + chName + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
