package easy.framework.elasticsearch.annotation;

import easy.framework.elasticsearch.metadata.ESFieldType;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description: els id setting
 * @Date: 2020/2/20.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElsId {
//    boolean value() default true;
    ESFieldType type() default ESFieldType.Auto;
    boolean mutilField() default false;
}
