package easy.framework.elasticsearch.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/22.
 */
@Slf4j
@SuppressWarnings(value = {"rawstype","unused"})
public class ESWrapperBuilder {

    public static <T> ESWrappers<T> build(ESWrappers<T> entityWrapper, T entity , boolean eqZero){
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(entity.getClass());
        } catch (IntrospectionException e) {
            log.error("build error", e);
            return null;
        }
        List<PropertyDescriptor> descriptors = Arrays.stream(beanInfo.getPropertyDescriptors()).filter(p -> {
            String name = p.getName();
            //过滤掉不需要修改的属性
            return !"class".equals(name) && !"id".equals(name);
        }).collect(Collectors.toList());
        for (PropertyDescriptor descriptor : descriptors) {
            Method readMethod = descriptor.getReadMethod();
            if(descriptor.getPropertyType().equals(Map.class))
                continue;
            if(readMethod == null) continue;
            try {
                Object o = readMethod.invoke(entity);
                if(null != o && StringUtils.isNotBlank(o.toString())){
                    if((o.toString().equals("0") && !eqZero)){

                    }else {
                        if(o instanceof Date){
                            Date dt = (Date) o;
                            entityWrapper.and().eq(descriptor.getName(), dt.getTime());
                        }else {
                            entityWrapper.and().eq(descriptor.getName(), o);
                        }
                    }
                }
            }catch (Exception e){
                log.error("build error",e);
            }
        }
        log.info(entity.toString());
        return entityWrapper;
    }

    public static Object getInfoValue(Object entity,String fName){
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(entity.getClass());
        } catch (IntrospectionException e) {
            log.error("build error", e);
            return null;
        }
        List<PropertyDescriptor> descriptors = Arrays.stream(beanInfo.getPropertyDescriptors()).filter(p -> {
            String name = p.getName();
            //过滤掉不需要修改的属性
            return !"class".equals(name) && !"id".equals(name);
        }).collect(Collectors.toList());
        for (PropertyDescriptor descriptor : descriptors) {
            Method readMethod = descriptor.getReadMethod();
            if(!descriptor.getName().equals(fName))
                continue;
            if(readMethod == null) continue;
            try {
                Object o = readMethod.invoke(entity);
                return o;
            }catch (Exception e){
                log.error("build error",e);
                return null;
            }
        }
        return null;
    }
}
