package easy.framework.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description: HighlightField setting
 * @Date: 2020/2/20.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElsHighlightField {
    String value() default "unified";
    String preTags() default "<font color='#D0021B'>";
    String postTags() default "</font>";
    boolean isAuto() default true;
}
