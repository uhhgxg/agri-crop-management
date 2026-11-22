package com.xxx.agri;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:agri-it")
class CropApiTest {

    private static final String RICE = """
            {"cropName":"水稻","varietyName":"试验稻一号","cropType":"粮食","suitArea":"长江中下游",
             "growthCycle":120,"yieldLevel":650.50,"plantStatus":1}
            """;
    private static final String TOMATO = """
            {"cropName":"番茄","varietyName":"试验番茄","cropType":"蔬菜","suitArea":"华北",
             "growthCycle":95,"yieldLevel":800,"plantStatus":0}
            """;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void createReadUpdateFilterAndLogicallyDelete() throws Exception {
        int initialTotal = request("GET", "/crop/list", null, 200).path("total").asInt();
        JsonNode created = request("POST", "/crop/add", RICE, 200);
        long id = created.path("id").asLong();
        assertThat(id).isPositive();
        assertThat(created.path("createTime").asText()).isNotBlank()
                .isEqualTo(created.path("updateTime").asText());
        assertThat(created.has("deleted")).isFalse();
        assertThat(request("GET", "/crop/" + id, null, 200)).isEqualTo(created);

        long tomatoId = request("POST", "/crop/add", TOMATO, 200).path("id").asLong();
        String update = RICE.replace("{", "{\"id\":" + id + ",").replace("120", "135");
        JsonNode updated = request("PUT", "/crop/update", update, 200);
        assertThat(updated.path("growthCycle").asInt()).isEqualTo(135);
        assertThat(updated.path("createTime").asText()).isEqualTo(created.path("createTime").asText());
        assertThat(updated.path("updateTime").asText()).isNotEqualTo(created.path("updateTime").asText());

        JsonNode page = request("GET", "/crop/list?pageNum=2&pageSize=1", null, 200);
        assertThat(page.path("total").asInt()).isEqualTo(initialTotal + 2);
        assertThat(page.path("pages").asInt()).isEqualTo(initialTotal + 2);
        assertThat(page.path("pageNum").asInt()).isEqualTo(2);
        assertThat(page.path("records").size()).isEqualTo(1);
        assertThat(page.path("records").get(0).path("id").asLong()).isEqualTo(id);

        String filters = "?cropName=%E7%A8%BB&varietyName=%E7%A8%BB%E4%B8%80"
                + "&cropType=%E7%B2%AE%E9%A3%9F&plantStatus=1";
        JsonNode filtered = request("GET", "/crop/list" + filters, null, 200);
        assertThat(filtered.path("total").asInt()).isEqualTo(1);
        assertThat(filtered.path("records").get(0).path("id").asLong()).isEqualTo(id);
        assertThat(request("GET", "/crop/list" + filters.replace("plantStatus=1", "plantStatus=0"),
                null, 200).path("total").asInt()).isZero();
        assertThat(request("GET", "/crop/list?cropName=&varietyName=%20&cropType=&plantStatus=",
                null, 200).path("total").asInt()).isEqualTo(initialTotal + 2);
        assertThat(request("GET", "/crop/list?plantStatus=0", null, 200)
                .path("records").get(0).path("id").asLong()).isEqualTo(tomatoId);

        assertThat(request("DELETE", "/crop/" + id, null, 200).isNull()).isTrue();
        request("GET", "/crop/" + id, null, 404);
        request("DELETE", "/crop/" + id, null, 404);
        request("PUT", "/crop/update", update, 404);
        assertThat(request("GET", "/crop/list" + filters, null, 200).path("total").asInt()).isZero();
        assertThat(jdbc.queryForObject("SELECT deleted FROM crop WHERE id = ?", Integer.class, id)).isEqualTo(1);
        request("DELETE", "/crop/" + tomatoId, null, 200);
    }

    @Test
    void returnUnifiedErrorsForInvalidInputAndUnexpectedDatabaseFailure() throws Exception {
        assertThat(response("POST", "/crop/add", RICE.replace("120", "0"), 400).path("msg").asText())
                .isEqualTo("生长周期不能小于1天");
        request("POST", "/crop/add", RICE.replace("120", "120.5"), 400);
        request("POST", "/crop/add", "{", 400);
        request("POST", "/crop/add", RICE.replace("{", "{\"id\":9,"), 400);
        request("PUT", "/crop/update", RICE, 400);
        request("GET", "/crop/list?pageNum=0&pageSize=101", null, 400);
        request("GET", "/crop/list?plantStatus=bad", null, 400);
        request("GET", "/crop/0", null, 400);
        request("GET", "/crop/999999", null, 404);
        request("PATCH", "/crop/1", null, 405);

        jdbc.execute("ALTER TABLE crop RENAME TO crop_unavailable");
        try {
            JsonNode result = response("GET", "/crop/list", null, 500);
            assertThat(result.path("msg").asText()).isEqualTo("服务器内部错误，请稍后重试");
        } finally {
            jdbc.execute("ALTER TABLE crop_unavailable RENAME TO crop");
        }
    }

    private JsonNode request(String method, String path, String body, int status) throws Exception {
        return response(method, path, body, status).path("data");
    }

    private JsonNode response(String method, String path, String body, int status) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }

        HttpResponse<String> response = client.send(request.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode()).as("%s %s body=%s", method, path, response.body()).isEqualTo(status);
        JsonNode result = json.readTree(response.body());
        assertThat(result.path("code").asInt()).isEqualTo(status);
        assertThat(result.path("msg").asText()).isNotBlank();
        assertThat(result.has("data")).isTrue();
        if (status >= 400) {
            assertThat(result.path("data").isNull()).isTrue();
        }
        return result;
    }
}
