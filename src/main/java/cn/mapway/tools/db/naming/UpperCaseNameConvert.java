package cn.mapway.tools.db.naming;

/**
 * The type Upper case name convert.
 */
public class UpperCaseNameConvert implements INameConvertor {
    @Override
    public String convert(String name) {
        return name.toUpperCase();
    }
}
