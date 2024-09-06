package videogamedb.feeders;


import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class VideoGameDbFeeders extends Simulation {

    private HttpProtocolBuilder httpProtocol = http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    private FeederBuilder<String> idcsv = csv("data/gamedata.csv").circular();
    private FeederBuilder<Object> idjson = jsonFile("data/gamedata.json").circular();

    private ChainBuilder getGameByCsv =
            feed(idcsv)
                    .exec(
                            http("get game with id #{gameId}").get("/videogame/#{gameId}")
                    );
    private ChainBuilder getGameByJson =
            feed(idjson)
                    .exec(
                            http("get game with json id #{id}").get("/videogame/#{id}")
                    );


    private ScenarioBuilder scenarioBuilder = scenario("Get games scenario")
            .exec(getGameByCsv,getGameByJson);

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(3))).protocols(httpProtocol);
    }
}
