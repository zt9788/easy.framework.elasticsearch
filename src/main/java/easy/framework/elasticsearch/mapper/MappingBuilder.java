package easy.framework.elasticsearch.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import easy.framework.elasticsearch.annotation.ESField;
import easy.framework.elasticsearch.annotation.ElsDefaultValue;
import easy.framework.elasticsearch.annotation.ElsId;
import easy.framework.elasticsearch.annotation.ElsSuggestKey;
import easy.framework.elasticsearch.metadata.ESDateFormat;
import easy.framework.elasticsearch.metadata.ESFieldType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author: Zhangtong
 * @description: mapping builder
 * @Date: 2020/2/20.
 */
@Slf4j
@Component
@SuppressWarnings(value = {"rawtypes","unused"})
public class MappingBuilder {

    public static String DEFAULT_SUGGEDT_KEY_PREFIX = "suggest";

    /**
     * 设置对象的ElasticSearch的Mapping
     *
     * @param clz
     * @param isAnn
     * @return
     */
    public static XContentBuilder setMapping(Class clz,boolean isAnn) throws Exception {
//        if(!isAnn) return setMapping(obj);
        List<Field> fieldList = getFields(clz);
        XContentBuilder mapping = null;
//        try {
            mapping = jsonBuilder().startObject().startObject("properties");
            for (Field field : fieldList) {
                //修饰符是static的字段不处理
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String name = field.getName();
                if (!isAnn) {
                    mapping.startObject(name)
                            .field("type", getElasticSearchMappingType(field.getType().getSimpleName().toLowerCase()))
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_max_word")
                            .endObject();
                    continue;
                }
                if (field.isAnnotationPresent(ElsSuggestKey.class)) {
                    ElsSuggestKey annotation = field.getAnnotation(ElsSuggestKey.class);
                    String suggestKey = annotation.value();
                    if (StringUtils.isBlank(suggestKey)) {
                        suggestKey = DEFAULT_SUGGEDT_KEY_PREFIX + "_" + name;
                    }
                    mapping.startObject(suggestKey)
                            .field("type", "completion")
                            //使用IK分词器
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_max_word")
                            .endObject();
                }
                if(field.isAnnotationPresent(ElsId.class)){
                    ElsId elsId = field.getAnnotation(ElsId.class);
                    String orgType = getElasticSearchMappingType(field.getType().getSimpleName().toLowerCase());
                    String type = orgType;
//                    if(type.equals("text"))
                        type = "keyword";
                    mapping.startObject(name)
                            .field("type", type);
//                            .field("fielddata",true)
                    if(elsId.mutilField()){
                        mapping.startObject("fields")
                                .startObject(orgType)
                                .field("type",orgType)
                                //.field("ignore_above",256)
                                .endObject().endObject();
                    }
                    mapping.endObject();
                }
                if (field.isAnnotationPresent(ESField.class)) {
                    ESField elsSeacherKey = field.getAnnotation(ESField.class);
                    ElsDefaultValue defaultValue = field.getAnnotation(ElsDefaultValue.class);
//                    String[] ignos = elsSeacherKey.ignoreFields();
//                    if(ignos.length > 0 && StringUtils.isNotBlank(ignos.toString())){
//                        for(String igno:ignos){
//                            if(igno.equals(name))
//                                continue;
//                        }
//                    }
                    //2.2.4 has it
                    /*
                    if(StringUtils.isNotBlank(elsSeacherKey.name())){
                        name = elsSeacherKey.name();
                    }
                    */
                    String type;
                    if (elsSeacherKey.type() == ESFieldType.Auto)
                        type = getElasticSearchMappingType(field.getType().getSimpleName().toLowerCase());
                    else {
                        type = elsSeacherKey.type().name().toLowerCase();
                        if(type.equals("integer"))
                            type = "long";
                    }
                    log.debug(name);
                    mapping.startObject(name);
                    mapping.field("type", type);
                    if(type.equals("text") && !elsSeacherKey.fielddata()){
                        mapping.startObject("fields")
                                .startObject("keyword")
                                .field("type","keyword")
                                .field("ignore_above",256).endObject().endObject();
                    }
                    if(type.equals("date") && ESDateFormat.custom == elsSeacherKey.format())
                        mapping.field("format",elsSeacherKey.pattern());

                    if (elsSeacherKey.fielddata()) {
                        if(elsSeacherKey.type() != ESFieldType.Text)
                            throw new Exception("只有Text才需要标记这个属性");
                        mapping.field("fielddata", "true");
                    }
                    //使用IK分词器
                    if (StringUtils.isNotBlank(elsSeacherKey.analyzer())) {
                        if (elsSeacherKey.analyzer().equals("auto"))
                            mapping.field("analyzer", "ik_max_word");
                        else
                            mapping.field("analyzer", elsSeacherKey.analyzer());
                    }
//                    else{
//                        if(elsSeacherKey.type() == FieldType.Text)
//                            builder = builder.field("analyzer", "ik_max_word");
//                    }
                    if (StringUtils.isNotBlank(elsSeacherKey.searchAnalyzer())) {
                        if (elsSeacherKey.searchAnalyzer().equals("auto"))
                            mapping.field("search_analyzer", "ik_max_word");
                        else
                            mapping.field("search_analyzer", elsSeacherKey.searchAnalyzer());
                    }
//                    else{
//                        if(elsSeacherKey.type() == FieldType.Text)
//                            builder.field("search_analyzer", "ik_max_word");
//                    }
                    /*
                    if(StringUtils.isNotBlank(elsSeacherKey.normalizer())&& elsSeacherKey.type() == FieldType.Keyword){
                        if(elsSeacherKey.normalizer().equals("auto"))
                            mapping.field("normalizer","my_normalizer");
                        else
                            mapping.field("normalizer",elsSeacherKey.normalizer());
                    }
                     */
                    if(defaultValue != null){// && !defaultValue.isNull()){
                        if(elsSeacherKey != null && elsSeacherKey.type() == ESFieldType.Text)
                            throw new Exception("ElsDefaultValue 注解必须是非Text属性");
                        mapping.field("null_value",defaultValue.value());
//                        mapping.field("null_value","key1");
                    }
//                    else if(defaultValue != null){
//                        mapping.field("null_value","null");
//                    }
                    mapping.endObject();
//                    mapping.endObject();
                }
            }
            mapping.endObject().endObject();
            log.info(JSONObject.toJSONString(mapping));
            log.info(mapping.toString());
//        } catch (IOException e) {
//            log.error("构建mapping出错",e);
//        }
        log.debug(JSONObject.toJSONString(mapping));
        return mapping;
    }
    /**
     * 获取对象所有自定义的属性
     *
     * @param obj
     * @return
     */
    protected static List<Field> getFields(Class obj) {
        Field[] fields = obj.getDeclaredFields();
        List<Field> fieldList = new ArrayList<>();
        fieldList.addAll(Arrays.asList(fields));
        Class objClass =  obj.getSuperclass();
        while (null != objClass) {
            fieldList.addAll(Arrays.asList(objClass.getDeclaredFields()));
            objClass = objClass.getSuperclass();
        }
        return fieldList;
    }
    /**
     * java类型与ElasticSearch类型映射
     *
     * @param varType
     * @return
     */
    protected static String getElasticSearchMappingType(String varType) {
        String es;
        switch (varType) {
            case "date":
                es = "date";
                break;
            case "double":
            case "float":
                es = "double";
                break;
            case "long":
            case "int":
            case "integer":
                es = "long";
                break;
            case "boolean":
                es = "boolean";
                break;
            default:
                es = "text";
                break;
        }
        return es;
    }
    public static String findKeyField(Field[] fields) {
        String result = "id";
        for (Field field: fields) {
            if(field.isAnnotationPresent(ElsId.class)){
                result = field.getName();
                return result;
            }
        }
        return  result;
    }
    public static Object getID(Object entry){
        String fieldName = MappingBuilder.findKeyField(entry.getClass().getFields());
        fieldName = "get"+ MappingBuilder.toUpperCaseFirstOne(fieldName);
        Method method;
        try {
            method = entry.getClass().getDeclaredMethod(fieldName);
            Object obj = method.invoke(entry);
            return obj;
        }catch (Exception e){
            throw new NullPointerException("获得id失败");
        }
    }
    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0))) {
            return s;
        }else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    public static String entityString(Object entity){
        return JSONObject.toJSONString(entity,
                SerializerFeature.WriteMapNullValue);
    }
}
