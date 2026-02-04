package uk.co.bbc;

import io.vertx.core.*;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.*;

public class Main extends VerticleBase {
    @Override
    public Future<?> start() {
        Verticle pollVerticle = new FetchVerticle();

        DeploymentOptions deptOpts = new DeploymentOptions().setConfig(config()).setThreadingModel(ThreadingModel.WORKER);

        return vertx.deployVerticle(pollVerticle, deptOpts);
    }
}

class FetchVerticle extends AbstractVerticle {
    private HttpClient httpClient;

    @Override
    public void start(Promise<Void> startPromise) {
        this.httpClient = vertx.createHttpClient(
                new HttpClientOptions()
                        .setDecompressionSupported(true)
                        .setKeepAlive(false)
        );

        fetch().onComplete(res -> {
            if (res.succeeded()) {
                System.out.println("fetch succeeded");
            } else {
                System.out.println("fetch failed:");
                res.cause().printStackTrace();
            }

            vertx.close();
        });

        startPromise.complete();
    }

    private Future<Void> fetch() {
        System.out.println("start fetch");

        OpenOptions crops = new OpenOptions()
                .setRead(false)
                .setWrite(true)
                .setCreate(true)
                .setTruncateExisting(true);

        return vertx.fileSystem().open(System.getProperty("java.io.tmpdir").concat("/poll-verticle-output-file"), crops)
                .compose(file -> httpClient.request(HttpMethod.GET, 8889, "127.0.0.1", "/data")
                        .compose(HttpClientRequest::send)
                        .expecting(HttpResponseExpectation.SC_OK)
                        .compose(response -> response.pipe().to(file))
                        .onFailure(t -> System.out.println("Streaming response to file failed"))
                );
    }
}