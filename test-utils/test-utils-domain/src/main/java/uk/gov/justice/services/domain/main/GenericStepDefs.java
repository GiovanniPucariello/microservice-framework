package uk.gov.justice.services.domain.main;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

public class GenericStepDefs {
    private static final String fmt = "%24s: %s%n";
    private Stream<Object> events;
    private Class<?> clazz;
    private Object object;

    static Object instantiate(List<String> args, String className) throws Exception {
        // Load the class.
        Class<?> clazz = Class.forName(className);

        // Search for an "appropriate" constructor.
        for (Constructor<?> ctor : clazz.getConstructors()) {
            Class<?>[] paramTypes = ctor.getParameterTypes();

            // If the arity matches, let's use it.
            if (args.size() == paramTypes.length) {

                // Convert the String arguments into the parameters' types.
                Object[] convertedArgs = new Object[args.size()];
                for (int i = 0; i < convertedArgs.length; i++) {
                    convertedArgs[i] = convert(paramTypes[i], args.get(i));
                }

                // Instantiate the object with the converted arguments.
                return ctor.newInstance(convertedArgs);
            }
        }

        throw new IllegalArgumentException("Don't know how to instantiate " + className);
    }

    static Object convert(Class<?> target, Object s) {
        if (target == Object.class || target == String.class || s == null) {
            return s;
        }
        if (target == Set.class || s == null) {
            return new HashSet((List) s);
        }
        if (target == List.class || s == null) {
            return s;
        }
        if (target == Character.class || target == char.class) {
            return ((String) s).charAt(0);
        }
        if (target == Byte.class || target == byte.class) {
            return Byte.parseByte((String) s);
        }
        if (target == Short.class || target == short.class) {
            return Short.parseShort((String) s);
        }
        if (target == Integer.class || target == int.class) {
            return Integer.parseInt((String) s);
        }
        if (target == Long.class || target == long.class) {
            return Long.parseLong((String) s);
        }
        if (target == Float.class || target == float.class) {
            return Float.parseFloat((String) s);
        }
        if (target == Double.class || target == double.class) {
            return Double.parseDouble((String) s);
        }
        if (target == Boolean.class || target == boolean.class) {
            return Boolean.parseBoolean((String) s);
        }
        if (target == UUID.class) {
            return UUID.fromString((String) s);
        }
        if (target == LocalDate.class) {
            return LocalDate.parse((String) s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        throw new IllegalArgumentException("Don't know how to convert to " + target);
    }

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert events == null;
    }

    @When("the method (.*) is called on the aggregate (.*)")
    public void add_new_receipe_2(final String methodName, final String aggregate, final String message) throws Exception {
        checkIfAggregateCreated(aggregate);
        Class<?>[] pType = paramsTypes(methodName);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualNode = mapper.readTree(message);
        Map argumentsMap = mapper.convertValue(actualNode, Map.class);
        List valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Object[] methodArgs = methodArgs(valuesList);

        Method method = object.getClass().getMethod(methodName, pType);
        if (argumentsMap.size() == 0) {
            events = (Stream<Object>) method.invoke(object, null);
        } else {
            events = (Stream<Object>) method.invoke(object, methodArgs);
        }
    }

    private Object[] methodArgs(List valuesList) throws Exception {
        Object[] objects = new Object[valuesList.size()];
        for (int index = 0; index < valuesList.size(); index++) {
            if (valuesList.get(index) instanceof HashMap) {
                //TODO get the class name dynamically from json
                objects[index] = instantiate(new ArrayList(((HashMap) valuesList.get(index)).values()), "uk.gov.moj.cpp.structure.domain.Suspect");
            } else {
                objects[index] = valuesList.get(index);
            }
        }
        return objects;
    }

    @Then("the events are generated with following data")
    public void new_recipe_event_generated(final String message) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualNode = mapper.readTree(message);
        List<String> expectedEvents = mapper.convertValue(actualNode.get("events"), List.class);
        final List<Object> eventList = events.collect(toList());
        MatcherAssert.assertThat(eventList.size(), CoreMatchers.is(expectedEvents.size()));
        Iterator it = mapper.convertValue(actualNode, Map.class).entrySet().iterator();
        for (int index = 0; index < expectedEvents.size(); index++) {
            final Object event = eventList.get(index);
            final String expectedEvent = expectedEvents.get(index);
            MatcherAssert.assertThat(event, CoreMatchers.instanceOf(Class.forName(expectedEvent)));
            for (Field field : event.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (field.getName().equalsIgnoreCase((String) pair.getKey())) {
                        MatcherAssert.assertThat(field.get(event).toString(), CoreMatchers.is(pair.getValue().toString()));
                        break;
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        }
    }

    private void checkIfAggregateCreated(final String aggregate) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (clazz == null && object == null) {
            this.clazz = Class.forName(aggregate);
            this.object = clazz.newInstance();
        }
    }

    private Class<?>[] paramsTypes(final String methodName) {
        Class<?>[] paramsTypes = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            paramsTypes = m.getParameterTypes();
        }
        return paramsTypes;
    }

    private void checkIfUUID(final List argumentValues) {
        for (int index = 0; index < argumentValues.size(); index++) {
            try {
                if (argumentValues.get(index) instanceof String) {
                    UUID uuid = UUID.fromString((String) argumentValues.get(index));
                    argumentValues.remove(index);
                    argumentValues.add(index, uuid);
                }
            } catch (IllegalArgumentException exception) {
                continue;
            }
        }
    }
}
