package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<Integer, Service> services = new ConcurrentHashMap<>();
    //TODO use this
    private DBConnector connector;
    private final BackgroundPoller poller = new BackgroundPoller();

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        populateServices();
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        LOG.info("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void populateServices() {
        Future<ResultSet> query = connector.query("SELECT * FROM service");
        query.setHandler(rs -> {
            for (JsonArray line : rs.result().getResults()) {
                services.put(line.getInteger(0), new Service(line.getString(1), line.getString(2),
                        Status.UNKNOWN, LocalDateTime.parse(line.getString(3), DATE_TIME_FORMATTER)));
            }
        });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(getService);
        router.post("/service").handler(postService);
        router.delete("/service/:id").handler(deleteService);
    }


    private final Handler<RoutingContext> getService = req -> {
        List<JsonObject> jsonServices = services
                .entrySet()
                .stream()
                .map(service ->
                        new JsonObject()
                                .put("id", service.getKey())
                                .put("name", service.getValue().getName())
                                .put("url", service.getValue().getUrl())
                                .put("addedOn", service.getValue().getAddedOn().format(DATE_TIME_FORMATTER))
                                .put("status", service.getValue().getStatus()))
                .collect(Collectors.toList());
        req.response()
                .putHeader("content-type", "application/json")
                .end(new JsonArray(jsonServices).encode());
    };

    private final Handler<RoutingContext> postService = req -> {
        JsonObject jsonBody = req.getBodyAsJson();
        LocalDateTime addedOn = LocalDateTime.now();
        connector.update("INSERT INTO service (url, name, addedOn) VALUES(?, ?, ?)",
                new JsonArray().add(jsonBody.getString("url")).add(jsonBody.getString("name")).add(addedOn.format(DATE_TIME_FORMATTER)))
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOG.debug("Inserted {} row(s) with id={}", done.result().getUpdated(), done.result().getKeys().getInteger(0));
                        services.put(done.result().getKeys().getInteger(0), new Service(jsonBody.getString("url"),
                                jsonBody.getString("name"), Status.UNKNOWN, addedOn));
                    } else {
                        LOG.warn("Error saving service:", done.cause());
                    }
                });
        req.response()
                .putHeader("content-type", "text/plain")
                .end("OK");
    };


    private final Handler<RoutingContext> deleteService = req -> {
        String id = req.request().getParam("id");
        connector.update("DELETE FROM service WHERE id = ?", new JsonArray().add(id))
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOG.debug("Deleted service with id={}", id);
                        services.remove(Integer.parseInt(id));
                    } else {
                        LOG.warn("Error deleting service", done.cause());
                    }
                });
        req.response()
                .putHeader("content-type", "text/plain")
                .end("OK");
    };
}