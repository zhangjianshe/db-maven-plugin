package cn.mapway.tools.db;

import lombok.Data;

import java.util.List;

/**
 * ConfigImpl
 *
 * @author zhangjianshe@gmail.com
 */

@Data
public class ConfigImpl implements IConfigure {

    List<String> includes;

    @Override
    public List<String> includes() {
        return includes;
    }
    List<String> excludes;
    @Override
    public List<String> excludes() {
        return excludes;
    }

    String driver;
    @Override
    public String getDriver() {
        return driver;
    }
    String jdbcUrl;
    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    String schema;
    @Override
    public String getSchema() {
        return schema;
    }

    String user;
    @Override
    public String getUser() {
        return user;
    }
    String password;
    @Override
    public String getPassword() {
        return password;
    }

    String entityPackage;
    @Override
    public String entityPackage() {
        return entityPackage;
    }

    String daoPackage;
    @Override
    public String daoPackage() {
        return daoPackage;
    }
    String daoPath;
    @Override
    public String daoPath() {
        return daoPath;
    }

    String entityPath;
    @Override
    public String entityPath() {
        return entityPath;
    }

    String mapper;
    @Override
    public String getMapper() {
        return mapper;
    }

    String useFieldStyle;
    @Override
    public String getUseFieldStyle() {
        return useFieldStyle;
    }


    @Override
    public String getUseGwt() {
        return "";
    }

    @Override
    public String getUseDocument() {
        return "";
    }

    @Override
    public String getUseNutz() {
        return "";
    }

    @Override
    public String getUseFieldIndex() {
        return "";
    }

    String author;
    @Override
    public String author() {
        return author;
    }

    String dateFormat;
    @Override
    public String dateFormat() {
        return dateFormat;
    }

    Boolean lombok;
    @Override
    public Boolean lombok() {
        return lombok;
    }

    Boolean withStaticField;
    @Override
    public Boolean withStaticField() {
        return withStaticField;
    }

    Boolean withSwagger;
    @Override
    public Boolean withSwagger() {
        return withSwagger;
    }

    Boolean overrideDao;
    @Override
    public Boolean overrideDao() {
        return overrideDao;
    }
    String dbSourceName;
    @Override
    public String dbSourceName() {
        return dbSourceName;
    }

    String mapperPath;
    @Override
    public String mapperPath() {
        return mapperPath;
    }

    String dbContext;
    @Override
    public String dbContext() {
        return dbContext;
    }
}
