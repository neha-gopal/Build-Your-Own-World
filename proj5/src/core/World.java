package core;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    private int width;
    private int height;
    private long seed;
    private Random random;
    private TETile[][] tiles;
    private List<Room> rooms;
    private Avatar avatar;
    private List<NPC> npcs;

    private static final int MIN_WIDTH = 4;
    private static final int MAX_WIDTH = 8;
    private static final int MIN_HEIGHT = 4;
    private static final int MAX_HEIGHT = 7;
    private static final int MIN_ROOMS = 8;
    private static final int MAX_ROOMS = 14;

    // build your own world!
    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.random = new Random(seed);
        this.tiles = new TETile[width][height];
        this.rooms = new ArrayList<>();
        this.npcs = new ArrayList<>();
    }

    public TETile[][] createWorld() {
        //fill grid with nothing tiles
        TETile[][] world = new TETile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        createRooms();
        placeAvatar();
        connectHallways();
        addWalls();
        placeNPCs();
        return tiles;

    }

    public TETile[][] getTiles() {
        return tiles;
    }

    public void createRooms() {
        int numRooms = RandomUtils.uniform(random, MIN_ROOMS, MAX_ROOMS + 1);
        int counter = 0; //counts the number of times a room is created
        int maxCount = numRooms * 20; //ensures program terminates after a reasonable number of room generations

        while (counter < maxCount && rooms.size() < numRooms) {
            counter++;
            int roomWidth = RandomUtils.uniform(random, MIN_WIDTH, MAX_WIDTH + 1);
            int roomHeight = RandomUtils.uniform(random, MIN_HEIGHT, MAX_HEIGHT + 1);

            // 1-tile border to place the wall tiles
            int x = RandomUtils.uniform(random, 1, width - roomWidth - 1);
            int y = RandomUtils.uniform(random, 1, height - roomHeight - 1);

            //adds the room if there's no overlap
            Room trialRoom = new Room(x, y, roomWidth, roomHeight);
            if (testRoom(trialRoom)) {
                setRoomTiles(trialRoom);
                rooms.add(trialRoom);

            }

        }
    }

    //test if room is valid (doesn't overlap with others)
    public boolean testRoom(Room trialRoom) {
        for (Room r : rooms) {
            if (trialRoom.overlaps(r)) {
                return false;
            }
        }
        return true;
    }

    public void setRoomTiles(Room room) {
        for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
            for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
                tiles[x][y] = Tileset.FLOOR;
            }
        }
    }


    private void connectHallways() {

        for (int i = 1; i < rooms.size(); i++) {
            Room first = rooms.get(i);
            int j = RandomUtils.uniform(random, 0, i);
            Room second = rooms.get(j);

            int firstX = first.centerX();
            int firstY = first.centerY();
            int secondX = second.centerX();
            int secondY = second.centerY();

            placeHallwayTile(firstX, firstY, secondX, secondY);
        }
    }

    private void placeHallStep(int x, int y) {
        if (tiles[x][y] == Tileset.NOTHING) {
            tiles[x][y] = Tileset.FLOOR;
        }
    }

    private void placeHallwayTile(int x1, int y1, int x2, int y2) {
        //picks random direction (horizontal or vertical)
        boolean horizontalFirst = random.nextBoolean();

        int x = x1;
        int y = y1;

        if (horizontalFirst) {
            //place hallway tiles till at second room x level
            while (x != x2) {
                tiles[x][y] = Tileset.FLOOR;
                x += Integer.compare(x2, x);
            }
            placeHallStep(x, y);

            //place hallway tiles till at second room y level
            while (y != y2) {
                tiles[x][y] = Tileset.FLOOR;
                y += Integer.compare(y2, y);
            }
            placeHallStep(x, y);
        } else {
            //opposite direction (vertical first)
            while (y != y2) {
                tiles[x][y] = Tileset.FLOOR;
                y += Integer.compare(y2, y);
            }
            placeHallStep(x, y);

            while (x != x2) {
                tiles[x][y] = Tileset.FLOOR;
                x += Integer.compare(x2, x);
            }
            placeHallStep(x, y);
        }
    }

    private void addWalls() {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    for (int xDisplacement  = -1; xDisplacement  <= 1; xDisplacement ++) {
                        for (int yDisplacement = -1; yDisplacement <= 1; yDisplacement++) {
                            int nextX = x + xDisplacement ;
                            int nextY = y + yDisplacement;
                            if (tiles[nextX ][nextY] == Tileset.NOTHING) {
                                tiles[nextX ][nextY] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    public NPC moveAvatar(char direction){
        int posX = avatar.getX();
        int posY = avatar.getY();

        switch (direction){
            case 'w':
                posY += 1;
                break;
            case 'a':
                posX -= 1;
                break;
            case 's':
                posY -= 1;
                break;
            case 'd':
                posX += 1;
                break;
        }
        if (isValidMove(posX, posY)) {
            tiles[avatar.getX()][avatar.getY()] = Tileset.FLOOR; // set prev location to floor
            avatar.setPosition(posX, posY);
            tiles[posX][posY] = Tileset.AVATAR;
        }
        return getNPCAt(posX, posY);
    }

    public void placeAvatar() {
        Room firstRoom = rooms.get(0);
        int startX = firstRoom.centerX();
        int startY = firstRoom.centerY();
        avatar = new Avatar(startX, startY);
        tiles[startX][startY] = Tileset.AVATAR;
    }

    private boolean isValidMove(int posX, int posY) {
        if (posX < 0 || posX >= width || posY < 0 || posY >= height) {
            return false;
        } else if (hasNPC(posX, posY)){
            return false;
        }
        return tiles[posX][posY] == Tileset.FLOOR;
    }

    private boolean hasNPC(int posX, int posY){
        for (NPC npc : npcs){
            if (npc.getX() == posX && npc.getY() == posY){
                return true;
            }
        }
        return false;
    }

    private NPC getNPCAt(int posX, int posY) {
        for (NPC npc : npcs) {
            if (npc.getX() == posX && npc.getY() == posY) {
                return npc;
            }
        }
        return null;
    }

    public void placeNPCs() {
        String[] npcNames = {"Villager", "Wizard", "Goblin"};

        int numNPCs = Math.min(npcNames.length, rooms.size() - 1);
        for (int i = 0; i < numNPCs; i++) {
            Room room = rooms.get(i + 1);
            int x = room.centerX();
            int y = room.centerY();

            NPC npc = new NPC(x, y, npcNames[i]);
            npcs.add(npc);
            tiles[x][y] = Tileset.NPC;
        }
    }
}


