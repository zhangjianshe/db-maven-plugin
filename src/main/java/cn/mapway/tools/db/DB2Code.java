package cn.mapway.tools.db;

import cn.mapway.tools.db.naming.CamelConvert;
import cn.mapway.tools.db.naming.INameConvertor;
import com.alibaba.druid.sql.SQLUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.squareup.javapoet.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.*;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.lang.model.element.Modifier;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * DB2 CODE
 *
 * @author zhangjianshe
 */
public class DB2Code {

    private final static String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "<mapper ></mapper>";
    private static final Logger logger = Logger.getLogger(DB2Code.class.getName());
    /**
     * configuration
     */
    IConfigure configure;
    INameConvertor camelConvert = new CamelConvert();

    /**
     * constuction function
     *
     * @param configuration config
     */
    public DB2Code(IConfigure configuration) {
        this.configure = configuration;
    }

    private Connection getConnection() {
        final String connectionUrl = configure.getJdbcUrl();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        System.out.println(Json.toJson(drivers));
        final DatabaseConnectionSource dataSource = new DatabaseConnectionSource(connectionUrl);

        dataSource.setUserCredentials(new SingleUseUserCredentials(configure.getUser(), configure.getPassword()));
        return dataSource.get();
    }

    public void run() {

        logger.info("============EXPORT DB DAO CODE=============");

        if (Strings.isBlank(configure.getSchema())) {
            logger.warning("没有定义数据库schema,本次操作不会生成任何DAO代码");
            return;
        }

        final List<String> includes = configure.includes();
        final List<String> excludes = configure.excludes();

        // Create the options
        final LimitOptionsBuilder limitOptionsBuilder =
                LimitOptionsBuilder.builder()
                        .includeSchemas(new RegularExpressionInclusionRule(configure.getSchema()))
                        .includeTables(tableFullName -> {
                            if (tableFullName.contains("$"))
                                return false;
                            int index = tableFullName.lastIndexOf('.');

                            String tableName = tableFullName;
                            if (index >= 0) {
                                tableName = tableFullName.substring(index + 1);
                            }
                            if (includes.size() > 0) {
                                return inList(includes, tableName);
                            } else {
                                return true;
                            }
                        });

        final LoadOptionsBuilder loadOptionsBuilder =
                LoadOptionsBuilder.builder()
                        // Set what details are required in the schema - this affects the
                        // time taken to crawl the schema
                        .withSchemaInfoLevel(SchemaInfoLevelBuilder.detailed());

        final SchemaCrawlerOptions options =
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                        .withLimitOptions(limitOptionsBuilder.toOptions())
                        .withLoadOptions(loadOptionsBuilder.toOptions());


        // Get the schema definition
        final Catalog catalog;
        try {
            catalog = SchemaCrawlerUtility.getCatalog(getConnection(), options);
        } catch (SchemaCrawlerException e) {
            e.printStackTrace();
            return;
        }

        for (final Schema schema : catalog.getSchemas()) {

            for (final Table table : catalog.getTables(schema)) {
                String tableName = table.getName();
                if (inList(excludes, tableName)) {
                    continue;
                }

                if (includes.size() > 0) {
                    if (!inList(includes, tableName)) {
                        continue;
                    }
                }
                exportTableEntity(table, configure);
                exportTableMapper(table, configure);
                exportTableDao(table, configure);
                try {
                    exportTableMapperXML(table, configure);
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean inList(List<String> includes, String table) {
        for (String s : includes) {
            if (s.compareToIgnoreCase(table) == 0) {
                return true;
            }
        }
        return false;
    }

    private String u(String s) {
        return s;
    }

    private String getClassTypeName(String name) {
        String temp = camelConvert.convert(name);
        return Strings.upperFirst(temp);
    }

    /**
     * 输出DAO类
     *
     * @param table
     * @param configure
     */
    private void exportTableMapper(Table table, IConfigure configure) {

        String fileName = getClassTypeName(table.getName()) + "Mapper";

        ClassName entityName = ClassName.get(configure.entityPackage(), getClassTypeName(table.getName()) + "Entity");

        ParameterizedTypeName t = ParameterizedTypeName.get(ClassName.get(BaseMapper.class), entityName);

        TypeSpec.Builder typeBuilder = TypeSpec.interfaceBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(t);

        typeBuilder.addJavadoc("<b>$L.$L</b>\r\n$L\r\n@author $L\r\n",
                table.getSchema().getCatalogName(), table.getName(),
                u(table.getRemarks()), configure.author());
        typeBuilder.addJavadoc("==================字段定义================\r\n");
        for (Column c : table.getColumns()) {
            typeBuilder.addJavadoc("$L\t$L\t$L\r\n", c.getName(), c.getColumnDataType().getName(), c.getRemarks());
        }

        String dbContext = configure.dbContext();
        if (Strings.isNotBlank(dbContext)) {
            dbContext = "." + dbContext;
        } else {
            dbContext = "";
        }
        if (!configure.overrideDao()) {//不覆盖

            Boolean exist = isFileExist(configure.daoPath(), configure.daoPackage() + ".mapper" + dbContext, fileName + ".java");
            if (exist) {
                logger.warning("存在Mapper文件" + fileName);
                return;
            }
        }

        try {
            JavaFile javaFile = JavaFile.builder(configure.daoPackage() + ".mapper" + dbContext, typeBuilder.build()).build();
            javaFile.writeTo(new File(configure.daoPath()));
        } catch (IOException e) {
            logger.warning("输出Mapper文件出错了,(" + fileName + ")" + e.getMessage());
        }
    }

    private Boolean isFileExist(String daoPath, String daoPackage, String fileName) {
        Path outputDirectory = new File(daoPath).toPath();
        if (!daoPackage.isEmpty()) {
            String temp = daoPackage.replace(".", File.separator);
            outputDirectory = outputDirectory.resolve(temp);
            try {
                Files.createDirectories(outputDirectory);
            } catch (IOException e) {
            }
        }

        Path outputPath = outputDirectory.resolve(fileName);
        return outputPath.toFile().exists();

    }

    /**
     * 输出Mapper
     *
     * @param table
     */
    private void exportTableDao(Table table, IConfigure configure) {
        String fileName = getClassTypeName(table.getName()) + "Dao";

        ClassName entityName = ClassName.get(configure.entityPackage(), getClassTypeName(table.getName()) + "Entity");

        String dbContext = configure.dbContext();
        if (Strings.isNotBlank(dbContext)) {
            dbContext = "." + dbContext;
        } else {
            dbContext = "";
        }

        ClassName mapperName = ClassName.get(configure.daoPackage() + ".mapper" + dbContext, getClassTypeName(table.getName()) + "Mapper");

        ClassName parentClassName = ClassName.get("com.baomidou.mybatisplus.extension.service.impl", "ServiceImpl");
        ParameterizedTypeName t = ParameterizedTypeName.get(parentClassName, mapperName, entityName);

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(t);


        typeBuilder.addAnnotation(AnnotationSpec.builder(Component.class).build());

        if (Strings.isNotBlank(configure.dbSourceName())) {
            //有数据源可以选择
            //添加注解 com.ziroom.hddp.common.db.DbSource

            AnnotationSpec.builder(Component.class).build();
            ClassName cn = ClassName.get("com.ziroom.hddp.common.db", "DbSource");

            AnnotationSpec.Builder builder = null;
            if (configure.dbSourceName().startsWith("\"")) {
                builder = AnnotationSpec.builder(cn).addMember("value", "$L", configure.dbSourceName());
            } else {
                ClassName className = ClassName.bestGuess(configure.dbSourceName());
                builder = AnnotationSpec.builder(cn).addMember("value", "$T", className);
            }
            typeBuilder.addAnnotation(builder.build());

        }


        if (!configure.overrideDao()) {//不覆盖
            Boolean exist = isFileExist(configure.daoPath(), configure.daoPackage() + ".service", fileName + ".java");
            if (exist) {
                logger.warning("存在Dao文件" + fileName);
                return;
            }
        }

        try {
            JavaFile javaFile = JavaFile.builder(configure.daoPackage() + ".service", typeBuilder.build()).build();
            javaFile.writeTo(new File(configure.daoPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出table的Mapper类
     *
     * @param table
     * @param configure
     */
    private void exportTableMapperXML(Table table, IConfigure configure) throws TransformerException {
        // maperPath/com/XX/XX/dbContext/YYYMapper.xml
        String path = configure.mapperPath();
        String temp = ClassUtils.convertClassNameToResourcePath(configure.daoPackage());
        path += File.separator + temp + File.separator + "mapper";
        String dbContext = configure.dbContext();
        if (Strings.isNotBlank(dbContext)) {
            dbContext = dbContext + File.separator;
        } else {
            dbContext = "";
        }
        path = path + File.separator + dbContext;

        if (Strings.isNotBlank(path)) {
            org.nutz.lang.Files.makeDir(new File(path));
        }
        String fileName = getClassTypeName(table.getName()) + "Mapper.xml";

        String pathName = path + fileName;
        File file = new File(pathName);

        org.dom4j.Document doc = null;


        if (file.exists()) {
            try {

                SAXReader saxReader = new SAXReader();
                saxReader.setValidation(false);
                saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                doc = saxReader.read(file); // 读取XML文件,获得document对象
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                doc = createDoc();
            }

            org.dom4j.Element root = doc.getRootElement();

            if (root.getName().equals("mapper")) {
                //找对文件了
                doc = null;//强制不输出文件
            } else {
                logger.warning(fileName + " 不是一个映射文件，请检查>" + root.getName());
                return;
            }
        } else {
            doc = createDoc();
            org.dom4j.Element root = doc.getRootElement();
            String dbContext1 = configure.dbContext();
            if (Strings.isNotBlank(dbContext1)) {
                dbContext1 = "." + dbContext1;
            } else {
                dbContext1 = "";
            }
            ClassName mapperName = ClassName.get(configure.daoPackage() + ".mapper" + dbContext1, getClassTypeName(table.getName()) + "Mapper");
            root.addAttribute("namespace", mapperName.canonicalName());
        }

        //format sql
        format(doc);
        if (doc != null) {
            //更新或者添加 映射节点
            boolean flag = true;
            try {
                OutputFormat format = new OutputFormat();
                format.setEncoding("UTF-8");
                format.setTrimText(false);
                format.setNewlines(true);
                format.setIndent(true);
                format.setPadText(false);
                format.setExpandEmptyElements(true);
                format.setXHTML(true);


                logger.info("ready to produce " + pathName);
                XMLWriter writer = new XMLWriter(Streams.fileOut(file), format);
                writer.write(doc);
                writer.close();

            } catch (Exception ex) {
                flag = false;
                logger.info("生成XML映射文件出错了" + ex.getLocalizedMessage());
            }
        }
    }

    private void format(Document doc) {
        Element root = doc.getRootElement();
        for (Element e : root.elements()) {
            String data = (String) e.getData();
            if (data != null) {
                data = SQLUtils.format(data, "");
                if (Strings.isNotBlank(data)) {
                    data = "\r" + data + "\r";
                }
            }
            List<Element> subs = e.elements();
            e.clearContent();
            e.setText(data);
            for (Element sub : subs) {
                e.add(sub);
                if (sub instanceof Text) {
                    Text t = (Text) sub;
                    String text = t.getText();
                    if (Strings.isNotBlank(text)) {
                        logger.info(text);

                        text = SQLUtils.format(text, "");
                        e.setText(text);
                        logger.info(e.getText());
                    }
                }
            }
        }
    }

    private void clearChildren(org.dom4j.Element resultMap) {
        resultMap.clearContent();
        resultMap.setText("");
    }

    private org.dom4j.Document createDoc() {

        try {
            SAXReader saxReader = new SAXReader();
            saxReader.setValidation(false);
            saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            org.dom4j.Document doc = saxReader.read(Streams.wrap(xmlBody.getBytes()));
            return doc;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 输出table实体类
     *
     * @param table
     * @param configure
     */
    private void exportTableEntity(Table table, IConfigure configure) {


        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getClassTypeName(table.getName()) + "Entity")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                        AnnotationSpec.builder(TableName.class).addMember("value", "value=$S", table.getName()).build());
        typeBuilder.addJavadoc("<b>$L.$L</b>\r\n$L\r\n$L\r\n@author $L", table.getSchema().getCatalogName(), table.getName(), u(table.getRemarks()), table.getDefinition(), configure.author());

        if (configure.lombok()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(Data.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(Builder.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(NoArgsConstructor.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(AllArgsConstructor.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(Accessors.class).addMember("chain", "true").build());
        }

        //输出静态字段名称
        if (configure.withStaticField()) {
            for (Column column : table.getColumns()) {
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(String.class, "FLD_" + column.getName().toUpperCase(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", column.getName());
                fieldBuilder.addJavadoc("$L\r\n数据库字段序号:$L", column.getName(), "" + column.getOrdinalPosition());
                typeBuilder.addField(fieldBuilder.build());
            }
        }

        for (Column column : table.getColumns()) {

            Class type = getDataType(column, column.getColumnDataType());
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(type,
                    camelConvert.convert(column.getName()),
                    Modifier.PRIVATE);

            if (type.getCanonicalName().equals(Timestamp.class.getCanonicalName())
                    || type.getCanonicalName().endsWith(Date.class.getCanonicalName())
                    || type.getCanonicalName().endsWith(java.sql.Date.class.getCanonicalName())
            ) {
                if (!Strings.isBlank(configure.dateFormat())) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonFormat.class)
                            .addMember("value", "pattern=$S", configure.dateFormat()).build());
                }
            }

            fieldBuilder.addJavadoc("$L \r\n缺省值:$L\r\n数据库字段长度:$L($L)",
                    u(column.getRemarks()), column.getDefaultValue(), column.getSize(),
                    column.isNullable() ? "允许为空" : "不允许为空");

            if (column.isPartOfPrimaryKey()) {
                if (column.isAutoIncremented()) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(TableId.class)
                            .addMember("value", "$S", column.getName())
                            .addMember("type", "$T.$L", IdType.class, IdType.AUTO.name())
                            .build());
                } else {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(TableId.class)
                            .addMember("value", "$S", column.getName())
                            .addMember("type", "$T.$L", IdType.class, IdType.INPUT
                                    .name())
                            .build());
                }
            } else {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(TableField.class).addMember("value", "$S", column.getName()).build());
            }
            typeBuilder.addField(fieldBuilder.build());
        }
        typeBuilder.addSuperinterface(Serializable.class);

        JavaFile javaFile = JavaFile.builder(configure.entityPackage(), typeBuilder.build()).build();

        try {

            javaFile.writeTo(new File(configure.entityPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    Class getDataType(Column c, ColumnDataType columnDataType) {

        if (columnDataType.getName().equals("NUMBER")) {

            if (c.getDecimalDigits() <= 0) {
                if (c.getSize() < 10) {
                    return Integer.class;
                }
                return Long.class;
            } else {
                return Double.class;
            }
        }
        String typeName = columnDataType.getName().toUpperCase();
        if (
                typeName.startsWith("TIMESTAMP")
                        || typeName.startsWith("DATETIME")
                        || typeName.startsWith("DATE")

        ) {
            return Date.class;
        }
        return columnDataType.getTypeMappedClass();
    }
}
