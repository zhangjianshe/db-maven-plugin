package cn.mapway.tools.doc.tools;

import cn.mapway.tools.doc.model.Doc;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;

/**
 * DocTools
 * 工具类
 *
 * @author zhangjianshe@gmail.com
 */
public class DocTools {


    public static String normalPath(String gp) {
        if (Strings.isBlank(gp)) {

            return "";
        }
        gp = Strings.trim(gp);
        while (gp.startsWith("/")) {
            gp = gp.substring(1);
        }
        if (gp.length() == 0) {
            return "";
        }
        while (gp.endsWith("/")) {
            gp = gp.substring(0, gp.length() - 1);
        }
        if (gp.length() == 0) {

            return "";
        }
        return "/" + gp;
    }
}
