package controller;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * MyType
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class MyType<T> {
    /**
     * Hello name
     */
    String name;
    /**
     * this is an age
     */
    Double age;

    /**
     * birthday
     *
     * @example '2020-03-04 12:06:01'
     */
    Date time;

    /**
     * template
     */
    List<T> listData;
}
