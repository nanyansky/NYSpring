package com.nanyan.spring;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYComponentScan;
import com.nanyan.spring.annotation.NYScope;
import com.nanyan.spring.interfaces.ApplicationContextAware;
import com.nanyan.spring.interfaces.BeanNameAware;
import com.nanyan.spring.interfaces.BeanPostProcessor;
import com.nanyan.spring.interfaces.InitializingBean;

import javax.naming.spi.ObjectFactory;
import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nanyan
 * @date 2022/9/11 10:48
 */
public class NanYanApplicationContext {
    private final Class configClass;

    /**
     * BeanDefinition Map --> beanName:beanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * Cache of singleton factories: bean name to ObjectFactory.
     */
    private final Map<String, ObjectFactory> singletonFactories = new HashMap<>(16);

    /**
     * Cache of early singleton objects: bean name to bean instance.
     */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /**
     * 单例池： beanName:beanObj
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 对象处理器集合
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public NanYanApplicationContext(Class configClass) {
        this.configClass = configClass;
        scan(configClass);

        //实例化Bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton"))
            {
                if(singletonObjects.get(beanName) == null)
                {
                    Object bean = createBean(beanName,beanDefinition);
                    singletonObjects.put(beanName,bean);
                }
            }
        }
    }

    //扫描,得到一系列 BeanDefinition，放入 beanDefinitionMap
    private void scan(Class configClass) {
        if (configClass.isAnnotationPresent(NYComponentScan.class)) {
            NYComponentScan componentScanAnnotation = (NYComponentScan) configClass.getAnnotation(NYComponentScan.class);
            //扫描路径：com.nanyan.test
            String path = componentScanAnnotation.value();
            //扫描路径：com/nanyan/test
            path = path.replace(".", "/");
//            System.out.println(path);

            //获取绝对路径
            ClassLoader classLoader = NanYanApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
//            System.out.println(resource);
            File files = new File(resource.getFile());
            if (files.isDirectory()) {
                for (File file : files.listFiles()) {
                    String filePath = file.getAbsolutePath();
//                    System.out.println(filePath);
                    if (filePath.endsWith(".class")) {
                        //提取 class 对象
                        // com\nanyan\test\Test
                        String className = filePath.substring(filePath.indexOf("com"), filePath.indexOf(".class"));
                        className = className.replace("\\", ".");
//                        System.out.println(className);

                        try {
                            Class<?> cls = classLoader.loadClass(className);
                            //判断当前class是否为Bean
                            if (cls.isAnnotationPresent(NYComponent.class)) {
                                NYComponent componentAnnotation = cls.getAnnotation(NYComponent.class);
                                String beanName = componentAnnotation.value();

                                //若Component注解无值，则需要转换类的名字
                                //UserService --> userService
                                if (beanName.equals("")) {
                                    beanName = Introspector.decapitalize(cls.getSimpleName());
                                }

                                // 生成 BeanDefinition，解析 单例bean or 多例bean
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(cls);
                                if (cls.isAnnotationPresent(NYScope.class)) {
                                    NYScope scopeAnnotation = cls.getAnnotation(NYScope.class);
                                    //多例
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    //不写注解 @NYScope 默认使用单例Bean
                                    beanDefinition.setScope("singleton");
                                }
                                //添加到BeanDefinitionMap
                                beanDefinitionMap.put(beanName, beanDefinition);


                                //生成beanPostProcessorList
                                if(BeanPostProcessor.class.isAssignableFrom(cls))
                                {
                                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) cls.getDeclaredConstructor().newInstance();
                                    beanPostProcessorList.add(beanPostProcessor);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //创建Bean
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = null;
        Class classType = beanDefinition.getType();

        try {
            //使用无参构造器
            Constructor<?> constructor = classType.getDeclaredConstructor();
            bean = constructor.newInstance();
            singletonObjects.put(beanName, bean);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //依赖注入
        try {
            populateBean(beanName,beanDefinition,bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        //初始化
        initializeBean(beanName,beanDefinition,bean);



        return bean;
    }

    //get Bean
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinitionMap.containsKey(beanName))
        {
            if(beanDefinition.getScope().equals("singleton"))
            {
                return singletonObjects.get(beanName);
            }
            else
            {
                return createBean(beanName, beanDefinition);
            }
        }
        else
        {
            throw new NullPointerException();
        }
    }

    //依赖注入
    void populateBean(String beanName,BeanDefinition beanDefinition,Object bean) throws IllegalAccessException, InvocationTargetException {
        Class classType = beanDefinition.getType();

        //属性注入
        Field[] fields = classType.getDeclaredFields();
        for(Field field : fields)
        {
            if(field.isAnnotationPresent(NYAutowired.class))
            {
                field.setAccessible(true);
                Object tmpBean = getBean(field.getName());
                field.set(bean,tmpBean);
            }
        }

        //方法注入
        Method[] methods = classType.getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(NYAutowired.class))
            {
                String paraName = method.getParameters()[0].getName();
                method.invoke(bean,getBean(paraName));
            }
        }
    }

    //初始化
    private Object initializeBean(String beanName, BeanDefinition beanDefinition,Object bean)
    {
        //Aware 回调
        if(bean instanceof BeanNameAware)
        {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if(bean instanceof ApplicationContextAware)
        {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }

        //初始化前
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessBeforeInitialization(bean,beanName);
        }

        //初始化时
        if(bean instanceof InitializingBean)
        {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        //初始化后
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessAfterInitialization(bean,beanName);
        }


        return bean;
    }
}
