package rest.update;

import com.google.gson.reflect.TypeToken;
import core.JdbiSingleton;
import core.TokenManager;
import core.exceptions.AbsentTokenException;
import core.exceptions.DBQueryExecutionException;
import core.exceptions.WrongBodyDataException;
import json.CURequest;
import json.Tuple;
import json.UserFeedback;
import org.jdbi.v3.core.Handle;
import org.joda.time.DateTime;
import org.springframework.ui.Model;
import rest.RESTResponse;
import utils.SQL;
import utils.Utils;

import java.lang.reflect.Type;

public class FeedbackInserter {

    public static Tuple<Integer, String> addFeedback(Integer id, String body, Model model) throws Exception {

        final Type type = new TypeToken<CURequest<UserFeedback>>(){}.getType();
        final CURequest<UserFeedback> stub = Utils.gson.fromJson(body, type);

        final UserFeedback userFeedback = stub.getContent();
        final String registration = stub.getToken();

        if (id != userFeedback.getMarkerId()) {
            throw new WrongBodyDataException("Mismatch between PathVariable markerId ("+ id + ") and body markerID (" + userFeedback.getMarkerId() + ")");
        }

        if (TokenManager.getInstance().hasToken(registration)) {
            Handle handler = JdbiSingleton.getInstance().open();
            int res;

            if (!userFeedback.getText().isEmpty()) { // Add Comment
                try {
                    res = handler
                            .createUpdate(SQL.insertCommentToMarkerQuery)
                            .bind("marker_id", userFeedback.getMarkerId())
                            .bind("text", Utils.stringify(userFeedback.getText()))
                            .execute();

                } catch (Exception e) {
                    throw new DBQueryExecutionException("Unable to execute statement on the DB");
                }
            } else {
                throw new WrongBodyDataException("Empty Comment.");
            }

            handler.close();

            return new json.Tuple<>(res, userFeedback.getText());
        } else {
            throw new AbsentTokenException(registration);
        }
    }
}
