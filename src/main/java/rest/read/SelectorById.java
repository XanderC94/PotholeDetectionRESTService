package rest.read;

import core.JdbiInstanceManager;
import json.Marker;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import utils.SQL;
import utils.Utils;

import java.util.List;
import java.util.Optional;

public class SelectorById {

    public static Optional<Marker> getMarkerByUId(Integer id, Model model) throws Exception {
        String info;
        Handle handler = JdbiInstanceManager.getInstance().getConnector().open();

        Query q = handler.select(SQL.selectMarkerByUId).bind("marker_id", id);

        List<Marker> res = Utils.resolveQuery(q);

        handler.close();

        if (res.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(res.get(0));
        }
    }
}
