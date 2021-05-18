package cn.mapway.tools.doc.model;

import lombok.Data;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleData
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class SimpleData extends DataItem {

    /**
     * 数据类型
     */
    DataType dataType;
    String example;
    String name;
    List<String> comments;
    List<String> notes;
    /**
     * 强制
     */
    Boolean mandatory;

    /**
     * 简单的数据
     */
    public SimpleData() {
        init();
    }

    /**
     * 简单的数据
     *
     * @param dt      dt
     * @param name    的名字
     * @param example 例子
     * @param comment 评论
     */
    public SimpleData(DataType dt, String name, String example, String comment) {
        init();
        this.dataType = dt;
        this.name = name;
        this.example = example;
        addComment(comment);
    }

    /**
     * 初始化
     */
    private void init() {
        dataType = DataType.StringType;
        example = "";
        name = "";
        comments = new ArrayList<>();
        notes = new ArrayList<>();
        mandatory = true;
    }

    /**
     * 添加评论
     *
     * @param comment 评论
     */
    public void addComment(String comment) {
        if (Strings.isNotBlank(comment)) {
            comments.add(comment);
        }
    }

    /**
     * 添加注
     *
     * @param note 请注意
     */
    public void addNote(String note) {
        if (Strings.isNotBlank(note)) {
            notes.add(note);
        }
    }

    /**
     * 发表评论
     *
     * @return {@link String}
     */
    public String toComment() {
        return Lang.concat(comments.toArray()).toString();
    }

    /**
     * 对笔记
     *
     * @return {@link String}
     */
    public String toNotes() {
        return Lang.concat(notes.toArray()).toString();

    }

}
