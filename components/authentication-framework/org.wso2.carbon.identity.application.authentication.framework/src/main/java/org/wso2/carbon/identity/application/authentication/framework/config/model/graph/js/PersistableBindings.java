package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;

/**
 * Javascript context data bindings, which allow us to transfer or save the javascript evaluation results via DB.
 */
public class PersistableBindings implements Bindings, Serializable {
    private static final Log logger = LogFactory.getLog(PersistableBindings.class);

    private transient HashMap<String, Object> liveMap = new HashMap<>();
    private HashMap<String, Object> persistableMap = new HashMap<>();

    public PersistableBindings() {
    }

    public PersistableBindings(Map<String, Object> initialMap) {
        this.liveMap.putAll(initialMap);
    }

    public PersistableBindings(Bindings initialMap) {
        this.liveMap.putAll(initialMap);
    }

    @Override
    public Object put(String name, Object value) {
        return liveMap.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        liveMap.putAll(toMerge);
    }

    @Override
    public void clear() {
        liveMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return liveMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return liveMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return liveMap.entrySet();
    }

    @Override
    public int size() {
        return liveMap.size();
    }

    @Override
    public boolean isEmpty() {
        return liveMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return liveMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return liveMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return liveMap.get(key);
    }

    @Override
    public Object remove(Object key) {
        return liveMap.remove(key);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        for (Map.Entry<String, Object> entry : liveMap.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (key.equals("nashorn.global")) {
                Object values = translate(value);
                persistableMap.put(key, values);
            } else if (value instanceof ScriptObjectMirror) {
                ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
                String val = scriptObjectMirror.toString();
                SerializedScriptObject serializedScriptObject = new SerializedScriptObject(val, null);
                persistableMap.put(key, serializedScriptObject);
            } else {
                logger.error("Non serialized javascript entity: " + key + ", value : " + value);
            }
        }
        out.writeObject(persistableMap);
    }

    private Object translate(Object value) {
        Object result = null;
        if (value instanceof Map) {
            HashMap resultMap = new HashMap();
            result = resultMap;
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                resultMap.put(entry.getKey(), translate(entry.getValue()));
            }
        } else if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
            String val = scriptObjectMirror.toString();
            SerializedScriptObject serializedScriptObject = new SerializedScriptObject(val, null);
            result = serializedScriptObject;
        }
        return result;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        persistableMap = (HashMap<String, Object>) in.readObject();
        liveMap = new HashMap<>();
        liveMap.putAll(persistableMap);
    }

    private class SerializedScriptObject implements Serializable {

        private String value;
        private String type;

        public SerializedScriptObject(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }
}
