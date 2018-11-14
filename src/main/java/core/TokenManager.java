package core;

import utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {

    static class EmptyTokenException extends Exception {

        public EmptyTokenException(String message) {
            super(message);
        }
    }

    static class AlreadyRegisteredException extends Exception {

        public AlreadyRegisteredException(String token) {
            super(String.format("Token %s already registered!", token));
        }
        public AlreadyRegisteredException(String token, Integer id) {
            super(String.format("Token %s already registered for ID %s!", token, id));
        }
    }

    static class AbsentTokenException extends Exception {
        public AbsentTokenException(String token) {
            super(String.format("Token %s is absent or unregistered. Please Register the token first.", token));
        }
    }

    private static TokenManager singleton = new TokenManager();

    public static TokenManager getInstance() {
        return TokenManager.singleton;
    }

    private ConcurrentHashMap<String, Set<Integer>> tokens;

    private TokenManager() {
        this.tokens = new ConcurrentHashMap<>();
    }

    public void addToken(final String token)
            throws EmptyTokenException, AlreadyRegisteredException {

        if (token.isEmpty()) {
            throw new EmptyTokenException("Given token is empty!");
        } else if (this.hasToken(token)) {
            throw new AlreadyRegisteredException(token);
        }
        this.tokens.put(token, new HashSet<>());
    }

    public boolean hasToken(final String token) {
        return this.tokens.containsKey(token);
    }

    public void register(final String token, final Integer id)
            throws AlreadyRegisteredException, AbsentTokenException {

        if (this.hasRegistered(token, id)){
            throw new AlreadyRegisteredException(token, id);
        } else if (this.hasToken(token)) {
            this.tokens.get(token).add(id);
        } else {
            throw new AbsentTokenException(token);
        }
    }

    public boolean hasRegistered(final String token, final Integer id) {

        if (this.hasToken(token)) {
            return this.tokens.get(token).contains(id);
        } else {
            return false;
        }
    }

    public static void main(String[] args) {

        try {
            TokenManager.getInstance().addToken("09d75776b535ccdca39250ea8c1b071c");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                TokenManager.getInstance().register("09d75776b535ccdca39250ea8c1b071c", i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Utils.println(TokenManager.getInstance().tokens.get("09d75776b535ccdca39250ea8c1b071c"));

        try {
            TokenManager.getInstance().addToken("09d75776b535ccdca39250ea8c1b071c");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.println(TokenManager.getInstance().tokens.get("09d75776b535ccdca39250ea8c1b071c"));

        for (int i = 0; i < 10; ++i) {
            try {
                TokenManager.getInstance().register("09d75776b535ccdca39250ea8c1b071c", i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
