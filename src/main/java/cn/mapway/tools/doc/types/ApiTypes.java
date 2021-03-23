package cn.mapway.tools.doc.types;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.utils.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * ApiTypes
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Slf4j
public class ApiTypes {
    private static final Map<String, ApiType> types = new HashMap<>();

    public static ApiType find(String typeName) {
        return types.get(typeName);
    }

    public static void put(String typeName, ApiType type) {
        types.put(typeName, type);
    }

    /**
     * 解析 Type
     *
     * @param returnType
     * @return
     */
    public static ApiType parse(Type returnType) {
        if (returnType.isClassOrInterfaceType()) {
            ClassOrInterfaceType classOrInterfaceType = returnType.asClassOrInterfaceType();
            if (classOrInterfaceType.isPrimitiveType()) {
                PrimitiveType primitiveType = classOrInterfaceType.toUnboxedType();
                String typeName = primitiveType.getType().toBoxedType().asString();
                ApiType apiType = new ApiType(typeName);
                return apiType;
            } else if (classOrInterfaceType.isBoxedType()) {
                ApiType apiType = new ApiType(classOrInterfaceType.asString());
                return apiType;
            }
        }
        ApiType apiType = types.get(returnType.asString());
        if (apiType == null) {
            apiType = new ApiType(returnType.asString());
            types.put(returnType.asString(), apiType);

            fromType(returnType);
        }
        return apiType;
    }

    /**
     * 解析 Type
     *
     * @param returnType
     * @param typeParametersMap
     * @return
     */
    public static ApiType parse(ResolvedReferenceType resolveType, List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {

        String typeName = resolveType.getQualifiedName();
        ApiType apiType = null;
        if (resolveType.isRawType()) {
            ResolvedPrimitiveType primitiveType = resolveType.asPrimitive();
            typeName = primitiveType.getBoxTypeQName();
            apiType = new ApiType(typeName);
        } else if (resolveType.isJavaLangObject()) {
            apiType = new ApiType(resolveType.getQualifiedName());
        } else if (typeName.equals("java.util.List")) {
            List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap1 = resolveType.getTypeParametersMap();
            ResolvedType t = null;
            for (Pair<ResolvedTypeParameterDeclaration, ResolvedType> p : typeParametersMap1) {
                t = findTypeVariable2(typeParametersMap, p.a);
            }
            if (t != null) {
                typeName = resolveType.getQualifiedName();
                apiType = new ApiType(typeName);
            } else {
                return new ApiType("List<Object>");
            }

        } else if (typeName.equals("java.lang.String")) {
            apiType = new ApiType("String");
        } else if (typeName.equals("java.lang.Integer")) {
            apiType = new ApiType("Integer");
        } else if (typeName.equals("java.lang.Long")) {
            apiType = new ApiType("Long");
        } else if (typeName.equals("java.lang.Double")) {
            apiType = new ApiType("Double");
        } else if (typeName.equals("java.lang.Byte")) {
            apiType = new ApiType("Integer");
        } else if (typeName.equals("java.lang.Short")) {
            apiType = new ApiType("Integer");
        }
        return apiType;
    }

    private static ResolvedType findTypeVariable2(List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap, ResolvedTypeParameterDeclaration a) {
        String name = a.getQualifiedName();
        for (Pair<ResolvedTypeParameterDeclaration, ResolvedType> p : typeParametersMap) {
            String t1 = p.a.getQualifiedName();
            if (t1.equals(name)) {
                return p.b;
            }
        }
        return null;
    }

    private static void fromType(Type type) {
        //一定会得到
        ApiType apiType = types.get(type.asString());

        ClassOrInterfaceType classOrInterfaceType;
        if (type.isClassOrInterfaceType()) {
            classOrInterfaceType = type.asClassOrInterfaceType();
            ResolvedReferenceType resolve = classOrInterfaceType.resolve();
            List<ResolvedFieldDeclaration> allFields = resolve.getAllFieldsVisibleToInheritors();
            List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = resolve.getTypeParametersMap();
            for (ResolvedFieldDeclaration f : allFields) {
                if (f.isField()) {
                    String name = f.getName();
                    if (f instanceof JavaParserFieldDeclaration) {
                        JavaParserFieldDeclaration jpfd = (JavaParserFieldDeclaration) f;
                        apiType.addField(name, parseJavaParserFieldDeclarition(jpfd, typeParametersMap));
                    }


                }
            }
        }
    }

    /**
     * 字段解析
     * 参数变量  T
     * 参数
     *
     * @param jpfd
     * @param typeParametersMap
     * @return
     */
    private static ApiType parseJavaParserFieldDeclarition(JavaParserFieldDeclaration jpfd, List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {


        ResolvedType type = jpfd.getType();

        if (type.isReferenceType()) {
            ResolvedReferenceType resolvedReferenceType = type.asReferenceType();
            return parse(resolvedReferenceType, typeParametersMap);
        } else if (type.isArray()) {

        } else if (type.isTypeVariable()) {
            ResolvedTypeVariable resolvedTypeVariable = type.asTypeVariable();
            ResolvedType rt = findTypeVariable(typeParametersMap, resolvedTypeVariable);
        } else if (type.isPrimitive()) {

        }


        return null;
    }

    /**
     * 解析
     *
     * @param resolvedTypeDeclaration
     * @param typeParametersMap
     * @return
     */
    private static ApiType parseResolved(ResolvedTypeDeclaration resolvedTypeDeclaration, List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {

        StringBuilder str = new StringBuilder();
        str.append(resolvedTypeDeclaration.getName());
        String collect = typeParametersMap.stream().map(t -> t.b.toString()).collect(joining(",", "<", ">"));
        str.append(collect);


        ApiType apiType = types.get(str.toString());
        if (apiType == null) {
            if (resolvedTypeDeclaration.isClass()) {
                ResolvedClassDeclaration resolvedClassDeclaration = resolvedTypeDeclaration.asClass();

            }
        }
        return apiType;
    }

    public static void report() {
        types.forEach((type, info) -> {
            log.info("=======  {}  ======", type);
            info.fields.forEach((m, t) -> {
                log.info("\t  {} {};", t, m);
            });
        });
    }

    private static ResolvedType findTypeVariable(List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap, ResolvedTypeVariable resolvedTypeVariable) {
        String t = resolvedTypeVariable.qualifiedName();

        for (Pair<ResolvedTypeParameterDeclaration, ResolvedType> p : typeParametersMap) {
            String t1 = p.a.getQualifiedName();
            if (t1.equals(t)) {
                return p.b;
            }
        }
        return null;
    }

    private static Map<ResolvedTypeParameterDeclaration, ResolvedType> fromList(List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> t) {
        Map<ResolvedTypeParameterDeclaration, ResolvedType> mapper = new HashMap<>();

        t.stream().forEach(p -> {
            mapper.put(p.a, p.b);
        });
        return mapper;
    }
}
