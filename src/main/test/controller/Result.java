package controller;

import lombok.Data;

import java.util.List;

/**
 * Result
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class Result<T> {
    public Integer result;
    public T data;
    public List<T> lists;
}
