package cn.mapway.plugin.db;

import cn.mapway.tools.db.DB2Code;
import cn.mapway.tools.db.IConfigure;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 生成数据库访问层DB for MyBatisPlus.
 */
@Mojo(name = "mybatis", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Mybatis extends AbstractMojo {

    private final static Logger logger = Logger.getLogger(Mybatis.class.getName());


    /**
     * The path.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "daoPath", required = true)
    private String daoPath;

    /**
     * The path.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "entityPath", required = true)
    private String entityPath;

    /**
     * The driver.
     */
    @Parameter(defaultValue = "com.mysql.jdbc.Driver", property = "driver", required = true)
    private String driver;

    /**
     * The jdbcurl.
     */
    @Parameter(defaultValue = "", property = "jdbcUrl", required = true)
    private String jdbcUrl;

    /**
     * The package name.
     */
    @Parameter(defaultValue = "test", property = "entityPackage", required = true)
    private String entityPackage;

    /**
     * The package name.
     */
    @Parameter(defaultValue = "test", property = "daoPackage", required = true)
    private String daoPackage;

    /**
     * The user.
     */
    @Parameter(defaultValue = "root", property = "user", required = true)
    private String user;

    /**
     * The pwd.
     */
    @Parameter(defaultValue = "pwd", property = "pwd", required = true)
    private String pwd;

    /**
     * The schema.
     */
    @Parameter(defaultValue = "schema", property = "schema", required = true)
    private String schema;

    /**
     * The nutz.
     */
    @Parameter(defaultValue = "1", property = "useFieldIndex", required = true)
    private String useFieldIndex;

    /**
     * The nutz.
     */
    @Parameter(defaultValue = "1", property = "useNutz", required = true)
    private String useNutz;


    /**
     * The nutz.
     */
    @Parameter(defaultValue = "0", property = "mapper", required = false)
    private String mapper;

    /**
     * 是否使用全大写字段 UPPER_CASE LOWER_CASE CAMEL
     */
    @Parameter(defaultValue = "CAMEL", property = "useFieldStyle", required = false)
    private String useFieldStyle;

    /**
     * 是否关联GWT
     */
    @Parameter(defaultValue = "false", property = "useGwt", required = false)
    private Boolean useGwt;

    /**
     * 是否关联文档
     */
    @Parameter(defaultValue = "1", property = "useDocument", required = false)
    private String useDocument;

    /**
     * 作者
     */
    @Parameter(defaultValue = "zhangjs2@ziroom.com", property = "author", required = false)
    private String author;

    /**
     * 日期格式
     */
    @Parameter(defaultValue = "", property = "dateFormat", required = false)
    private String dateFormat;

    /**
     * lombok
     */
    @Parameter(defaultValue = "false", property = "lombok", required = false)
    private Boolean lombok;

    /**
     * 包括
     */
    @Parameter(defaultValue = "", property = "includes", required = false)
    private String includes;

    /**
     * 包含的表明，用逗号或者分号分隔
     */
    @Parameter(defaultValue = "", property = "excludes", required = false)
    private String excludes;

    /**
     * 是否输出字段的静态名称,缺省生成
     */
    @Parameter(defaultValue = "false", property = "withStaticField", required = false)
    private Boolean withStaticField;

    /**
     * 是否输出Swagger注解,缺省false
     */
    @Parameter(defaultValue = "false", property = "withSwagger", required = false)
    private Boolean withSwagger;

    /**
     * 是否覆盖DAO层,缺省是不覆盖 false
     */
    @Parameter(defaultValue = "false", property = "overrideDao", required = false)
    private Boolean overrideDao;

    /**
     * DB SOURCE 名称
     */
    @Parameter(defaultValue = "", property = "dbSourceName", required = false)
    private String dbSourceName;

    /**
     * DB Context
     */
    @Parameter(defaultValue = "", property = "dbContext", required = false)
    private String dbContext;

    /**
     * XML mapper的路径，缺省是资源路径
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "mapperPath", required = false)
    private String mapperPath;

    /**
     * 解析行
     *
     * @param data 数据
     * @return {@link List<String>}
     */
    private static List<String> parseLines(String data) {
        if (Strings.isBlank(data)) {
            return new ArrayList<String>();
        }
        data = Strings.trim(data);
        String[] items = Strings.split(data, false, ',', ';');
        ArrayList<String> list = new ArrayList<>(30);
        for (String item : items) {
            item = Strings.trim(item);
            if (!Strings.isBlank(item)) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 执行
     *
     * @throws MojoExecutionException 魔力执行异常
     */
    public void execute()
            throws MojoExecutionException {

        IConfigure configure = new IConfigure() {


            @Override
            public String getUser() {
                return user;
            }

            @Override
            public String getSchema() {
                return schema;
            }


            @Override
            public String getPassword() {
                return pwd;
            }

            @Override
            public String entityPackage() {
                return entityPackage;
            }

            @Override
            public String daoPackage() {
                return daoPackage;
            }

            @Override
            public String daoPath() {
                return daoPath;
            }

            @Override
            public String entityPath() {
                return entityPath;
            }


            @Override
            public String getJdbcUrl() {
                return jdbcUrl;
            }


            @Override
            public List<String> includes() {
                List<String> ins = parseLines(includes);
                logger.info("要处理的数据库表");
                for (String tableName : ins) {
                    logger.info(tableName);
                }
                return ins;
            }

            @Override
            public List<String> excludes() {
                List<String> exs = parseLines(excludes);
                logger.info("要排除的数据库表");
                for (String tableName : exs) {
                    logger.info(tableName);
                }
                return exs;
            }

            @Override
            public String getDriver() {
                return driver;
            }


            @Override
            public String getMapper() {

                return mapper;
            }

            public String getUseFieldStyle() {
                return useFieldStyle;
            }

            @Override
            public Boolean getUseGwt() {
                return useGwt;
            }

            @Override
            public String getUseDocument() {
                return useDocument;
            }

            @Override
            public String getUseNutz() {
                return useNutz;
            }

            @Override
            public String getUseFieldIndex() {
                return useFieldIndex;
            }

            @Override
            public String author() {
                return Strings.isBlank(author) ? "zhangjs2@ziroom.com" : author;
            }

            @Override
            public String dateFormat() {
                return dateFormat;
            }

            @Override
            public Boolean lombok() {
                return lombok;
            }

            @Override
            public Boolean withStaticField() {
                return withStaticField;
            }

            @Override
            public Boolean overrideDao() {
                return overrideDao;
            }

            @Override
            public String dbSourceName() {
                return dbSourceName;
            }

            @Override
            public Boolean withSwagger() {
                return withSwagger;
            }


            @Override
            public String mapperPath() {
                return mapperPath;
            }

            @Override
            public String dbContext() {
                return dbContext;
            }
        };

        DB2Code app = new DB2Code(configure);
        app.run();

    }
}
