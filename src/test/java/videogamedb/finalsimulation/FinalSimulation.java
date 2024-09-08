package videogamedb.finalsimulation;



import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class FinalSimulation extends Simulation {


    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            ;

    private ChainBuilder authenticate =

            exec (session -> session.set("username","admin")
                    .set("password","admin"))
                    .exec(
                            http("authenticate").post("/authenticate").body(
                                            ElFileBody("data/authbody.json")

                                    ).asJson()
                                    .check(jmesPath("token").saveAs("jwtToken"))
                    )
                    .exec(session -> {
                        String jwtToken = session.getString("jwtToken");
                        System.out.println("Token is " + jwtToken);
                        return session;
                    });

    FeederBuilder<String> gameCreateCsv = csv("data/gamedata.csv").circular();


    private ChainBuilder createGame =
            feed(gameCreateCsv)
                    .exec(
                            http("create game #{category}").post("/videogame")
                                    .header("authorization", "Bearer " + "#{jwtToken}")
                                    .body(
                                            ElFileBody("data/create.json")
                                    ).asJson()
                                    .check(jmesPath("id").saveAs("createdId"))
                    );

    private ChainBuilder deleteGame =
            exec(http("Delete game with id #{createdId}")

                    .delete("/videogame/1")
                    .header("authorization", "Bearer " + "#{jwtToken}")
            );


    ChainBuilder allGames =
             exec(http("Get all games").get("/videogame"));

     ScenarioBuilder sb = scenario("Final Scenario")
             .forever ().on(
             exec(allGames)
             .exec(authenticate)
             .exec(createGame)
             .exec(deleteGame)
             );


    @Override
    public void before() {
        super.before();
        System.out.println("Running final simulation");
    }

    {
        setUp(sb.injectOpen(rampUsers(5).during(5))).maxDuration(10).protocols(httpProtocol);
    }

    @Override
    public void after() {
        super.after();
        System.out.println("Final simulation ended");
    }
}
