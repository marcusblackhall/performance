package videogamedb.feeders;


import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class VideoGameDbFeeders extends Simulation {

    private HttpProtocolBuilder httpProtocol = http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    private FeederBuilder<String> idcsv = csv("data/gamedata.csv").circular();
    private FeederBuilder<Object> idjson = jsonFile("data/gamedata.json").circular();

    private Iterator<Map<String, Object>> customIterator =
            Stream.generate((Supplier<Map<String, Object>>) () -> {
                Random random = new Random();
                int nr = random.nextInt(10) + 1;
                return Collections.singletonMap("customgameId", nr);
            }).iterator();

    private Iterator<Map<String, Object>> customCreateIterator =
            Stream.generate((Supplier<Map<String, Object>>) () -> {
                Random random = new Random();
                int nr = random.nextInt(10) + 1;
                HashMap<String,Object> obj = new HashMap<>();
                obj.put("category","mycat");
                obj.put("name","myname");
                obj.put("releaseDate", LocalDate.now().toString());
                obj.put("reviewScore", 5);

                return obj;


            }).iterator();



    private ChainBuilder authenticate =

                    exec(
                            http("authenticate").post("/authenticate").body(StringBody("{\n" +
                                            "  \"password\": \"admin\",\n" +
                                            "  \"username\": \"admin\"\n" +
                                            "}"))
                                    .check(jmesPath("token").saveAs("jwtToken"))
                    )
                    .exec( session -> {
                        String jwtToken = session.getString("jwtToken");
                        System.out.println("Token is " + jwtToken );
                        return session;
                    });
            ;

    private ChainBuilder getGameByCsv =
            feed(idcsv)
                    .exec(
                            http("get game with id #{gameId}").get("/videogame/#{gameId}")
                    );
    private ChainBuilder getGameByJson =
            feed(idjson)
                    .exec(
                            http("get game with name #{name}").get("/videogame/#{id}")
                    );

    private ChainBuilder getGameByCustom =
            feed(customIterator)
                    .exec(
                            http("get game with custom id #{customgameId}").get("/videogame/#{customgameId}")
                    );

    private ChainBuilder createGame =
            feed(customCreateIterator)
                    .exec(
                            http("create game #{category}").post("/videogame")
                                    .header("authorization","Bearer " + "#{jwtToken}")
                                    .body(
                                    ElFileBody("data/create.json")
                            ).asJson()
                    );


    private ScenarioBuilder scenarioBuilder = scenario("Get games scenario")
            .exec(authenticate)
            .repeat(10).on(
                    exec(getGameByCsv, getGameByJson, getGameByCustom,createGame)
            );

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
