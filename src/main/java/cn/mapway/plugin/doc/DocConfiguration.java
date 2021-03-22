package cn.mapway.plugin.doc;

import lombok.Data;

import java.util.List;

/**
 * DocConfiguration
 * 生成文档的配置参数
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class DocConfiguration {
    private String basePath;
    private String libPath;
    private String output;
    private List<String> scans;
    private String format;
}
