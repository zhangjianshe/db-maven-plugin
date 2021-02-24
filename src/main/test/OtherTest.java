import org.springframework.util.ClassUtils;

import java.io.File;

/**
 * OtherTest
 *
 * @author zhangjianshe@gmail.com
 */
public class OtherTest {
    public static void main(String[] args) {
        String a=ClassUtils.convertClassNameToResourcePath("de.xyz.MyClass");

        System.out.println(a);
    }
}
