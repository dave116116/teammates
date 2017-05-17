package teammates.storage.entity;

import java.lang.reflect.Field;

import javax.jdo.annotations.PrimaryKey;

import com.googlecode.objectify.annotation.Id;

import teammates.common.util.Assumption;

/**
 * Base class for all entities persisted to the Datastore.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseEntity {
    protected static String getFieldWithPrimaryKeyAnnotation(Class<?> cls) {
        Field[] fs = cls.getDeclaredFields();
        for (Field f : fs) {
            if (f.isAnnotationPresent(PrimaryKey.class) || f.isAnnotationPresent(Id.class)) {
                return f.getName();
            }
        }
        Assumption.fail("There should be a field annotated with @PrimaryKey or @Id");
        return null;
    }
}