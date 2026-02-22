package com.example.chat.auth.server.core.domain.credential;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Device {
    private String deviceId;
    private String platform;
    private String browser;

    public boolean isWeb() {
        return "WEB".equalsIgnoreCase(platform);
    }

    public boolean isSameDevice(Device other) {
        if (other == null)
            return false;
        return Objects.equals(deviceId, other.deviceId) &&
                Objects.equals(platform, other.platform) &&
                Objects.equals(browser, other.browser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Device device))
            return false;
        return Objects.equals(deviceId, device.deviceId) &&
                Objects.equals(platform, device.platform) &&
                Objects.equals(browser, device.browser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, platform, browser);
    }
}
