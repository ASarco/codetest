package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;

public class DBConnector {

  private static final String DB_PATH = "poller.db";
  private final SQLClient client;

  public DBConnector(Vertx vertx){
    JsonObject config = new JsonObject()
        .put("url", "jdbc:sqlite:" + DB_PATH)
        .put("max_pool_size", 20)
        .put("driver_class", "org.sqlite.JDBC");

    client = JDBCClient.createShared(vertx, config);


  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }


  public Future<ResultSet> query(String query, JsonArray params) {
    if(query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if(!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    client.queryWithParams(query, params, result -> {
      if(result.failed()){
        queryResultFuture.fail(result.cause());
      } else {
        queryResultFuture.complete(result.result());
      }
    });
    return queryResultFuture;
  }

  public Future<UpdateResult> update(String query, JsonArray params) {
    if (query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if (!query.endsWith(";")) {
      query = query + ";";
    }

    Future<UpdateResult> updateResultFuture = Future.future();

    client.updateWithParams(query, params, result ->{
      if(result.failed()){
        updateResultFuture.fail(result.cause());
      } else {
        updateResultFuture.complete(result.result());
      }
    });
    return updateResultFuture;
  }



  }
