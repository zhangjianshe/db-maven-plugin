package cn.mapway.plugin.doc;

import cn.mapway.tools.doc.DocGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Doc
 * 文档生成器
 *
 * @author zhangjianshe@gmail.com
 */
@Mojo(name = "doc", defaultPhase = LifecyclePhase.POST_SITE)
@Slf4j
public class DocMojo {
    /**
     * The path.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/resource", property = "output", required = true)
    private String output;

    @Parameter(property = "scans", required = true)
    private List<String> scans;

    @Parameter(defaultValue = "html", property = "format", required = true)
    private String format;

    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "basePath", required = true)
    private String basePath;
    @Parameter(defaultValue = "${project.target}", property = "libPath", required = true)
    private String libPath;

    /**
     * 执行
     * Execute mojo
     *
     * @throws MojoExecutionException 魔力执行异常
     */
    public void execute()
            throws MojoExecutionException {
        DocConfiguration docConfiguration = new DocConfiguration();
        docConfiguration.setFormat(format);
        docConfiguration.setOutput(output);
        docConfiguration.setScans(scans);
        DocGen docGen = new DocGen(docConfiguration);
        docGen.run();
    }
}
