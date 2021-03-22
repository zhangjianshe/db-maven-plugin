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

    public SimpleData() {
        init();
    }

    public SimpleData(DataType dt, String name, String example, String comment) {
        init();
        this.dataType = dt;
        this.name = name;
        this.example = example;
        addComment(comment);
    }

    private void init() {
        dataType = DataType.StringType;
        example = "";
        name = "";
        comments = new ArrayList<>();
        notes = new ArrayList<>();
        mandatory = true;
    }

    public void addComment(String comment) {
        if (Strings.isNotBlank(comment)) {
            comments.add(comment);
        }
    }

    public void addNote(String note) {
        if (Strings.isNotBlank(note)) {
            notes.add(note);
        }
    }

    public String toComment() {
        return Lang.concat(comments.toArray()).toString();
    }

    public String toNotes() {
        return Lang.concat(notes.toArray()).toString();

    }

}
