package easy.framework.elasticsearch.annotation;

import easy.framework.elasticsearch.metadata.ESDateFormat;
import easy.framework.elasticsearch.metadata.ESFieldType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/4/3.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ESField {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    ESFieldType type() default ESFieldType.Auto;

    boolean index() default true;

    ESDateFormat format() default ESDateFormat.none;

    String pattern() default "";

    boolean store() default false;

    boolean fielddata() default false;

    String searchAnalyzer() default "";

    String analyzer() default "";

    String normalizer() default "";

    String[] ignoreFields() default {};

    boolean includeInParent() default false;

    String[] copyTo() default {};
}
