package videogamedb.script;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MyFirstTest  extends Simulation{

    // 1. http

    private final HttpProtocolBuilder httpProtocol =  http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    // 2.scenario

    private final ScenarioBuilder scn = scenario("My First Test")
            .exec(http("get all games").get("/videogame"));

    // 3. Load simulation

    {
        setUp( scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
