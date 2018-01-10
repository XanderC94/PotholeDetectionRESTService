package prototype;

import json.Marker;
import json.Point;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.util.List;

public class TryJDBI {

    public static void main(String[] args) {

        Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:6666/potholes", "postgres", "pumpkins")
                .installPlugin(new PostgresPlugin());

        Handle handler = jdbi.open();

        final String area = "Riccione";

        List<Marker> res = handler.select(
                "SELECT Location_Detection AS LocDet, ST_AsGeoJSON(Coordinates)::json->'coordinates' AS Coordinates, N_Detections AS N_Det FROM markers"
        ).map((rs, ctx) -> new Marker(
                rs.getLong("N_Det"),
                new Point(rs.getString("Coordinates")),
                rs.getString("LocDet"))
        ).list();


        for (Marker m : res) {
            System.out.println(m.toString());
        }

    }
}
