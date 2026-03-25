package com.sodimac.facturacion.pool;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sodimac.facturacion.util.UtilsPoolDataSource;

@Component
public class DataSourceBct {

    private static DataSource ds;

    private static String activeProfile;

    @Value("${spring.profiles.active:dev}")
    public void setActiveProfile(String profile) {
        activeProfile = profile;
    }

    public DataSource getDataSource() {
        if (ds == null) {
            String propertiesFile = "databaseBct-" + activeProfile + ".properties";
            ds = UtilsPoolDataSource.buildPoolDataSource(propertiesFile);
        }
        return ds;
    }

}
