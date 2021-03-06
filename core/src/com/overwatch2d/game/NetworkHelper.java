package com.overwatch2d.game;

import com.badlogic.gdx.Gdx;

import java.net.*;
import java.util.ArrayList;

public class NetworkHelper implements Constants {
    private static InetAddress host;
    private static boolean isHost = false;
    private static MulticastSocket clientSocket = null;
    private static boolean isServer = false;

    static {
        try {
            int port = 1000 + (int)Math.floor(Math.random() * 7000);

            clientSocket = new MulticastSocket(port);

            System.out.println("Client socket initialized at :" + clientSocket.getLocalPort());
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void connect(String host, String name) {
        try {
            InetAddress hostAddress = InetAddress.getByName(host);

            NetworkHelper.host = hostAddress;

            NetworkHelper.clientSend(new Packet("CONNECT", new ConnectPacket(name)), hostAddress);
        }
        catch(Exception e) {}
    }

    public static Thread createClientReceiver() {
        Thread t = new Thread(() -> {
            while(true) {
                try {
                    byte[] bytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                    clientSocket.receive(packet);

                    Object rawData = Serialize.toObject(packet.getData());

                    Packet receivedPacket = (Packet)(rawData);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();

                    if(!receivedPacket.getType().equals("HERO_UPDATE")) {
                        System.out.println("[Client] Received " + receivedPacket.getType() + " from " + address.toString() + ":" + port);
                    }

                    switch(receivedPacket.getType()) {
                        case "PLAYER_LIST": {
                            ArrayList<Player> players = ((PlayerListPacket)receivedPacket.getPayload()).getPlayers();

                            JoinScreen.setPlayers(players);

                            break;
                        }
                        case "START_GAME": {
                            if(isServer) {
                                new Thread(() -> Gdx.app.postRunnable(() -> HostScreen.startGame())).start();
                            }
                            else {
                                new Thread(() -> Gdx.app.postRunnable(() -> JoinScreen.startGame())).start();
                            }

                            break;
                        }
                        case "CHANGE_TEAM": {
                            String name = ((ChangeTeamPacket)receivedPacket.getPayload()).getName();
                            int team = ((ChangeTeamPacket)receivedPacket.getPayload()).getTeam();

                            JoinScreen.changeTeam(name, team);
                            break;
                        }
                        case "HERO_SPAWN": {
                            String playername = ((HeroSpawnPacket)receivedPacket.getPayload()).getPlayername();
                            int heroType = ((HeroSpawnPacket)receivedPacket.getPayload()).getHeroType();

                            Gdx.app.postRunnable(() -> GameScreen.spawnPlayer(playername, heroType));

                            break;
                        }
                        case "HERO_UPDATE": {
                            HeroUpdatePacket p = ((HeroUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            float x = p.getX();
                            float y = p.getY();
                            float angle = p.getAngle();
                            int currentHP = p.getCurrentHP();
                            boolean isDead = p.isDead();
                            float timeToRespawn = p.getTimeToRespawn();

                            Gdx.app.postRunnable(() -> GameScreen.updateHero(name, x, y, angle, currentHP, isDead, timeToRespawn));

                            break;
                        }
                        case "PLAYER_UPDATE": {
                            PlayerUpdatePacket p = ((PlayerUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            int eliminations = p.getEliminations();
                            int deaths = p.getDeaths();

                            Gdx.app.postRunnable(() -> GameScreen.updatePlayer(name, eliminations, deaths));

                            break;
                        }
                        case "WORLD_UPDATE": {
                            WorldUpdatePacket w = ((WorldUpdatePacket)receivedPacket.getPayload());

                            int currentObjective = w.getCurrentObjective();
                            float objective1Capture = w.getObjective1Capture();
                            float objective2Capture = w.getObjective2Capture();
                            float gameTimer = w.getGameTimer();
                            boolean battleHasStarted = w.isBattleHasStarted();
                            float preparationDuration = w.getPreparationDuration();

                            Gdx.app.postRunnable(() -> GameScreen.updateWorld(currentObjective, objective1Capture, objective2Capture, gameTimer, battleHasStarted, preparationDuration));

                            break;
                        }
                        case "HERO_ANGLE_UPDATE": {
                            HeroAngleUpdatePacket h = ((HeroAngleUpdatePacket)receivedPacket.getPayload());

                            String name = h.getName();
                            float angle = h.getAngle();

                            Gdx.app.postRunnable(() -> GameScreen.updateHeroAngle(name, angle));

                            break;
                        }
                        case "PLAYER_INPUT_UPDATE": {
                            PlayerInputUpdatePacket p = ((PlayerInputUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            boolean WHold = p.isWHold();
                            boolean AHold = p.isAHold();
                            boolean SHold = p.isSHold();
                            boolean DHold = p.isDHold();

                            Gdx.app.postRunnable(() -> GameScreen.updatePlayerInput(name, WHold, AHold, SHold, DHold));

                            break;
                        }
                        case "PROJECTILE_SPAWN": {
                            ProjectileSpawnPacket p = ((ProjectileSpawnPacket)receivedPacket.getPayload());

                            float initialX = p.getInitialX();
                            float initialY = p.getInitialY();
                            float destX = p.getDestX();
                            float destY = p.getDestY();
                            int damage = p.getDamage();
                            String heroName = p.getHeroName();

                            Gdx.app.postRunnable(() -> GameScreen.spawnProjectile(initialX, initialY, destX, destY, damage, heroName));

                            break;
                        }
                        case "PROJECTILE_HELLFIRE_SPAWN": {
                            ProjectileHellfireSpawnPacket p = ((ProjectileHellfireSpawnPacket)receivedPacket.getPayload());

                            float initialX = p.getInitialX();
                            float initialY = p.getInitialY();
                            ArrayList<Float> destX = p.getDestX();
                            ArrayList<Float> destY = p.getDestY();
                            int damage = p.getDamage();
                            String heroName = p.getHeroName();

                            Gdx.app.postRunnable(() -> GameScreen.spawnHellfireProjectile(initialX, initialY, destX, destY, damage, heroName));

                            break;
                        }
                        case "HERO_FIRE_PRIMARY": {
                            HeroFirePrimary p = ((HeroFirePrimary)receivedPacket.getPayload());

                            String name = p.getName();
                            float x = p.getX();
                            float y = p.getY();

                            Gdx.app.postRunnable(() -> GameScreen.firePrimary(name, x, y));

                            break;
                        }
                    }
                } catch (Exception ioe) {
                    System.out.println("Client Error:");
                    System.out.println(ioe);
                }
            }
        });

        return t;
    }

    public static Thread createServerReceiver() {
        Thread t = new Thread(() -> {
            isServer = true;

            while(true) {
                try {
                    byte[] bytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                    MulticastSocket s = new MulticastSocket(PORT);

                    s.receive(packet);

                    s.close();

                    Object rawData = Serialize.toObject(packet.getData());

                    Packet receivedPacket = (Packet)(rawData);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();

                    if(!receivedPacket.getType().equals("HERO_UPDATE")) {
                        System.out.println("[Server] Received " + receivedPacket.getType() + " from " + address.toString() + ":" + port);
                    }

                    switch(receivedPacket.getType()) {
                        case "CONNECT": {
                            String name = ((ConnectPacket)receivedPacket.getPayload()).getName();

                            Overwatch2D.getServer().connectPlayer(name, address, port);
                            break;
                        }
                        case "CHANGE_TEAM": {
                            String name = ((ChangeTeamPacket)receivedPacket.getPayload()).getName();
                            int team = ((ChangeTeamPacket)receivedPacket.getPayload()).getTeam();

                            Overwatch2D.getServer().changeTeam(name, team);

                            break;
                        }
                        case "HERO_SPAWN": {
                            String name = ((HeroSpawnPacket)receivedPacket.getPayload()).getPlayername();
                            int heroType = ((HeroSpawnPacket)receivedPacket.getPayload()).getHeroType();

                            Overwatch2D.getServer().spawnHero(name, heroType);

                            break;
                        }
                        case "HERO_UPDATE": {
                            HeroUpdatePacket p = ((HeroUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            float x = p.getX();
                            float y = p.getY();
                            float angle = p.getAngle();
                            int currentHP = p.getCurrentHP();
                            boolean isDead = p.isDead();
                            float timeToRespawn = p.getTimeToRespawn();

                            Overwatch2D.getServer().updateHero(name, x, y, angle, currentHP, isDead, timeToRespawn);

                            break;
                        }
                        case "PLAYER_UPDATE": {
                            PlayerUpdatePacket p = ((PlayerUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            int eliminations = p.getEliminations();
                            int deaths = p.getDeaths();

                            Overwatch2D.getServer().updatePlayer(name, eliminations, deaths);

                            break;
                        }
                        case "WORLD_UPDATE": {
                            WorldUpdatePacket w = ((WorldUpdatePacket)receivedPacket.getPayload());

                            int currentObjective = w.getCurrentObjective();
                            float objective1Capture = w.getObjective1Capture();
                            float objective2Capture = w.getObjective2Capture();
                            float gameTimer = w.getGameTimer();
                            boolean battleHasStarted = w.isBattleHasStarted();
                            float preparationDuration = w.getPreparationDuration();

                            Overwatch2D.getServer().updateWorld(currentObjective, objective1Capture, objective2Capture, gameTimer, battleHasStarted, preparationDuration);

                            break;
                        }
                        case "HERO_ANGLE_UPDATE": {
                            HeroAngleUpdatePacket h = ((HeroAngleUpdatePacket)receivedPacket.getPayload());

                            String name = h.getName();
                            float angle = h.getAngle();

                            Overwatch2D.getServer().updateHeroAngle(name, angle);

                            break;
                        }
                        case "PLAYER_INPUT_UPDATE": {
                            PlayerInputUpdatePacket p = ((PlayerInputUpdatePacket)receivedPacket.getPayload());

                            String name = p.getName();
                            boolean WHold = p.isWHold();
                            boolean AHold = p.isAHold();
                            boolean SHold = p.isSHold();
                            boolean DHold = p.isDHold();

                            Overwatch2D.getServer().updatePlayerInput(name, WHold, AHold, SHold, DHold);

                            break;
                        }
                        case "PROJECTILE_SPAWN": {
                            ProjectileSpawnPacket p = ((ProjectileSpawnPacket)receivedPacket.getPayload());

                            float initialX = p.getInitialX();
                            float initialY = p.getInitialY();
                            float destX = p.getDestX();
                            float destY = p.getDestY();
                            int damage = p.getDamage();
                            String heroName = p.getHeroName();

                            Overwatch2D.getServer().spawnProjectile(initialX, initialY, destX, destY, damage, heroName);

                            break;
                        }
                        case "PROJECTILE_HELLFIRE_SPAWN": {
                            ProjectileHellfireSpawnPacket p = ((ProjectileHellfireSpawnPacket)receivedPacket.getPayload());

                            float initialX = p.getInitialX();
                            float initialY = p.getInitialY();
                            ArrayList<Float> destX = p.getDestX();
                            ArrayList<Float> destY = p.getDestY();
                            int damage = p.getDamage();
                            String heroName = p.getHeroName();

                            Overwatch2D.getServer().spawnHellfireProjectile(initialX, initialY, destX, destY, damage, heroName);

                            break;
                        }
                        case "HERO_FIRE_PRIMARY": {
                            HeroFirePrimary p = ((HeroFirePrimary)receivedPacket.getPayload());

                            String name = p.getName();
                            float x = p.getX();
                            float y = p.getY();

                            Overwatch2D.getServer().firePrimary(name, x, y);

                            break;
                        }
                    }
                } catch (Exception ioe) {
                    System.out.println("Server Error:");
                    System.out.println(ioe);
                }
            }
        });

        return t;
    }

    public static void clientSend(Packet p, InetAddress address) {
        try {
            DatagramPacket packet;
            byte buf[] = Serialize.toBytes(p);
            packet = new DatagramPacket(buf, buf.length, address, PORT);

            if(!p.getType().equals("HERO_UPDATE")) {
                System.out.println("[Client] Sending " + p.getType() + " (" + packet.getLength() +"B) to " + address);
            }

            clientSocket.send(packet);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static void serverSend(Packet p, InetAddress address, int port) {
        try {
            DatagramPacket packet;
            byte buf[] = Serialize.toBytes(p);

            packet = new DatagramPacket(buf, buf.length, address, port);

            MulticastSocket s = new MulticastSocket(PORT);

            if(!p.getType().equals("HERO_UPDATE")) {
                System.out.println("[Server] Sending " + p.getType() + " (" + packet.getLength() +"B) to " + address + ":" + port);
            }

            s.send(packet);

            s.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static InetAddress getHost() {
        return NetworkHelper.host;
    }

    public static void setIsHost(boolean isHost) {
        NetworkHelper.isHost = isHost;
    }

    public static boolean isHost() {
        return isHost;
    }
}
