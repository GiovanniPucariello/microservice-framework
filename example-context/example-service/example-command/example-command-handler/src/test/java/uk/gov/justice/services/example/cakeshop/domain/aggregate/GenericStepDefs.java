package uk.gov.justice.services.example.cakeshop.domain.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericStepDefs {
    private Stream<Object> events;
    private Class<?> clazz;
    private Object object;

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert events == null;
    }

    @When("(.*) is called on the (.*) with (.*)")
    public void call_method_with_params(final String methodName, final String aggregate, final String fileName) throws Exception {
        String message = json(fileName);
        createAggregate(aggregate);
        Class<?>[] pType = paramsTypes(methodName);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualNode = mapper.readTree(message);
        Map argumentsMap = mapper.convertValue(actualNode, Map.class);
        List valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Object[] methodArgs = methodArgs(valuesList, getAllEventNames(argumentsMap));

        Method method = object.getClass().getMethod(methodName, pType);
        if (argumentsMap.size() == 0) {
            events = (Stream<Object>) method.invoke(object, null);

        } else {
            events = (Stream<Object>) method.invoke(object, methodArgs);
        }
    }

    private String json(String file) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/json/" + file)));
    }

    private List<String> getAllEventNames(Map argumentsMap) {
        List<String> classNames = new ArrayList();
        Iterator it = argumentsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue() instanceof HashMap) {
                classNames.add((String) pair.getKey());
            }
        }
        return classNames;
    }

    private Object[] methodArgs(List valuesList, List<String> expectedEventNames) throws Exception {
        Object[] objects = new Object[valuesList.size()];
        int objectIndexInJson = 0;
        for (int index = 0; index < valuesList.size(); index++) {
            if (valuesList.get(index) instanceof HashMap) {
                objects[index] = instantiate(new ArrayList(((HashMap) valuesList.get(index)).values()), expectedEventNames.get(objectIndexInJson));
                objectIndexInJson = objectIndexInJson + 1;
            } else {
                objects[index] = valuesList.get(index);
            }
        }
        return objects;
    }


    static Object instantiate(List<String> args, String className) throws Exception {
        // Load the class.
        Class<?> clazz = classWithFullyQualifiedClassName(className);

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

    private static Class<?> classWithFullyQualifiedClassName(String className) {
        Class<?> clazz = null;
        final Package[] packages = Package.getPackages();

        for (final Package p : packages) {
            final String pack = p.getName();
            final String tentative = pack + "." + StringUtils.capitalize(className);
            try {
                clazz = Class.forName(tentative);

            } catch (final ClassNotFoundException e) {
                continue;
            }
            break;
        }
        return clazz;
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

    @Then("the events are generated with (.*)")
    public void new_recipe_event_generated(final String fileName) throws ClassNotFoundException,
            IOException, IllegalAccessException, InstantiationException {
        String message = json(fileName);
        ObjectMapper mapper = mapper();
        final ArrayNode fromJson = (ArrayNode) mapper.readTree(message);
        final List fromEvents = events.collect(Collectors.toList());

        assertEquals(fromJson.size(), fromEvents.size());

        for (int index = 0; index < fromEvents.size(); index++) {
            String eventNameFromJson = fromJson.get(index).get("_metadata").path("name").asText();
            assertTrue(eventNameFromJson.equalsIgnoreCase(eventName(fromEvents.get(index))));
            removeEventNameElement(fromJson, index);
            assertTrue(fromJson.get(index).equals(mapper.valueToTree(fromEvents.get(index))));
        }
    }

    private void removeEventNameElement(ArrayNode ls1, int index) {
        ObjectNode object = (ObjectNode) ls1.get(index);
        object.remove("_metadata");
    }

    private String eventName(Object obj) throws ClassNotFoundException {
        Class<?> cls = Class.forName(obj.getClass().getName());
        return cls.getAnnotation(Event.class).value();
    }

    private ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private void createAggregate(final String aggregate) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (clazz == null && object == null) {
            this.clazz = classWithFullyQualifiedClassName(aggregate);
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