package easy.framework.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/26.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElsDefaultValue {
//    boolean isNull() default false;
    String value();
}
