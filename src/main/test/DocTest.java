import cn.mapway.plugin.doc.DocConfiguration;
import cn.mapway.tools.doc.DocGen;
import cn.mapway.tools.doc.types.ApiTypes;

import java.util.Arrays;

/**
 * DocTest
 *
 * @author zhangjianshe@gmail.com
 */
public class DocTest {
    public static void main(String[] args) {
        DocConfiguration docConfiguration = new DocConfiguration();
        docConfiguration.setFormat("html");
        docConfiguration.setOutput("d:\\data\\1.html");
        docConfiguration.setBasePath("e:\\dev\\db-maven-plugin\\src\\main\\test");
        docConfiguration.setLibPath("");
        docConfiguration.setScans(Arrays.asList("controller", "com.ziroom.zrpd.api.designs"));

        DocGen gen = new DocGen(docConfiguration);
        gen.run();

        ApiTypes.report();
    }
}
