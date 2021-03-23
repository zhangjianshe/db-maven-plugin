package cn.mapway.tools.doc.tools;

import cn.mapway.tools.doc.model.DataItem;
import cn.mapway.tools.doc.model.Entry;
import cn.mapway.tools.doc.types.ApiType;
import cn.mapway.tools.doc.types.ApiTypes;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;

/**
 * ParameterTools
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
public class ParameterTools {
    /**
     * 处理接口的输入参数
     *
     * @param entry
     * @param md
     */
    public static void processInputParameter(Entry entry, MethodDeclaration md) {
        NodeList<Parameter> parameters = md.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            log.info("{} {}", md.getNameAsString(), parameter.getNameAsString());
        }
    }

    /**
     * 处理接口的输入参数
     *
     * @param entry
     * @param md
     */
    public static void processOutputParameter(Entry entry, MethodDeclaration md) {
        Type returnType = md.getType();
        ApiType apiType= ApiTypes.parse(returnType);

    }
}
