package cn.mapway.tools.db;

import java.util.List;

/**
 * The interface Configure.
 *
 * @author zhangjianshe
 */
public interface IConfigure {

    /**
     * 输出字段名称的静态常量
     *
     * @return true
     */
    Boolean exportFieldName();

    /**
     * 需要生成的列表
     *
     * @return list
     */
    List<String> includes();

    /**
     * 需要生成的列表
     *
     * @return list
     */
    List<String> excludes();


    /**
     * Gets driver.
     *
     * @return the driver
     */
    String getDriver();

    /**
     * ��ݿ�����URL.
     *
     * @return the jdbcurl
     */
    String getJdbcUrl();


    /**
     * Gets the schema.
     *
     * @return the schema
     */
    String getSchema();

    /**
     * Gets the user.
     *
     * @return the user
     */
    String getUser();

    /**
     * Gets the password.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Gets the package.
     *
     * @return the package
     */
    String entityPackage();

    /**
     * DAO Package
     *
     * @return daopackage
     */
    String daoPackage();


    /**
     * DAO Mapper路径
     *
     * @return
     */
    String daoPath();

    /**
     * Entity Mapper路径
     *
     * @return
     */
    String entityPath();

    /**
     * Gets mapper.
     *
     * @return the mapper
     */
    String getMapper();

    /**
     * 是否使用字段全部大写
     *
     * @return use field style
     */
    String getUseFieldStyle();

    /**
     * 是否关联GWT
     *
     * @return use gwt
     */
    String getUseGwt();

    /**
     * 是否关联注解
     *
     * @return use document
     */
    String getUseDocument();

    /**
     * 是否关联数据Nutz注解
     *
     * @return use nutz
     */
    String getUseNutz();

    /**
     * 是否使用字段索引
     *
     * @return use field index
     */
    String getUseFieldIndex();

    /**
     * 返回作者
     *
     * @return author
     */
    String author();

    /**
     * 日期格式
     *
     * @return dateformat
     */
    String dateFormat();

    /**
     * 是否启用lombok
     *
     * @return enable lombok
     */
    Boolean lombok();
}
