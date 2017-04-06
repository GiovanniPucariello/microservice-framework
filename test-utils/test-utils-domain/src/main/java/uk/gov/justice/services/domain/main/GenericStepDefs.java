package uk.gov.justice.services.domain.main;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.reflections.util.ConfigurationBuilder;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.reflections.util.ClasspathHelper.forClassLoader;

public class GenericStepDefs {
    private Stream<Object> events;
    private Class<?> clazz;
    private Object object;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert events == null;
    }

    @When("(.*) to a (.*) using (.*)")
    public void call_method_with_params(final String methodName, final String aggregate, final String fileName) throws Exception {
        createAggregate(aggregate);
        ObjectMapper mapper = mapper();
        Map argumentsMap = mapper.convertValue(mapper.readTree(json(fileName)), Map.class);
        List valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Method method = object.getClass().getMethod(methodName, paramsTypes(methodName));
        if (argumentsMap.size() == 0) {
            events = (Stream<Object>) method.invoke(object, null);
        } else {
            events = (Stream<Object>) method.invoke(object, methodArgs(valuesList, getAllEventNames(argumentsMap), mapper));
        }
    }

    @Then("the (.*)")
    public void new_recipe_event_generated(final String fileName) throws ClassNotFoundException,
            IOException, IllegalAccessException, InstantiationException {
        String message = json(fileName);
        ObjectMapper mapper = mapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final ArrayNode fromJson = (ArrayNode) mapper.readTree(message);
        final List fromEvents = events.collect(Collectors.toList());

        assertEquals(fromJson.size(), fromEvents.size());

        for (int index = 0; index < fromEvents.size(); index++) {
            String eventNameFromJson = fromJson.get(index).get("_metadata").path("name").asText();
            assertTrue(eventNameFromJson.equalsIgnoreCase(eventName(fromEvents.get(index))));
            removeMetaDataNode(fromJson, index);//remove metadata node to compare two json objects
            assertTrue(fromJson.get(index).equals(mapper.valueToTree(fromEvents.get(index))));
        }
    }

    private String json(String file) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/json/" + file + ".json")));
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

    private Object[] methodArgs(List valuesList, List<String> expectedEventNames, ObjectMapper mapper) throws Exception {
        Object[] objects = new Object[valuesList.size()];
        int objectIndexInJson = 0;
        for (int index = 0; index < valuesList.size(); index++) {
            if (valuesList.get(index) instanceof HashMap) {
                objects[index] = mapper.readValue(mapper.writeValueAsString(valuesList.get(index)),
                        classWithFullyQualifiedClassName(expectedEventNames.get(objectIndexInJson)));
                objectIndexInJson = objectIndexInJson + 1;
            } else {
                objects[index] = valuesList.get(index);
            }
        }
        return objects;
    }

    private static Class<?> classWithFullyQualifiedClassNameWithReflections(String className) {
        Class<?> clazz = null;
        for (final String packageName : findAllPackages()) {
            final String tentative = packageName + "." + StringUtils.capitalize(className);
            try {
                clazz = Class.forName(tentative);
            } catch (final ClassNotFoundException e) {
                continue;
            }
            break;
        }
        return clazz;
    }

    private static Class<?> classWithPackage(final String className) {
        System.out.println("Trying with Reflections");
        final Class<?> clazz = classWithFullyQualifiedClassNameWithPackages(className);
        if (clazz == null) {
            System.out.println("Trying Packages as reflections dint work");
            return classWithFullyQualifiedClassNameWithReflections(className);
        }
        System.out.println("Packages Worked");

        return clazz;
    }

    private static Set<String> findAllPackages() {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        //classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(Thread.currentThread().getContextClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(".*.aggregate.*"))
        );

        Set<Class<? extends Aggregate>> classes = reflections.getSubTypesOf(Aggregate.class);
        Set<String> packageNameSet = new TreeSet<String>();
        for (Class classInstance : classes) {
            packageNameSet.add(classInstance.getPackage().getName());
        }
        return packageNameSet;
    }

    private static Class classWithFullyQualifiedClassName(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            classWithPackage(className);
        }
        return clazz;
    }

    private static Class<?> classWithFullyQualifiedClassNameWithPackages(String className) {
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
    private void removeMetaDataNode(ArrayNode ls1, int index) {
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