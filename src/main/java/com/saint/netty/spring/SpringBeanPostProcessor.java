package com.saint.netty.spring;

import com.saint.netty.handler.process.AbstractProcessMsg;
import com.saint.netty.util.AnnotionManagerUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @date: 2019/9/9 19:37
 */
@Service
public class SpringBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(AnnotionManagerUtil.cotainBeanName(beanName)){
            Integer msgType = AnnotionManagerUtil.getMsgTypeByBeanName(beanName);
            if(msgType!=null){
                AnnotionManagerUtil.addProcessor(msgType, (AbstractProcessMsg)bean);
            }
        }
        return bean;
    }
}
