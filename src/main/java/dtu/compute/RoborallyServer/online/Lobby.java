package dtu.compute.RoborallyServer.online;

import dtu.compute.RoborallyServer.RoboRallyServer;
import dtu.compute.RoborallyServer.fileaccess.model.GameTemplate;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Lobby {
    private final String id;
    private boolean inGame = false;
    private RoboRallyServer gameServer = null;
    private Thread gameThread;

    private ArrayList<String> players = new ArrayList<>();

    public Lobby(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public int addPlayer(String name) {
        if (players.size() >= 6) return -1;
        if (players.contains(name)) return -2;
        players.add(name);
        return 0;
    }

    public boolean lobbyFull() {
        return players.size() >= 6;
    }

    public void removePlayer(String playerName) {
        players.remove(playerName);
    }

    public boolean startGame(String mapName) {
        if (players.size() < 2 || players.size() > 6) return false;
        inGame = true;

        gameThread = new Thread(() -> {
            gameServer = new RoboRallyServer(this);
            gameServer.createGame(players, mapName);
            gameServer.startGameLoop();
        });
        gameThread.start();

        return true;
    }

    public boolean loadGame(GameTemplate gameState) {
        if (players.size() != gameState.players.size()) return false;
        for (int i = 0; i < gameState.players.size(); i++) {
            if (!players.contains(gameState.players.get(i).name)) return false;
        }
        inGame = true;

        gameThread = new Thread(() -> {
            gameServer = new RoboRallyServer(this);
            gameServer.loadGame(gameState);
            gameServer.startGameLoop();
        });
        gameThread.start();

        return true;
    }

    public void stopGame() {
        gameServer = null;
        inGame = false;
        gameThread.interrupt();
    }
}
