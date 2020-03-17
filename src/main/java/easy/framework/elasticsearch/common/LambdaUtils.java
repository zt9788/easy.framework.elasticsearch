package easy.framework.elasticsearch.common;

import com.alibaba.fastjson.JSONObject;
import easy.framework.elasticsearch.function.IFunctionAttribute;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/27.
 */
@Slf4j
public class LambdaUtils {

    public  <T> String convertToFieldName2(IFunctionAttribute fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        String prefix = null;
        if(methodName.startsWith("get")){
            prefix = "get";
        }
        else if(methodName.startsWith("is")){
            prefix = "is";
        }
        if(prefix == null){
            log.warn("无效的getter方法: "+methodName);
        }
        // 截取get/is之后的字符串并转换首字母为小写（S为diboot项目的字符串工具类，可自行实现）
        return methodName;//S.uncapFirst(S.substringAfter(methodName, prefix));
    }
    /***
     * 转换方法引用为属性名
     * @param fn
     * @return
     */
    public static <T> String convertToFieldName(IFunctionAttribute<? extends T> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        log.info(lambda.getImplClass());
        String methodName = lambda.getImplMethodName();
        log.info(JSONObject.toJSONString(lambda));
        String prefix = null;
        if(methodName.startsWith("get")){
            prefix = "get";
        }
        else if(methodName.startsWith("is")){
            prefix = "is";
        }
        if(prefix == null){
            log.warn("无效的getter方法: "+methodName);
        }
        // 截取get/is之后的字符串并转换首字母为小写（S为diboot项目的字符串工具类，可自行实现）
        log.info(methodName);
        return methodName;//S.uncapFirst(S.substringAfter(methodName, prefix));
    }

//    /***
//     * 转换setter方法引用为属性名
//     * @param fn
//     * @return
//     */
//    public static <T,R> String convertToFieldName(IFunctionAttribute<T,R> fn) {
//        SerializedLambda lambda = getSerializedLambda(fn);
//        String methodName = lambda.getImplMethodName();
//        if(!methodName.startsWith("set")){
//            log.warn("无效的setter方法: "+methodName);
//        }
//        // 截取set之后的字符串并转换首字母为小写（S为diboot项目的字符串工具类，可自行实现）
//        return methodName;//S.uncapFirst(S.substringAfter(methodName, "set"));
//    }

    /***
     * 获取类对应的Lambda
     * @param fn
     * @return
     */
    private static SerializedLambda getSerializedLambda(Serializable fn){
        //先检查缓存中是否已存在
        SerializedLambda lambda = null;
        try{//提取SerializedLambda并缓存
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            lambda = (SerializedLambda) method.invoke(fn);
        }
        catch (Exception e){
            log.error("获取SerializedLambda异常, class="+fn.getClass().getSimpleName(), e);
        }
        return lambda;
    }


    public <T> void convertToFieldName3(IFunctionAttribute<T> getAccount) {
    }
}
