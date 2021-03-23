package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

/**
 * TestController
 * 解放手写文档的烦恼
 *
 * @author zhangjianshe@gmail.com
 * @group parent
 */
@Controller
public class TestController {
    /**
     * Just do it
     * 解放手写文档的烦恼===
     *
     * @param file file
     * @param t    tempData
     * @return
     * @group /This/Is/taggroup
     * @tags name, tom
     */
    @PostMapping(value = "/time/losted")
    public Result<MyType<String>> justDoIt(MyType t, MultipartFile file) {
        return new Result<>();
    }
}
