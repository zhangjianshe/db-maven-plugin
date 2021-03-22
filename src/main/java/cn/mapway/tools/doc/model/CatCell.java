package cn.mapway.tools.doc.model;

import lombok.Data;

/**
 * CatCell
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class CatCell {
    int rowspan = 1;
    int colspan = 1;
    Group group;
}
