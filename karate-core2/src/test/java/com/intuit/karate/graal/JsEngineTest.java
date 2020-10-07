package com.intuit.karate.graal;

import com.intuit.karate.server.Request;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pthomas3
 */
class JsEngineTest {

    static final Logger logger = LoggerFactory.getLogger(JsEngineTest.class);

    JsEngine je;

    @BeforeEach
    void beforeEach() {
        je = JsEngine.global();
    }

    @AfterEach
    void afterEach() {
        JsEngine.remove();
    }

    @Test
    void testFunctionExecute() {
        JsValue v = je.eval("(function(){ return ['a', 'b', 'c'] })");
        assertTrue(v.isFunction());
        JsValue res = v.execute();
        assertTrue(res.isArray());
        String json = je.toJson(res);
        assertEquals("[\"a\",\"b\",\"c\"]", json);
        assertEquals("function(){ return ['a', 'b', 'c'] }", v.toString());
    }

    @Test
    void testFunctionVariableExecute() {
        je.eval("var add = function(a, b){ return a + b }");
        JsValue jv = je.eval("add(1, 2)");
        assertEquals(jv.getValue(), 3);
    }

    @Test
    void testJavaInterop() {
        je.eval("var SimplePojo = Java.type('com.intuit.karate.graal.SimplePojo')");
        JsValue sp = je.eval("new SimplePojo()");
        Value ov = sp.getOriginal();
        assertTrue(ov.isHostObject());
        SimplePojo o = ov.as(SimplePojo.class);
        assertEquals(null, o.getFoo());
        assertEquals(0, o.getBar());
    }

    @Test
    void testJsOperations() {
        je.eval("var foo = { a: 1 }");
        JsValue v = je.eval("foo.a");
        Object val = v.getValue();
        assertEquals(val, 1);
    }

    @Test
    void testMapOperations() {
        Map<String, Object> map = new HashMap();
        map.put("foo", "bar");
        map.put("a", 1);
        map.put("child", Collections.singletonMap("baz", "ban"));
        je.put("map", map);
        JsValue v1 = je.eval("map.foo");
        assertEquals(v1.getValue(), "bar");
        JsValue v2 = je.eval("map.a");
        assertEquals(v2.getValue(), 1);
        JsValue v3 = je.eval("map.child");
        assertEquals(v3.getValue(), Collections.singletonMap("baz", "ban"));
        JsValue v4 = je.eval("map.child.baz");
        assertEquals(v4.getValue(), "ban");
    }

    @Test
    void testListOperations() {
        je.eval("var temp = [{a: 1}, {b: 2}]");
        JsValue temp = je.eval("temp");
        je.put("items", temp.getValue());
        je.eval("items.push({c: 3})");
        JsValue items = je.eval("items");
        assertTrue(items.isArray());
        assertEquals(3, items.getAsList().size());
        je.eval("items.splice(0, 1)");
        items = je.eval("items");
        assertEquals(2, items.getAsList().size());
    }

    @Test
    void testRequestObject() {
        Request request = new Request();
        request.setMethod("GET");
        request.setPath("/index");
        Map<String, List<String>> params = new HashMap();
        params.put("hello", Collections.singletonList("world"));
        request.setParams(params);
        je.put("request", request);
        JsValue jv = je.eval("request.params['hello']");
        assertEquals(jv.getAsList(), Collections.singletonList("world"));
        jv = je.eval("request.param('hello')");
        assertEquals(jv.getValue(), "world");
    }
    
    @Test
    void testBoolean() {
        assertFalse(je.eval("1 == 2").isTrue());
        assertTrue(je.eval("1 == 1").isTrue());
    }

}