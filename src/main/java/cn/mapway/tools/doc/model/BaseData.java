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

    public BaseData() {
        comments = new ArrayList<>();
        notes = new ArrayList<>();
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

}
