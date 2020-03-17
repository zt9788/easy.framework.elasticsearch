package easy.framework.elasticsearch.mapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/3/2.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings(value = {"rawstype","unused"})
public class ESQueryWrapper<T> extends ESBaseWrapper<T> implements Serializable {

//    protected ESWrappers<T> esWrappers;
    public ESQueryWrapper(ESWrappers<T> wrappers){
//        this.esWrappers = wrappers;
        super(wrappers);
    }
    private Map<String, List<Object>> in = new ConcurrentHashMap<>();
    public <T> ESWrappers<T> eq(String key,Object value){
        if(in.containsKey(key)){
            in.get(key).add(value);
        }else{
            List<Object> list = new ArrayList<>();
            list.add(value);
            in.put(key,list);
        }
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> in(String key,List<Object> values){
        if(in.containsKey(key)) {
            in.get(key).addAll(values);
        }else{
            in.put(key,values);
        }
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> in(String key,Object... valus){
        if(in.containsKey(key)) {
            in.get(key).addAll(Arrays.asList(valus));
        }else{
            in.put(key,Arrays.asList(valus));
        }
        return (ESWrappers<T>) this.esWrappers;
    }

    private Map<String,List<ESRangeAttribute>> range = new ConcurrentHashMap<>();
    public <T> ESWrappers<T> lt(String key,Object value){
        if(range.containsKey(key)){
            range.get(key).add(new ESRangeAttribute(value,true,false));
        }else {
            List<ESRangeAttribute> list = new ArrayList<>();
            list.add(new ESRangeAttribute(value, true, false));
            range.put(key, list);
        }
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> lte(String key,Object value){
        if(range.containsKey(key)){
            range.get(key).add(new ESRangeAttribute(value,true,true));
        }else {
            List<ESRangeAttribute> list = new ArrayList<>();
            list.add(new ESRangeAttribute(value,true,true));
            range.put(key, list);
        }
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> gt(String key,Object value){
        if(range.containsKey(key)){
            range.get(key).add(new ESRangeAttribute(value,false,false));
        }else {
            List<ESRangeAttribute> list = new ArrayList<>();
            list.add(new ESRangeAttribute(value,false,false));
            range.put(key, list);
        }
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> gte(String key,Object value){
        if(range.containsKey(key)){
            range.get(key).add(new ESRangeAttribute(value,false,true));
        }else {
            List<ESRangeAttribute> list = new ArrayList<>();
            list.add(new ESRangeAttribute(value,false,true));
            range.put(key, list);
        }
        return (ESWrappers<T>) this.esWrappers;
    }

    private Map<String,List<Object>> like = new ConcurrentHashMap<>();
    public <T> ESWrappers<T> like(String key, String value){
        if (this.like.containsKey(key)){
            this.like.get(key).add(value);
        }else{
            List<Object> values = new ArrayList<>();
            values.add(value);
            this.like.put(key,values);
        }
        return (ESWrappers<T>) this.esWrappers;
    }

    private List<String> isNull = new ArrayList<>();
    public <T> ESWrappers<T> isNull(String key){
        isNull.add(key);
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> isNull(String... keys){
        isNull.addAll(Arrays.asList(keys));
        return (ESWrappers<T>) this.esWrappers;
    }

    private List<String> isNotNull = new ArrayList<>();
    public <T> ESWrappers<T> isNotNull(String key){
        isNotNull.add(key);
        return (ESWrappers<T>) this.esWrappers;
    }
    public <T> ESWrappers<T> isNotNull(String... keys){
        isNotNull.addAll(Arrays.asList(keys));
        return (ESWrappers<T>) this.esWrappers;
    }

}
