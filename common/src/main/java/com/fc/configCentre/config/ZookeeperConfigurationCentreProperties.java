package com.fc.configCentre.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 *  @author fangyuan
 */
@Getter
@Setter
public class ZookeeperConfigurationCentreProperties implements Serializable {

    private static final long serialVersionUID = -8927159079253907016L;

    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    private String jdbcUrl;

    private String username;

    private String password;

    private Hikari hikari;

    private String poolName = "MyHikariCP";


    @Getter
    @Setter
    public static class Hikari implements Serializable{

        private static final long serialVersionUID = 3318823623247574896L;
        //最小空闲连接数量
        private Integer minimumIdle = 1;

        //空闲连接存活最大时间，默认600000（10分钟）
        private Integer idleTimeout = 180000;

        //连接池最大连接数，默认是10
        private Integer maximumPoolSize = 10;

        //此属性控制从池返回的连接的默认自动提交行为,默认值：true
        private Boolean autoCommit = true;

        //此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
        private Integer maxLifetime = 1800000;

        //数据库连接超时时间,默认30秒，即30000
        private Integer connectionTimeout = 30000;

        //验证数据库查询时间
        private String  connectionTestQuery = "SELECT 1";

        //确定池中的连接是否处于只读模式
        private Boolean readOnly = false;

        //验证超时时间 默认5s
        private Integer validationTimeout = 5000;

    }
}
