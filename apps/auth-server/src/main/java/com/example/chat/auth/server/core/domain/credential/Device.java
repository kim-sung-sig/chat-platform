package com.example.chat.auth.server.core.domain.credential;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

    private String deviceId; // 브라우저나 앱에서 생성한 UUID
    private String platform; // WEB, ANDROID, IOS
    private String browser; // Chrome, Safari 등

    public boolean isWeb() {
        return "WEB".equalsIgnoreCase(platform);
    }

    public boolean isSameDevice(Device other) {
        if (other == null)
            return false;
        return Objects.equals(deviceId, other.deviceId)
                && Objects.equals(platform, other.platform)
                && Objects.equals(browser, other.browser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Device device = (Device) o;
        return Objects.equals(deviceId, device.deviceId)
                && Objects.equals(platform, device.platform)
                && Objects.equals(browser, device.browser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, platform, browser);
    }
}
