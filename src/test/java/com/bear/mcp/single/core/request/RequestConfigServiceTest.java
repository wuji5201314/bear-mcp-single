package com.bear.mcp.single.core.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestConfigServiceTest {

    @Test
    void injectsServiceHeadersForXxxAdminRequests() {
        RequestConfigService service = service("http://xxx-admin:8080/", "shared-secret");
        Request.Builder builder = new Request.Builder().url("http://xxx-admin:8080/xxx-admin/api/classes");

        service.applyXxxAdminAuth(builder, "http://xxx-admin:8080/xxx-admin/api/classes");

        Request request = builder.build();
        assertEquals("true", request.header("X-Bear-Mcp-Request"));
        assertEquals("shared-secret", request.header("X-Bear-Mcp-Secret"));
    }

    @Test
    void doesNotLeakServiceSecretToOtherHosts() {
        RequestConfigService service = service("http://xxx-admin:8080", "shared-secret");
        Request.Builder builder = new Request.Builder().url("https://example.com/xxx-admin/api/classes");

        service.applyXxxAdminAuth(builder, "https://example.com/xxx-admin/api/classes");

        Request request = builder.build();
        assertNull(request.header("X-Bear-Mcp-Request"));
        assertNull(request.header("X-Bear-Mcp-Secret"));
    }

    @Test
    void doesNotInjectHeadersWhenSecretIsMissing() {
        RequestConfigService service = service("http://xxx-admin:8080", "");
        Request.Builder builder = new Request.Builder().url("http://xxx-admin:8080/xxx-admin/api/classes");

        service.applyXxxAdminAuth(builder, "http://xxx-admin:8080/xxx-admin/api/classes");

        Request request = builder.build();
        assertNull(request.header("X-Bear-Mcp-Request"));
        assertNull(request.header("X-Bear-Mcp-Secret"));
    }

    @Test
    void failsClearlyWhenEnvironmentPlaceholderIsMissing() {
        RequestConfigService service = service("http://xxx-admin:8080", "");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.resolveEnvironmentVariables("Bearer ${BEAR_MIRRORED_API_SECRET_NOT_SET}"));

        assertEquals("请求配置引用的环境变量未设置: BEAR_MIRRORED_API_SECRET_NOT_SET", exception.getMessage());
    }

    private RequestConfigService service(String baseUrl, String secret) {
        return new RequestConfigService(null, new ObjectMapper(), baseUrl, secret);
    }
}
