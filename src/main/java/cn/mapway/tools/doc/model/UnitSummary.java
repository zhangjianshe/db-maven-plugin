package cn.mapway.tools.doc.model;

import cn.mapway.tools.doc.tools.DocTools;
import lombok.Data;
import org.nutz.lang.Strings;

/**
 * UnitSummary
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class UnitSummary extends BaseData {
    String author;
    String basePath;
    String groupPath;

    public UnitSummary() {
        author = "";
        basePath = "";
    }

    /**
     * 集的作者
     *
     * @param author 作者
     */
    public void setAuthor(String author) {
        if (Strings.isNotBlank(author)) {
            this.author = author;
        }
    }

    /**
     * 得到组织路径
     *
     * @return {@link String}
     */
    public String getGroupPath() {
        return groupPath;
    }

    /**
     * 组群路径
     * 确保 grouppath 为 /*** 的形式 最后没有/号 开头有/
     * 如果为空 则为空字符串
     *
     * @param gp 全科医生
     */
    public void setGroupPath(String gp) {
        groupPath= DocTools.normalPath(gp);
    }
}
