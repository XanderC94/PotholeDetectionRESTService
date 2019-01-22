package rest.update;

import com.google.gson.reflect.TypeToken;
import core.JdbiSingleton;
import core.TokenManager;
import core.exceptions.WrongBodyDataException;
import json.CURequest;
import json.Upvote;
import org.jdbi.v3.core.Handle;
import org.joda.time.DateTime;
import org.springframework.ui.Model;
import utils.SQL;
import utils.Utils;

import java.lang.reflect.Type;

public class UpvoteUpdater {

    public static Integer addUpvote(Integer id, String body, Model model) throws Exception {

        final Type type = new TypeToken<CURequest<Upvote>>(){}.getType();
        final CURequest<Upvote> stub = Utils.gson.fromJson(body, type);

        final Upvote upvote = stub.getContent();

        Handle handler = JdbiSingleton.getInstance().open();

        if (id != upvote.getMarkerId()) {
            throw new WrongBodyDataException("Mismatch between PathVariable markerId ("+ id + ") and body markerID (" + upvote.getMarkerId() + ")");
        }

        TokenManager.getInstance().register(stub.getToken(), upvote.getMarkerId());

        int res = handler.createUpdate(SQL.upvoteMarkerQuery)
                .bind("marker_id", upvote.getMarkerId()).execute();

        handler.close();

        return res;

    }
}
