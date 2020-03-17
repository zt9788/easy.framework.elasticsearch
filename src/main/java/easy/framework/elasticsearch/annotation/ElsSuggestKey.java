package easy.framework.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description: SuggestKey setting
 * @Date: 2020/2/20.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElsSuggestKey {
    String value() default "";
}
