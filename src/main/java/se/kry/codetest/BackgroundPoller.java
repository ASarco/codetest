package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class BackgroundPoller {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundPoller.class);

    private final WebClient webClient = WebClient.create(Vertx.vertx());

    public Future<List<String>> pollServices(Map<Integer, Service> services) {
        LOG.debug("Polling!!! {}", services);

        for (Map.Entry<Integer, Service> entry : services.entrySet()) {
            Service service = entry.getValue();
            LOG.debug("Checking {}...", service.getName());
            webClient.head(80, service.getUrl(), "/").send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    if (response.statusCode() == 200) {
                        saveStatus(entry.getKey(), services, service, Status.OK);
                    } else {
                        saveStatus(entry.getKey(), services, service, Status.FAIL);
                    }
                } else {
                    LOG.warn("Check failed for {} due to {}", service.getName(), ar.cause().getMessage());
                    saveStatus(entry.getKey(), services, service, Status.FAIL);
                }
            });
            LOG.debug("Check Finished for {} with status {}", service.getName(), services.get(entry.getKey()).getStatus());
        }
        return Future.succeededFuture();
    }

    private Service saveStatus(Integer id, Map<Integer, Service> services, Service service, Status status) {
        return services.put(id, new Service(service.getUrl(), service.getName(), status, service.getAddedOn()));
    }
}
