package com.example.chat.ops.plugin.registry.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.contract.plugin.PluginDescriptor;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import com.example.chat.ops.plugin.registry.application.PluginRegistryService;
import com.example.chat.ops.plugin.registry.presentation.dto.RegisterPluginRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plugins")
public class PluginRegistryController {
    private final PluginRegistryService pluginRegistryService;

    public PluginRegistryController(PluginRegistryService pluginRegistryService) {
        this.pluginRegistryService = pluginRegistryService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PluginDescriptor>> register(
            @RequestHeader("X-Project-Role") String role,
            @Valid @RequestBody RegisterPluginRequest request
    ) {
        OpsAuthorization.require(role, OpsAction.PLUGIN_REGISTER);
        PluginDescriptor descriptor = pluginRegistryService.register(new PluginDescriptor(
                request.id(),
                request.name(),
                request.version(),
                request.supportedSources(),
                Instant.now()
        ));
        return ApiResponse.created(descriptor).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PluginDescriptor>>> list(
            @RequestHeader("X-Project-Role") String role
    ) {
        OpsAuthorization.require(role, OpsAction.AUDIT_READ);
        return ApiResponse.ok(pluginRegistryService.list()).toResponseEntity();
    }
}
