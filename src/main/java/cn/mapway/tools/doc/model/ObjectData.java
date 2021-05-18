package cn.mapway.tools.doc.model;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * FieldItem
 * 用来表示一个字段信息
 * 简单的数据类型
 * Int
 * Long
 * String
 * Boolean
 * Float
 * Double
 * BigDecimal
 * Date
 * Array
 * List
 * Map
 * File
 * <p>
 * 复杂的数据类型 由以上简单数据类型组合而成,当展开为接口形式时,不可能再有 模板参数类型
 * 其他的数据类型为 ObjectType
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class ObjectData extends DataItem {
    List<DataItem> fields;

    /**
     * 对象数据
     */
    public ObjectData() {
        fields = new ArrayList<>();
    }

    /**
     * 添加字段
     *
     * @param dt dt
     */
    public void addField(DataItem dt) {
        fields.add(dt);
    }
}
