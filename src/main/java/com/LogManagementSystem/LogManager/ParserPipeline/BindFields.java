package com.LogManagementSystem.LogManager.ParserPipeline;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class BindFields {


    // This is called Reflection API and is used to bind fields have same name as key in classes

    public <T> void bindRemainingFields(Map<String, Object> map, T instance){
        Class<?> clazz = instance.getClass();
        Set<String> presentFields = new HashSet<>();
        for(Map.Entry<String, Object> entry : map.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();

            try {
                Field field = clazz.getDeclaredField(key); // get field with same name
                field.setAccessible(true);   // allow access (even private)

                Object current = field.get(instance);

                // only set if not already filled
                if(current == null || (current instanceof Number && ((Number) current).intValue() == 0)){
                    field.set(instance, value);
                    presentFields.add(key);
                }
            } catch (NoSuchFieldException e) {
//                throw new RuntimeException(e);
                // if entity doesn't have this field, ignore
            } catch (IllegalAccessException e) {
                // access modifier issue, ignore
//                throw new RuntimeException(e);
            } catch (Exception e){
                // in-case field type is mismatched
            }
        }
        for(String key : presentFields){
            map.remove(key);
        }
    }

}
