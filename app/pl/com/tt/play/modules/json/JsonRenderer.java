/*
 * Copyright (c) 2012 Transition Technologies S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.com.tt.play.modules.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import play.db.jpa.JPABase;
import play.exceptions.TemplateNotFoundException;
import play.mvc.*;

/**
 * Play framework controller pointcut that will render JSON instead of template
 * if client passed "accept:application/json" header.
 * <p/>
 * This pointcut will extract arguments your controller has passed to html
 * template and render them as JSON instead.
 * <p/>
 * To use this class annotate your controller with
 * <code>@With(JsonRenderer.class)</code> and methods you want to expose via
 * json with {@link JsonEndpoint}. You can also annotate whole controller class
 * with {@link JsonEndpoint} to use it on all methods, but this doesn't allow
 * you to declare which template arguments to serialize (<strong>only ones
 * extending {@link JPABase} are serialized by default</strong>).
 * <p/>
 * <strong>Remember to add {@link Expose} annotation on all fields of classes
 * you want to be serialized to JSON. All other fields will be ignored</strong>.
 *
 * @author Marek Piechut <m.piechut@tt.com.pl>
 */
public class JsonRenderer extends Controller {

    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface JsonIgnore {
    }

    @After
    @Catch(TemplateNotFoundException.class)
    static void renderModelsAsJson(Throwable result) throws Throwable {
        JsonEndpoint classAnnotation = getControllerAnnotation(JsonEndpoint.class);
        JsonEndpoint methodAnnotation = getActionAnnotation(JsonEndpoint.class);

        boolean process = (methodAnnotation != null || classAnnotation != null) && isJsonRequested(result);

        if (process) {
            Map<String, Object> args = Scope.RenderArgs.current().data;
            Map<String, Object> outputObjects = new HashMap<String, Object>();
            if (methodAnnotation != null && methodAnnotation.output().length > 0) {
                String[] outputArgs = methodAnnotation.output();
                for (String argName : outputArgs) {
                    Object object = args.get(argName);
                    outputObjects.put(argName, object);
                }
            } else {
                for (Map.Entry<String, Object> entry : args.entrySet()) {
                    if (entry.getValue() instanceof JPABase) {
                        outputObjects.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeHierarchyAdapter(Collection.class, new RunTimeTypeCollectionAdapter());
            gsonBuilder.setExclusionStrategies(new JsonExclusionStrategy());
            Gson gson = gsonBuilder.create();
            String json = gson.toJson(outputObjects);
            renderJSON(json);
        }

        if (result != null) {
            throw result;
        }
    }

    @Util
    private static boolean isJsonRequested(Throwable result) {
        Http.Header accepts = request.headers.get("accept");
        boolean json = accepts != null && "application/json".equalsIgnoreCase(accepts.value());
        json = json || result instanceof TemplateNotFoundException && ((TemplateNotFoundException) result).getPath().endsWith(
                ".json");
        return json;
    }

    /**
     * Indicates that given controller method or all methods of controller
     * should be automatically exposed to json requests.
     * <p/>
     * You can annotate whole controller class with {@link JsonEndpoint} to use
     * it on all methods, but this doesn't allow you to declare which template
     * arguments to serialize (only ones extending {@link JPABase} are
     * serialized by default).
     * <p/>
     * Remember to add {@link Expose} annotation on all fields of classes you
     * want to be serialized to JSON. All other fields will be ignored.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface JsonEndpoint {

        /**
         * Template parameters to serialize to JSON (only ones extending
         * {@link JPABase} are serialized by default)
         * <p/>
         * These have to exactly match names of variables you pass to html
         * template in annotated controller method.
         */
        public String[] output() default {};
    }

    private static class RunTimeTypeCollectionAdapter implements JsonSerializer<Collection> {

        public JsonElement serialize(Collection src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            if (src == null) {
                return null;
            }
            JsonArray array = new JsonArray();
            for (Object child : src) {
                JsonElement element = context.serialize(child);
                array.add(element);
            }
            return array;
        }
    }

    private static class JsonExclusionStrategy implements ExclusionStrategy {

        public boolean shouldSkipField(FieldAttributes f) {
            boolean ignored = f.getAnnotation(JsonIgnore.class) != null;
            if (!ignored) {
                String className = f.getDeclaringClass().getName();
                ignored = JsonEndpointPlugin.CLASS_IGNORE_PATTERN.matcher(className).matches();
            }
            return ignored;
        }

        public boolean shouldSkipClass(Class<?> clazz) {
            JsonIgnore annotation = clazz.getAnnotation(JsonIgnore.class);
            return annotation != null || JsonEndpointPlugin.CLASS_IGNORE_PATTERN.matcher(clazz.getName()).matches();
        }
    }
}
