import cn.mapway.plugin.doc.DocConfiguration;
import cn.mapway.tools.doc.DocGen;

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
        docConfiguration.setBasePath("D:\\soft\\zrpd\\zrpd-web\\src\\main\\java");
        docConfiguration.setLibPath("");
        docConfiguration.setScans(Arrays.asList("com.ziroom.zrpd.design","com.ziroom.zrpd.api.designs"));

        DocGen gen = new DocGen(docConfiguration);
        gen.run();
    }
}
