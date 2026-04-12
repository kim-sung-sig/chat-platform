package com.example.chat.ops.plugin.registry.application;

import com.example.chat.ops.contract.error.OpsErrorCode;
import com.example.chat.ops.contract.error.OpsException;
import com.example.chat.ops.contract.plugin.PluginDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class InMemoryPluginRegistryService implements PluginRegistryService {
    private final ConcurrentMap<String, PluginDescriptor> plugins = new ConcurrentHashMap<>();

    @Override
    public PluginDescriptor register(PluginDescriptor plugin) {
        PluginDescriptor existing = plugins.putIfAbsent(plugin.id(), plugin);
        if (existing != null) {
            throw new OpsException(OpsErrorCode.PLUGIN_ALREADY_REGISTERED);
        }
        return plugin;
    }

    @Override
    public List<PluginDescriptor> list() {
        return new ArrayList<>(plugins.values());
    }
}
