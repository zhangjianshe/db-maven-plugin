package cn.mapway.plugin.db;

import cn.mapway.tools.db.IConfigure;
import cn.mapway.tools.db.NutzImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Nuts
 *
 * @author zhangjianshe@gmail.com
 */
@Mojo(name = "nutz", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Slf4j
public class Nuts extends AbstractMojo {
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
     * 作者
     */
    @Parameter(defaultValue = "zhangjs2@ziroom.com", property = "author", required = false)
    private String author;

    /**
     * 包含的表明，用逗号或者分号分隔
     */
    @Parameter(defaultValue = "", property = "includes", required = false)
    private String includes;

    /**
     * 包含的表明，用逗号或者分号分隔
     */
    @Parameter(defaultValue = "", property = "excludes", required = false)
    private String excludes;

    /**
     * The package name.
     */
    @Parameter(defaultValue = "test", property = "entityPackage", required = true)
    private String entityPackage;

    /**
     * 是否输出Swagger注解,缺省false
     */
    @Parameter(defaultValue = "false", property = "withSwagger", required = false)
    private Boolean withSwagger;
    /**
     * 是否输出Swagger注解,缺省false
     */
    @Parameter(defaultValue = "false", property = "withStaticField", required = false)
    private Boolean withStaticField;

    /**
     * 是否使用lombok
     */
    @Parameter(defaultValue = "false", property = "lombok", required = false)
    private Boolean lombok;

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
     * @throws MojoFailureException   魔力衰竭例外
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
                return "daoPackage";
            }

            @Override
            public String daoPath() {
                return "daoPath";
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
                log.info("要处理的数据库表");
                for (String tableName : ins) {
                    log.info(tableName);
                }
                return ins;
            }

            @Override
            public List<String> excludes() {
                List<String> exs = parseLines(excludes);
                log.info("要排除的数据库表");
                for (String tableName : exs) {
                    log.info(tableName);
                }
                return exs;
            }

            @Override
            public String getDriver() {
                return driver;
            }


            @Override
            public String getMapper() {

                return "mapper";
            }

            public String getUseFieldStyle() {
                return "useFieldStyle";
            }

            @Override
            public String getUseGwt() {
                return "useGwt";
            }

            @Override
            public String getUseDocument() {
                return "useDocument";
            }

            @Override
            public String getUseNutz() {
                return "useNutz";
            }

            @Override
            public String getUseFieldIndex() {
                return "useFieldIndex";
            }

            @Override
            public String author() {
                return Strings.isBlank(author) ? "zhangjs2@ziroom.com" : author;
            }

            @Override
            public String dateFormat() {
                return "dateFormat";
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
                return false;
            }

            @Override
            public String dbSourceName() {
                return "";
            }

            @Override
            public Boolean withSwagger() {
                return withSwagger;
            }


            @Override
            public String mapperPath() {
                return "mapperPath";
            }

            @Override
            public String dbContext() {
                return "dbContext";
            }
        };
        NutzImpl impl = new NutzImpl(configure);
        impl.run();
    }
}
