package rest.read;

import core.JdbiSingleton;
import json.Marker;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import rest.RESTResponse;
import utils.SQL;
import utils.Utils;

import java.util.List;

import static utils.Utils.*;

public class SelectorByHierarchy {

    public static List<Marker> getResources(
            String country, String region, String county, String town, String road, Model model
    ) throws Exception {

        county = Utils.provincesFilter.filter(county);
        region = Utils.regionFilter.filter(region);

        Handle handler = JdbiSingleton.getInstance().open();

        String filters = createFilter(country, region, county, town, road);
//        Utils.println(filters);
        Query q = handler.select(SQL.selectMarkersQuery.apply(filters));

        if (!country.toLowerCase().equals(defaultCountry)) {
            q = q.bind("country", Utils.stringify(country));
        }

        if (!region.toLowerCase().equals(defaultRegion)) {
            q = q.bind("region", Utils.stringify(region));
        }

        if (!county.toLowerCase().equals(defaultCounty)) {
            q = q.bind("county", Utils.stringify(county));
        }

        if (!town.toLowerCase().equals(defaultTown)) {
            q = q.bind("town", Utils.stringify(town));
            q = q.bind("city", Utils.stringify(town));
        }

        if (!road.toLowerCase().equals(defaultRoad)) {
            q = q.bind("road", Utils.stringify(road));
        }

        List<Marker> res = resolveQuery(q);

        handler.close();

//        Utils.println(res.stream().map(r -> r.getCoordinates().toString()).collect(Collectors.toList()));

        return res;
    }
}
