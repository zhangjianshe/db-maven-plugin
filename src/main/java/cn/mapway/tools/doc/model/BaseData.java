package cn.mapway.tools.doc.model;

import lombok.Data;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseData
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class BaseData {

    List<String> comments;
    List<String> notes;

    /**
     * 基本数据
     */
    public BaseData() {
        comments = new ArrayList<>();
        notes = new ArrayList<>();
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

}
