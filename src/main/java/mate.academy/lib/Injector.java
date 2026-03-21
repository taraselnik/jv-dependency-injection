package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = getImplClass(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Class does not exist. Can't create instance of "
                    + interfaceClazz.getName());
        }

        if (clazz.isAnnotationPresent(Component.class)) {
            clazzImplementationInstance = createInstance(clazz);
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = injector.getInstance(field.getType());

                clazzImplementationInstance = createInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createInstance(clazz);
            }
        }
        return clazzImplementationInstance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getImplClass(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpls = Map.of(
                ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
        return interfaceImpls.get(interfaceClazz);
    }
}
