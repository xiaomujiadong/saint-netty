package com.saint.netty.util;

import com.saint.netty.annotation.MsgType;
import com.saint.netty.handler.process.AbstractProcessMsg;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.reflections.Reflections;

/**
 */
public class AnnotionManagerUtil {

    /**
     * 定义classMap
     */
    private static Map<Integer, String> classMap = new ConcurrentHashMap<>();

    /**
     * 记录beanName的名字
     */
    private static Set<String> beanNameSet = new HashSet<>();

    /**
     * 维护msgType与processor的关系
     */
    private static Map<Integer, AbstractProcessMsg> processMsgMap = new HashMap<>();

    /**
     * 维护beanName和msgType的关系
     */
    private static Map<String, Integer> beanNameMsgType = new HashMap<>();

    static {
        //反射工具包
        Reflections reflections = new Reflections("com.saint.netty");
        //获取带Handler注解的类
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(MsgType.class);
        for (Class classes : classSet) {
            MsgType msgType = (MsgType) classes.getAnnotation(MsgType.class);
            //获取注解的value
            int value = msgType.msgType();
            String beanName = decapitalize(classes.getSimpleName());
            classMap.put(value, beanName);
            beanNameSet.add(beanName);
            beanNameMsgType.put(beanName, value);
        }
    }

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        // 如果发现类的前两个字符都是大写，则直接返回类名
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))){
            return name;
        }
        // 将类名的第一个字母转成小写，然后返回
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static boolean cotainBeanName(String beanName){
        return beanNameSet.contains(beanName);
    }

    public static void addProcessor(int msgType, AbstractProcessMsg abstractProcessMsg){
        processMsgMap.put(msgType, abstractProcessMsg);
    }

    public static AbstractProcessMsg getProcessorByMsgType(int msgType){
        return processMsgMap.get(msgType);
    }

    public static Integer getMsgTypeByBeanName(String beanName){
        return beanNameMsgType.get(beanName);
    }
}
