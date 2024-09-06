package videogamedb.script;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class VideoGameDb extends Simulation {

    ChainBuilder allGames =
            repeat(2).on(
                    exec(http("get all games").get("/videogame").check(status().is(200))
                            .check(jmesPath("[].id").saveAs("allids"))
                            .check(jsonPath("$[?(@.id==1)].name").is("Resident Evil 4")

                            )
                    ));

    ChainBuilder getGame2 =

                    foreach( "#{idList}","elt").on(
                         exec(http("get game 2").get("/videogame/#{elt}").check(status().is(200))
                    )

                    );

    HttpProtocolBuilder httpProtocolBuilder = http.acceptHeader("application/json")
            .baseUrl("https://videogamedb.uk/api");

    ScenarioBuilder sb = scenario("Scenario with pauses")
            .exec(
                    allGames
            )
            .pause(5)
            .exec(http("get game 1").get("/videogame/1")
                    .check(status()
                            .in(200, 201, 202)))
            .pause(4)
            .exec(http("get all").get("/videogame")
                    .check(jmesPath("[]").saveAs("allgames"))
            )
            .exec(session -> {
                System.out.println("unbelievable " + session.get("allids"));

                List<String> allids = convertToList(session.get("allids"));
                Session session2 = session.set("idList",allids);
                return session2;
            })
            .exec(getGame2);

    private List<String> convertToList(String allids) {
        String csv = allids.replace("[", "")
                .replace("]", "");
        String[] split = csv.split(",");
        List<String> list = Arrays.asList(split);
        return list;


    }

    {
        setUp(sb.injectOpen(atOnceUsers(1))).protocols(httpProtocolBuilder);
    }

}
