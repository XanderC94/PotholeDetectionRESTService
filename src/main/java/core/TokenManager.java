package core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import core.exceptions.AbsentTokenException;
import core.exceptions.AlreadyRegisteredException;
import core.exceptions.EmptyTokenException;
import core.exceptions.UninitializedServiceException;
import json.Registration;
import utils.Logging;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class TokenManager {

    private static TokenManager singleton;
    private static volatile boolean isInit = false;
    private File savefile;

    public static TokenManager getInstance(final File tokens) throws IOException {
        if (!isInit) {
            TokenManager.singleton = new TokenManager(tokens);
            isInit = true;
        }

        return TokenManager.singleton;
    }

    public static TokenManager getInstance() throws UninitializedServiceException {
        if (!isInit) {
            throw new UninitializedServiceException("Token Manager Service isn't initialized.");
        }

        return TokenManager.singleton;
    }

    private final Gson gson = new Gson();

    private ConcurrentHashMap<String, Set<Integer>> tokens;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TokenManager(final File tokens) throws IOException {

        this.savefile = tokens;

        try {
            BufferedReader br = new BufferedReader(new FileReader(this.savefile));
            Type type = new TypeToken<List<Registration>>(){}.getType();

            List<Registration> l = gson.fromJson(br, type);

            this.tokens = new ConcurrentHashMap<>(
                    l.parallelStream().collect(Collectors.toMap(Registration::getToken, Registration::getIds))
            );
        } catch (Exception e) {
            Logging.log("Previous Token File not found. Will start Empty and Create a new one.");

            this.savefile.getParentFile().mkdirs();
            this.savefile.createNewFile();

            this.tokens = new ConcurrentHashMap<>();
        }
    }

    public void addToken(final String token)
            throws EmptyTokenException, AlreadyRegisteredException {

        if (token.isEmpty()) {
            throw new EmptyTokenException("Given token is empty!");
        } else if (this.hasToken(token)) {
            throw new AlreadyRegisteredException(token);
        }
        this.tokens.put(token, new HashSet<>());

        update(token);
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

            update(token, id);

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

    private void writeToSaveFile() throws IOException {
        FileWriter saveWriter = new FileWriter(this.savefile);

        List<Registration> l = tokens.entrySet().parallelStream()
                .map(e -> new Registration(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        saveWriter.write(gson.toJson(l));
        saveWriter.close();
    }

    private void update(String token, Integer id) {
        executor.submit(() -> {
            try {
                writeToSaveFile();
                Logging.log(String.format("Saved id %s for token %s.", id, token));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void update(final String token) {
        executor.submit(() -> {
            try {
                writeToSaveFile();
                Logging.log(String.format("Saved token %s.", token));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void close() throws IOException, InterruptedException {
        executor.awaitTermination(5, TimeUnit.SECONDS);
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

        try {
            Logging.println(TokenManager.getInstance().tokens.get("09d75776b535ccdca39250ea8c1b071c"));
        } catch (UninitializedServiceException e) {
            e.printStackTrace();
        }

        try {
            TokenManager.getInstance().addToken("09d75776b535ccdca39250ea8c1b071c");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Logging.println(TokenManager.getInstance().tokens.get("09d75776b535ccdca39250ea8c1b071c"));
        } catch (UninitializedServiceException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                TokenManager.getInstance().register("09d75776b535ccdca39250ea8c1b071c", i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
