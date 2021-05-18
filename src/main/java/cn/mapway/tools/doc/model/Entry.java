package cn.mapway.tools.doc.model;

import lombok.Data;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry
 * 接口实例
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class Entry extends BaseData {
    Boolean isValid;
    String path;
    String author;
    String group;
    List<String> methods;
    List<String> tags;
    Group parent;

    List<DataItem> ins;
    PathData pathData;
    QueryData queryData;
    DataItem out;


    /**
     * 条目
     */
    public Entry() {
        isValid = true;
        methods = new ArrayList<>();
        tags = new ArrayList<>();
        pathData = new PathData();
        queryData = new QueryData();
        ins = new ArrayList<>();
    }

    /**
     * 设置父
     *
     * @param g g
     */
    public void setParent(Group g) {
        this.parent = g;
    }

    /**
     * 添加标签
     *
     * @param tag 标签
     */
    public void addTag(String tag) {
        if (Strings.isNotBlank(tag)) {
            tags.add(tag.trim());
        }
    }

    /**
     * 获得集团
     *
     * @return {@link String}
     */
    public String getGroup() {
        return Strings.trim(this.group);
    }

    /**
     * 组群
     *
     * @param group 集团
     */
    public void setGroup(String group) {
        if (Strings.isNotBlank(group)) {
            this.group = group;
        }
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
     * 字符串
     *
     * @return {@link String}
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(group).append("\t")
                .append(Lang.concat(methods.toArray())).append("\t")
                .append(path).append("\t")
                .append(Lang.concat(comments.toArray())).append("\t")
                .append(author).append("\t");
        sb.append("\r\n");
        return sb.toString();
    }


    /**
     * 所有的父母
     *
     * @return data
     */
    public List<Group> allParents() {
        List<Group> groups = new ArrayList<>();
        Group p = this.getParent();
        while (p != null) {
            groups.add(0, p);
            p = p.getParent();
        }
        return groups;
    }
}
