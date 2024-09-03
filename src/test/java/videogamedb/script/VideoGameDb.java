package videogamedb.script;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class VideoGameDb extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http.acceptHeader("application/json")
            .baseUrl("https://videogamedb.uk/api");

    ScenarioBuilder sb = scenario("Scenario with pauses")
            .exec(http("get all games").get("/videogame").check(status().is(200))
                    .check(jsonPath("$[?(@.id==1)].name").is("Resident Evil 4"))
            )
            .pause(5)
            .exec(http("get game 1").get("/videogame/1")
                    .check(status()
                            .in(200, 201, 202)))
            .pause(4)
            .exec(http("get all").get("/videogame"))
            .pause(Duration.ofMillis(4000));

    {
        setUp(sb.injectOpen(atOnceUsers(1))).protocols(httpProtocolBuilder);
    }

}
