package me.oriharel.playershops.utilities

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException

/**
 * made to simplify some reflection operations
 */
class ReflectionUtils {
    object Fields {
        /**
         * Easily get a field value
         *
         * @param instance  the instance of the object to get from
         * @param fieldName the field name
         * @param <R>       the type of the field
         * @return the value or null if not found
        </R> */
        fun <R> getFieldValueOfObject(instance: Any, fieldName: String?): R? {
            try {
                val field = instance.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                return field[instance] as R
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Easily get a field value. for cases with inheritance
         *
         * @param instance  the instance of the object to get from
         * @param fieldName the field name
         * @param <R>       the type of the field
         * @return the value or null if not found
        </R> */
        fun <R, T> getFieldValueOfObjectExact(instance: T, fieldName: String?): R? {
            try {
                val field = object : TypeToken<T>() {}.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                return field[instance] as R
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Easily get a field value. if your object is an interface, use this.
         *
         * @param instance  the instance of the object to get from
         * @param clazz     the class that implements the interface and has the field
         * @param fieldName the field name
         * @param <R>       the type of the field
         * @return the value or null if not found
        </R> */
        fun <R> getFieldValueOfUnknownClass(instance: Any?, clazz: Class<*>, fieldName: String?): R? {
            try {
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                return field[instance] as R
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Easily get a field value. if your object is an interface, use this.
         *
         * @param instance  the instance of the object to get from
         * @param className the class that implements the interface and has the field
         * @param fieldName the field name
         * @param <R>       the type of the field
         * @return the value or null if not found
        </R> */
        fun <R> getFieldValueOfUnknownClass(instance: Any?, className: String?, fieldName: String?): R? {
            try {
                val field = Class.forName(className).getDeclaredField(fieldName)
                field.isAccessible = true
                return field[instance] as R
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Get a Field
         *
         * @param instance  the instance of the object to get from
         * @param fieldName the field name
         * @return the value or null if not found
         */
        fun getField(instance: Any, fieldName: String?): Field? {
            try {
                return instance.javaClass.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Get a Field
         *
         * @param clazz     the class to get the field from
         * @param fieldName the field name
         * @return the value or null if not found
         */
        fun getField(clazz: Class<*>, fieldName: String?): Field? {
            try {
                return clazz.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Easily get a field value
         *
         * @param className the class name to get the field from. for package-private classes
         * @param fieldName the field name
         * @return the value or null if not found
         */
        fun getField(className: String?, fieldName: String?): Field? {
            try {
                return Class.forName(className).getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
    }

    object Methods {
        /**
         * Easily execute a method
         *
         * @param instance   the instance of the object to get from
         * @param methodName the method name
         * @param parameters the parameters of the method
         * @param <R>        the type of return value
         * @return the return value or null
        </R> */
        fun <R> executeMethod(instance: Any, methodName: String?, vararg parameters: Class<*>?): R? {
            try {
                val method = instance.javaClass.getDeclaredMethod(methodName, *parameters)
                method.isAccessible = true
                return method.invoke(instance) as R
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            return null
        }
    }
}