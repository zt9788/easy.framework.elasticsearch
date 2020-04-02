package easy.framework.elasticsearch.mapper;


import lombok.Getter;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/3/2.
 */
//@Data
@Getter
public class ESWrappers<T> implements Serializable {
    private Object[] searchAfterValue;
    private boolean useSearchAfter;
    public <T> ESWrappers<T> setUseSearchAfter(boolean useSearchAfter){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用SearchAfter");
        this.useSearchAfter = useSearchAfter;
        return (ESWrappers<T>) this;
    }
    public <T> ESWrappers<T> setUseSearchAfter(boolean useSearchAfter,Object[] value){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用SearchAfter");
        this.useSearchAfter = useSearchAfter;
        this.searchAfterValue = value;
        return (ESWrappers<T>) this;
    }
    private boolean useFilter = true;
    public <T> ESWrappers<T> setUseFilter(boolean useFilter){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.useFilter = useFilter;
        return (ESWrappers<T>) this;
    }
    private TimeValue timeValue = TimeValue.timeValueMinutes(1L);
    public <T> ESWrappers<T> setTimeValue(TimeValue timeValue){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.timeValue = timeValue;
        return (ESWrappers<T>) this;
    }

    private boolean useScroll = false;
    public <T> ESWrappers<T> setUseScroll(boolean useScroll){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.useScroll = useScroll;
        return (ESWrappers<T>) this;
    }
    private String scrollId;
    private <T> ESWrappers<T> setScrollId(String sid){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.scrollId = sid;
        return (ESWrappers<T>) this;
    }

    private int page = 1;
    public <T> ESWrappers<T> setPage(int page){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.page = page;
        if(this.page < 1){
            this.page = 1;
        }
        return (ESWrappers<T>) this;
    }
    private int size = 20;
    public <T> ESWrappers<T> setSize(int size){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.size = size;
        if(this.size < 1){
            this.size = 1;
        }
        return (ESWrappers<T>) this;
    }
    private ESQueryWrapper<T> and;// = new ESQueryWrapper<T>();
    private ESQueryWrapper<T> or;// = new ESQueryWrapper<T>();
    private List<ESWrappers<T>> subCondition;
    public ESWrappers<T> subCondition(){
        ESWrappers<T> wrappers = new ESWrappers<>(this);
//        ESQueryWrapper<T> query = new ESQueryWrapper<>(this);
        subCondition.add(wrappers);
        return wrappers;
    }
    public ESWrappers<T> endCondition(){
        if(superClz == null)
            throw new NullPointerException("没有父wapper");
        return superClz;
    }
    private ESWrappers<T> superClz;
    private ESWrappers(ESWrappers<T> superClz){
        this();
        this.superClz = superClz;
    }
    private ESWrappers(){
        this.and = new ESQueryWrapper<>(this);
        this.or = new ESQueryWrapper<>(this);
        this.subCondition = new ArrayList<>();
    }
    public <T> ESWrappers<T> clear(){
        this.and = new ESQueryWrapper<>(this);
        this.or = new ESQueryWrapper<>(this);
        this.subCondition.clear();
        return (ESWrappers<T>) this;
    }
    public static <T> ESWrappers<T> build(){
        ESWrappers<T> wrappers = new ESWrappers<>();
        return wrappers;
    }

    public ESQueryWrapper<T> and(){
        return this.and;
    }
    public ESQueryWrapper<T> or(){
        return this.or;
    }
    private Map<String, SortOrder> order = new ConcurrentHashMap<>();
    public <T> ESWrappers<T> order(String key,SortOrder order){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.order.put(key,order);
        return (ESWrappers<T>) this;
    }
    private boolean highlight = false;
    public <T> ESWrappers<T> setHighlight(boolean highlight){
        if(this.superClz != null)
            throw new NullPointerException("在子查询中不能使用排序");
        this.highlight = highlight;
        return (ESWrappers<T>) this;
    }
}
