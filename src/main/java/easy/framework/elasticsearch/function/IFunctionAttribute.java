package easy.framework.elasticsearch.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/27.
 */
@FunctionalInterface
public interface IFunctionAttribute<T> extends Serializable {
    Object apply(T source);
}
