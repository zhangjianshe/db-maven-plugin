package cn.mapway.tools.doc;

import cn.mapway.plugin.doc.DocConfiguration;
import cn.mapway.tools.doc.model.Doc;
import cn.mapway.tools.doc.model.Entry;
import cn.mapway.tools.doc.model.UnitSummary;
import cn.mapway.tools.doc.tools.DocTools;
import cn.mapway.tools.doc.tools.ParameterTools;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * DocGen
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
public class DocGen {
    private DocConfiguration docConfiguration;

    public DocGen(DocConfiguration docConfiguration) {
        this.docConfiguration = docConfiguration;
    }

    private String packageToPath(String pack) {
        return pack.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
    }

    public void run() {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File(docConfiguration.getBasePath())));
        try {
            typeSolver.add(new JarTypeSolver(new File("D:\\repository\\com\\ziroom\\hddp\\design-client\\1.1.0\\design-client-1.1.0.jar")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);

        List<String> packages = docConfiguration.getScans();
        if (Lang.isEmpty(packages)) {
            log.warn("没有配置扫描的包");
            return;
        }
        List<Entry> entries = new ArrayList<>();
        for (String packageName : packages) {
            if (Strings.isBlank(packageName)) {
                continue;
            }
            String filePath = docConfiguration.getBasePath() + File.separator + packageToPath(packageName);
            log.info("处理包 {} @ {}", packageName, filePath);
            List<File> lsFiles = listAllFiles(filePath);
            for (File f : lsFiles) {
                List<Entry> entries1 = parseFile(f);
                if (Lang.isNotEmpty(entries1)) {
                    entries.addAll(entries1);
                }
            }
        }

        Doc doc = Doc.parseEntries(entries);

        log.info("write to {}", docConfiguration.getOutput());
        Files.write(docConfiguration.getOutput(), doc.toHtml());
    }


    private List<File> listAllFiles(String filePath) {
        File[] dirs = Files.scanDirs(new File(filePath));
        ArrayList list = new ArrayList<File>();
        for (File d : dirs) {
            File[] lsFile = Files.files(d, "java");
            for (int i = 0; i < lsFile.length; i++) {
                list.add(lsFile[i]);
            }
        }
        return list;
    }

    private List<Entry> parseFile(File file) {
        try {
            CompilationUnit unit = StaticJavaParser.parse(file);
            NodeList<TypeDeclaration<?>> types = unit.getTypes();
            for (int i = 0; i < types.size(); i++) {
                TypeDeclaration<?> typeDeclaration = types.get(i);
                Optional<AnnotationExpr> controller = typeDeclaration.getAnnotationByClass(Controller.class);
                Optional<AnnotationExpr> restController = typeDeclaration.getAnnotationByClass(RestController.class);

                if (controller.isPresent()) {
                    return processController(typeDeclaration);
                } else if (restController.isPresent()) {
                    return processRestController(typeDeclaration);
                } else {
                    log.warn("{} is not a controller", typeDeclaration.getName());
                    return new ArrayList<>();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<Entry> processRestController(TypeDeclaration<?> typeDeclaration) {
        log.info("处理控制器 {}", typeDeclaration.getName());
        Optional<AnnotationExpr> restController = typeDeclaration.getAnnotationByClass(RestController.class);
        Optional<AnnotationExpr> requestMapping = typeDeclaration.getAnnotationByClass(RequestMapping.class);
        return processApi(typeDeclaration, requestMapping);
    }

    private List<Entry> processController(TypeDeclaration<?> typeDeclaration) {
        log.info("处理控制器 {}", typeDeclaration.getName());
        Optional<AnnotationExpr> controller = typeDeclaration.getAnnotationByClass(Controller.class);
        Optional<AnnotationExpr> requestMapping = typeDeclaration.getAnnotationByClass(RequestMapping.class);
        return processApi(typeDeclaration, requestMapping);
    }

    private List<Entry> processApi(TypeDeclaration<?> typeDeclaration, Optional<AnnotationExpr> requestMapping) {
        final UnitSummary unitSummary = parseSummary(typeDeclaration, requestMapping);
        log.info(Json.toJson(unitSummary));
        List<Entry> entries = typeDeclaration.getMethods().stream().filter(md ->
                isApi(md)
        ).map(md -> parseEntry(unitSummary, md)).filter(e -> e.getIsValid()).collect(Collectors.toList());

        return entries;
    }

    private Boolean isApi(MethodDeclaration md) {
        if (!md.isPublic()) {
            return false;
        }
        Optional<AnnotationExpr> req = md.getAnnotationByClass(RequestMapping.class);
        Optional<AnnotationExpr> get = md.getAnnotationByClass(GetMapping.class);
        Optional<AnnotationExpr> post = md.getAnnotationByClass(PostMapping.class);

        return req.isPresent() || get.isPresent() || post.isPresent();
    }

    private Entry parseEntry(final UnitSummary unitSummary, MethodDeclaration md) {
        final Entry entry = new Entry();

        //处理Path
        Optional<AnnotationExpr> req = md.getAnnotationByClass(RequestMapping.class);
        req.ifPresent(requestMapping -> {
            List<String> paths = annoKeyListValue(requestMapping, "value", "path");
            List<String> methods = annoKeyListValue(requestMapping, "method");
            if (methods.size() == 0) {
                methods.add("GET");
            }
            entry.getMethods().addAll(methods);
            if (paths.size() > 0) {
                entry.setPath(concatPath(unitSummary.getBasePath(), paths.get(0)));
            } else {
                log.warn("{} is not config path", md.toString());
                entry.setIsValid(false);
            }
        });

        //处理ApiOperation注解
        Optional<AnnotationExpr> apiOperation = md.getAnnotationByClass(ApiOperation.class);
        apiOperation.ifPresent(t -> {
            entry.addComment(annoKeyValue(t, "value"));
            entry.addNote(annoKeyValue(t, "notes"));
            List<String> tags = annoKeyListValue(t, "tags");
            if (tags.size() > 0) {
                //处理 group path
                entry.setGroup(unitSummary.getGroupPath() + DocTools.normalPath(tags.get(0)));
                for (int i = 1; i < tags.size(); i++) {
                    entry.addTag(tags.get(i));
                }
            }
        });

        //处理JavaDoc
        Optional<Javadoc> javadoc = md.getJavadoc();
        if (javadoc.isPresent()) {
            entry.addComment(javadoc.get().getDescription().toText());
            entry.setAuthor(findTag(javadoc.get(), "author").orElse(unitSummary.getAuthor()));
        } else {
            entry.setAuthor(unitSummary.getAuthor());
        }

        if (Lang.isEmpty(entry.getComments())) {
            entry.addComment(md.getNameAsString());
        }

        //处理输入参数
        ParameterTools.processInputParameter(entry, md);

        //处理输出参数
        ParameterTools.processOutputParameter(entry, md);
        //处理QueryForm参数

        //处理路径参数

        return entry;
    }

    private String concatPath(String path0, String path1) {
        if (Strings.isBlank(path0)) {
            return path1;
        }
        String temp = Strings.trim(path0);
        if (!temp.endsWith("/")) {
            temp = temp + "/";
        }
        String temp2 = Strings.trim(path1);
        while (temp2.startsWith("/")) {
            temp2 = temp2.substring(1);
        }
        return temp + temp2;
    }

    private List<String> annoKeyListValue(AnnotationExpr anno, String... keys) {
        if (anno == null || Lang.isEmpty(keys)) {
            return new ArrayList<>();
        }
        List<Node> childNodes = anno.getChildNodes();
        final List<String> values = new ArrayList<>();
        for (Node n : childNodes) {
            if (n instanceof MemberValuePair) {
                MemberValuePair pair = (MemberValuePair) n;
                String key = pair.getNameAsString();
                if (Lang.contains(Lang.array(keys), key)) {
                    Expression exp = pair.getValue();
                    exp.ifStringLiteralExpr(t -> {
                        values.add(t.asString());
                    });
                    exp.ifArrayInitializerExpr(t -> extractList(t, values));
                    exp.ifFieldAccessExpr(t -> values.add(t.getNameAsString()));
                    log.info(exp.getClass().getName());
                }
            }
        }
        return values;
    }

    private void extractList(ArrayInitializerExpr t, List<String> values) {
        NodeList<Expression> values1 = t.getValues();
        for (int i = 0; i < values1.size(); i++) {
            Expression expression = values1.get(i);
            expression.ifStringLiteralExpr(t1 -> values.add(t1.asString()));
        }
    }


    private UnitSummary parseSummary(TypeDeclaration<?> typeDeclaration, Optional<AnnotationExpr> requestMapping) {
        final UnitSummary unitSummary = new UnitSummary();

        requestMapping.ifPresent(rm -> {
            unitSummary.setBasePath(annoKeyValue(rm, "value", "name"));
        });


        Optional<AnnotationExpr> swaggerApi = typeDeclaration.getAnnotationByClass(Api.class);
        swaggerApi.ifPresent(api -> {
            String comment = annoKeyValue(api, "value");
            String note = annoKeyValue(api, "notes");
            unitSummary.addComment(comment);
            unitSummary.addNote(note);
            List<String> tags = annoKeyListValue(api, "tags");
            if (tags.size() > 0) {
                //处理 group path
                unitSummary.setGroupPath(tags.get(0));
            }
        });

        Optional<Javadoc> javadoc = typeDeclaration.getJavadoc();
        javadoc.ifPresent(doc -> {
            unitSummary.addComment(doc.getDescription().toText());
            Optional<String> author = findTag(doc, "author");
            if (author.isPresent()) {
                String a = author.get();
                if (Strings.isBlank(a)) {
                    unitSummary.setAuthor("");
                } else {
                    unitSummary.setAuthor(a);
                }
            } else {
                unitSummary.setAuthor("");
            }
        });

        if (Strings.isBlank(unitSummary.getGroupPath())) {
            unitSummary.setGroupPath(DocTools.normalPath(typeDeclaration.getNameAsString()));
        }

        return unitSummary;
    }


    /**
     * 从JavaDoc 找到标签的内容
     *
     * @param doc
     * @param key
     * @return
     */
    private Optional<String> findTag(Javadoc doc, String key) {
        if (doc == null || Strings.isBlank(key)) {
            return Optional.empty();
        }

        String tempKey = Strings.trim(key).toUpperCase();
        List<String> collect = doc.getBlockTags().stream().filter(t -> t.getTagName().toUpperCase().startsWith(tempKey))
                .map(tag -> tag.getContent().toText()).collect(Collectors.toList());
        if (collect.size() > 0) {
            return Optional.ofNullable(collect.get(0));
        }
        return Optional.empty();
    }

    /**
     * 获取注解的属性
     *
     * @param anno
     * @param keys
     * @return
     */
    private String annoKeyValue(AnnotationExpr anno, String... keys) {
        if (anno == null || Lang.isEmpty(keys)) {
            return "";
        }
        List<Node> childNodes = anno.getChildNodes();
        final List<String> values = new ArrayList<>();
        for (Node n : childNodes) {
            if (n instanceof MemberValuePair) {
                MemberValuePair pair = (MemberValuePair) n;
                String key = pair.getNameAsString();
                if (Lang.contains(Lang.array(keys), key)) {
                    pair.getValue().ifStringLiteralExpr(t -> values.add(t.asString()));
                }
            }
        }
        if (values.size() > 0) {
            return values.get(0);
        }
        return null;
    }


}
