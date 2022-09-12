package com.nanyan.spring;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYComponentScan;
import com.nanyan.spring.annotation.NYScope;
import com.nanyan.spring.aop.AnnotationAwareAspectJAutoProxyCreator;
import com.nanyan.spring.interfaces.*;
import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
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
     * 三级缓存，存放实例化完成的 Bean 工厂
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    /**
     * Cache of early singleton objects: bean name to bean instance.
     * 二级缓存，存放早期 Bean 的引用，尚未装配属性的 Bean
     */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /**
     * 单例池： beanName:beanObj
     * 一级缓存，存放完全实例化且属性赋值完成的 Bean ，可以直接使用
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 对象处理器集合
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 需要销毁的 Bean Map
     */
    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    public NanYanApplicationContext(Class configClass) {
        this.configClass = configClass;
        scan(configClass);

        registerBeanPostProcessors();

        preInstantiateSingletons();
    }

    /**
     * 扫描方法
     * @param configClass
     */
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
                                if (BeanPostProcessor.class.isAssignableFrom(cls)) {
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

    /**
     * Bean的生命周期: 创建 -> 依赖注入 -> 初始化
     *
     * @param beanName
     * @param beanDefinition
     * @return Object
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = null;
        try {
            //创建对象
            bean = createBeanInstance(beanName, beanDefinition);

            //若是单例对象，在依赖注入前，须先存入三级缓存
            if (beanDefinition.isSingleton()) {
                Object finalBean = bean;
                this.singletonFactories.put(beanName, new ObjectFactory<Object>() {
                    @Override
                    public Object getObject() throws RuntimeException {
                        for (BeanPostProcessor beanPostProcessor : NanYanApplicationContext.this.beanPostProcessorList) {
                            if (beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor) {
                                ((SmartInstantiationAwareBeanPostProcessor) beanPostProcessor).getEarlyBeanReference(finalBean, beanName);
                            }
                        }
                        return finalBean;
                    }
                });

                this.earlySingletonObjects.remove(beanName);
            }
            //依赖注入
            populateBean(beanName, beanDefinition, bean);
            //初始化
            initializeBean(beanName, beanDefinition, bean);

            //注册需要销毁的 Bean Map
            registerDisposableBeanIfNecessary(beanName,bean,beanDefinition);


        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        }
        return bean;
    }

    //注册需要销毁的 Bean Map
    private void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
        if (beanDefinition.isSingleton() && DisposableBeanAdapter.hasDestroyMethod(bean, beanDefinition)) {
            this.disposableBeans.put(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));

        }
    }


    //创建对象
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition) throws Throwable {
        Class classType = beanDefinition.getType();
        //使用无参构造器
        Constructor<?> constructor = classType.getDeclaredConstructor();
        return constructor.newInstance();
    }

    //get Bean
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Object bean;
        if (beanDefinitionMap.containsKey(beanName)) {
            if (beanDefinition.isSingleton()) {
                bean = getSingleton(beanName,true);
                //若三级缓存都没有，则需创建Bean，并加入一级缓存，移除三级缓存中的beanName
                if(bean == null)
                {
                    bean = createBean(beanName,beanDefinition);
                    //加入一级缓存
                    this.singletonObjects.put(beanName,bean);
                    this.earlySingletonObjects.remove(beanName);
                    this.singletonFactories.remove(beanName);
                }

//                bean = singletonObjects.get(beanName);
//                if (bean == null) {
//                    bean = createBean(beanName, beanDefinition);
//                    singletonObjects.put(beanName, bean);
//                }
                return bean;
            } else {
                return createBean(beanName, beanDefinition);
            }
        } else {
            throw new NullPointerException();
        }
    }

    //从缓存中获取单例Bean
    private Object getSingleton(String beanName, boolean allowEarlyReference) {
        //一级缓存:单例池
        Object bean = this.singletonObjects.get(beanName);
        if (bean == null) {
            // 二级缓存：提前创建的单例对象池
            bean = this.earlySingletonObjects.get(beanName);
            if (bean == null && allowEarlyReference) {
                // 三级缓存：单例工厂池
                ObjectFactory<?> objectFactory = this.singletonFactories.get(beanName);
                if (objectFactory != null) {
                    bean = objectFactory.getObject();
                    //将该Bean加入一级缓存
                    singletonObjects.put(beanName, bean);
                    //移出三级缓存
                    singletonFactories.remove(beanName);
                }
            }
        }
        return bean;
    }

    //依赖注入
    void populateBean(String beanName, BeanDefinition beanDefinition, Object bean) throws IllegalAccessException, InvocationTargetException {
        Class classType = beanDefinition.getType();

        //属性注入
        Field[] fields = classType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(NYAutowired.class)) {
                field.setAccessible(true);
                Object tmpBean = getBean(field.getName());
                field.set(bean, tmpBean);
            }
        }

        //方法注入
        Method[] methods = classType.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(NYAutowired.class)) {
                String paraName = method.getParameters()[0].getName();
                method.invoke(bean, getBean(paraName));
            }
        }
    }

    //初始化
    private Object initializeBean(String beanName, BeanDefinition beanDefinition, Object bean) {
        //Aware 回调
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }

        //初始化前
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
        }

        //初始化时
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        //初始化后
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * 创建所有 Bean 后处理器，放入 singletonObjects 容器中，并注册到 beanPostProcessorList
     * Bean 后处理器属于单例，提前创建好了并放入容器，所以 Bean 后处理器并不会重复创建
     * 在后续的 preInstantiateSingletons() 初始化单例中，会先从容器中获取，获取不到再创建
     */
    private void registerBeanPostProcessors() {

        //注册常用的 Bean 后处理器到 beanDefinitionMap 中
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setType(AnnotationAwareAspectJAutoProxyCreator.class);
        beanDefinition.setScope("singleton");
        beanDefinitionMap.put("internalAutoProxyCreator", beanDefinition);


        /*
          1. 从 beanDefinitionMap 中找出所有的 BeanPostProcessor
          2. 创建 BeanPostProcessor 放入容器
          3. 将创建的 BeanPostProcessor 注册到 beanPostProcessorList
          这里的写法：先注册的 BeanPostProcessor 会对后创建的 BeanPostProcessor 进行拦截处理，
          BeanPostProcessor 的创建走 bean 的生命周期流程
         */
        this.beanDefinitionMap.entrySet()
                .stream()
                .filter((entry) -> BeanPostProcessor.class.isAssignableFrom(entry.getValue().getType()))
                .forEach((entry) -> {
                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) getBean(entry.getKey());
                    this.beanPostProcessorList.add(beanPostProcessor);
                });
    }

    // 将扫描到的单例 bean 创建出来放到单例池中
    private void preInstantiateSingletons() {
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        });
    }

    //销毁方法
    public void close(){
        destroySingletons();
    }
    private void destroySingletons(){
        synchronized (disposableBeans){
            for (Object bean : disposableBeans.values()) {
                try {
                    ((DisposableBeanAdapter) bean).destroy();
                } catch (Exception e) {
                    System.out.println("销毁Bean出错，抛出异常："+ e);;
                }
            }
        }
        //清理缓存
        this.singletonObjects.clear();
        this.earlySingletonObjects.clear();
        this.singletonFactories.clear();
    }
    /**
     * 对外提供销毁 bean 的方法
     * @param beanName
     * @param bean
     */
    public void destroyBean(String beanName, Object bean) {
        new DisposableBeanAdapter(bean, beanName, null).destroy();
    }
    public void destroyBean(Object bean) {
        new DisposableBeanAdapter(bean, bean.getClass().getName(), null).destroy();
    }

}