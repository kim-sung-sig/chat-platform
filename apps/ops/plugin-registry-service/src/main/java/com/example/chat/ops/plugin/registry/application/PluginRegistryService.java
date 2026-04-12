package com.example.chat.ops.plugin.registry.application;

import com.example.chat.ops.contract.plugin.PluginDescriptor;
import java.util.List;

public interface PluginRegistryService {
    PluginDescriptor register(PluginDescriptor plugin);

    List<PluginDescriptor> list();
}
