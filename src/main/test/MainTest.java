import cn.mapway.tools.db.DB2Code;
import cn.mapway.tools.db.IConfigure;
import org.nutz.lang.Lang;

import java.util.ArrayList;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {


        final DbParam p1 = DbParam.builder().url("jdbc:mysql://10.30.7.114:3306/jz_design?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false")
                .daoPackage("com.ziroom.code.test.dao")
                .entityPackage("com.ziroom.code.test.entity")
                .schema("jz_design")
                .user("dev_jiazhuang")
                .password("ziroomdb")
                .path("d:\\code\\").build();

        final DbParam p2 = DbParam.builder().url("jdbc:mysql://www.mapway.cn:3306/workday?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false")
                .daoPackage("com.ziroom.code.test.dao")
                .entityPackage("com.ziroom.code.test.entity")
                .schema("workday")
                .user("workday")
                .password("workday.cn")
                .path("d:\\code\\").build();

        final DbParam p3 = DbParam.builder().url("jdbc:oracle:thin:@10.16.26.21:1521:svdp")
                .daoPackage("com.ziroom.code.test.dao")
                .entityPackage("com.ziroom.code.test.entity")
                .schema("HLASSET")
                .user("HLASSET")
                .password("oracle")
                .path("d:\\code\\").build();


        final DbParam dp = p1;
        IConfigure config = new IConfigure() {

            @Override
            public Boolean withStaticField() {
                return false;
            }

            @Override
            public List<String> includes() {
                return Lang.array2list(Lang.array("hddp_design_approval","hddp_technology_test", "sys_dictionary", "BZ_ROOMCONFIG_DETAIL", "BZ_ATTACHMENT_NEW"));
            }

            @Override
            public List<String> excludes() {
                return new ArrayList<>();
            }

            @Override
            public String getDriver() {
                return "";
            }

            @Override
            public String getJdbcUrl() {
                return dp.getUrl();
            }


            @Override
            public String getSchema() {
                return dp.getSchema();
            }

            @Override
            public String getUser() {
                return dp.getUser();
            }

            @Override
            public String getPassword() {
                return dp.getPassword();
            }

            @Override
            public String entityPackage() {
                return dp.getEntityPackage();
            }

            @Override
            public String daoPackage() {
                return dp.getDaoPackage();
            }

            @Override
            public String daoPath() {
                return "d:\\code\\";
            }

            @Override
            public String entityPath() {
                return "d:\\code\\";
            }

            @Override
            public String getMapper() {
                return null;
            }

            @Override
            public String getUseFieldStyle() {
                return null;
            }

            @Override
            public String getUseGwt() {
                return null;
            }

            @Override
            public String getUseDocument() {
                return null;
            }

            @Override
            public String getUseNutz() {
                return null;
            }

            @Override
            public String getUseFieldIndex() {
                return null;
            }

            @Override
            public String author() {
                return "zhangjianshe";
            }

            @Override
            public String dateFormat() {
                return "yyyy-MM-dd HH:mm:ss";
            }

            @Override
            public Boolean lombok() {
                return true;
            }

            @Override
            public Boolean overrideDao() {
                return true;
            }

            @Override
            public Boolean withSwagger() {
                return true;
            }

            @Override
            public String dbSourceName() {
                return "com.zrpd.exe.DataSourceConfig.DB_ORACLE_MASTER";
            }

            @Override
            public String mapperPath() {
                return "d:\\code\\com\\ziroom\\hddp\\resource";
            }

        };
        DB2Code app = new DB2Code(config);
        app.run();
    }
}
