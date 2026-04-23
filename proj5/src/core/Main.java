package core;

import tileengine.TERenderer;
import tileengine.TETile;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.Color;
import utils.FileUtils;

public class Main {

    private static final int WIDTH = 70;
    private static final int HEIGHT = 32;
    private static final int HUD_ROWS = 3;
    private static final long SEED = 7162790311120124118L; //change seed
    private static final String SAVE_FILE = "save.txt";
    private static StringBuilder history = new StringBuilder();
    private static String currentDialogue = "";
    private static boolean inDialogue = false;
    private static NPC currentNPC = null;
    private static String[] currentOptions = null;

    public static void main(String[] args) {

        // build your own world!
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        AudioManager.playMenuMusic();
        StdDraw.setPenColor(Color.WHITE);
        double curr_x = WIDTH / 2.0;
        double curr_y = HEIGHT * 0.75;
        StdDraw.text(curr_x, curr_y, "61B: BYOW");
        curr_y -= 4;
        StdDraw.text(curr_x, curr_y, "New Game (N)");
        curr_y -= 2;
        StdDraw.text(curr_x, curr_y, "Load Game (L)");
        curr_y -= 2;
        StdDraw.text(curr_x, curr_y, "Quit (Q)");
        StdDraw.show();

        char c; // Variable for saving the most recent character typed by the user.
        World world;
        TETile[][] tiles;

        while (true) {
            while (StdDraw.hasNextKeyTyped()) {
                c = StdDraw.nextKeyTyped();
                c = Character.toLowerCase(c);
                switch (c) {
                    case 'n':
                        String seedString = "";

                        StdDraw.clear(Color.BLACK);
                        StdDraw.setPenColor(Color.WHITE);
                        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.75, "Enter seed followed by S:");
                        StdDraw.show();
                        while (true) {
                            if (StdDraw.hasNextKeyTyped()) {
                                c = StdDraw.nextKeyTyped();
                                if (c == 's' || c == 'S') {
                                    // Done entering seed - start the game
                                    break;
                                } else if (Character.isDigit(c)) {
                                    seedString += c;

                                    StdDraw.clear(Color.BLACK);
                                    StdDraw.setPenColor(Color.WHITE);
                                    StdDraw.text(WIDTH / 2.0, HEIGHT * 0.75, "Enter seed followed by S:");
                                    StdDraw.setPenColor(Color.YELLOW);
                                    StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, seedString);
                                    StdDraw.show();
                                }
                            }
                        }

                        long seed = Long.parseLong(seedString);
                        world = newGame(seed);
                        tiles = world.createWorld();

                        AudioManager.stopAllMusic();
                        AudioManager.playGameMusic();

                        runGame(world, ter, tiles);
                        break;
                    case 'l':
                        world = loadGame();
                        if(world == null) {
                            break;
                        }
                        tiles = world.getTiles();
                        AudioManager.stopAllMusic();
                        AudioManager.playGameMusic();
                        runGame(world, ter, tiles);
                        break;
                    case 'q':
                        AudioManager.stopAllMusic();
                        System.exit(0); // Closes the game window and quits the game.
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public static World newGame(long seed){
        history = new StringBuilder();
        history.append('n').append(seed).append('s');
        World randWorld = new World(WIDTH, HEIGHT-HUD_ROWS, seed);
        return randWorld;
    }


    public static World loadGame(){
        if (!FileUtils.fileExists(SAVE_FILE)) {
            return null;
        }
        String saved = FileUtils.readFile(SAVE_FILE);
        history = new StringBuilder(saved);  // resume history from file
        return replayFromHistory(saved);
    }

    public static void saveGame(){
        FileUtils.writeFile(SAVE_FILE, history.toString());
    }

    private static World replayFromHistory(String input) {
        String s = input.toLowerCase();
        int i = 0;
        if (s.length() == 0) {
            return newGame(SEED);
        }

        char first = s.charAt(i);
        if (first != 'n') {
            return newGame(SEED);
        }

        i++;
        StringBuilder oldSeed = new StringBuilder();
        while (i < s.length() && s.charAt(i) != 's') {
            oldSeed.append(s.charAt(i));
            i++;
        }
        i++; // skip 's'

        long seed = Long.parseLong(oldSeed.toString());
        World world = new World(WIDTH, HEIGHT - 1, seed);
        TETile[][] tiles = world.createWorld();

        // replay movement commands after 's'
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == 'w' || c == 'a' || c == 's' || c == 'd') {
                world.moveAvatar(c);
            }
            i++;
        }
        return world;
    }


    public static void  updateHUD(TETile[][] world, String dialogue, String[] options) {
        StdDraw.setPenColor(Color.WHITE);
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        String desc;
        if (x < 0 || x >= world.length || y < 0 || y >= world[0].length) {
            desc = "";
        } else {
            TETile tile = world[x][y];
            if(tile == null) {
                desc = "";
            }
            else {
                desc = tile.description();
            }

        }
        String hudText = desc;

        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT - 2, WIDTH / 2.0, 2);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1,HEIGHT - 1,desc);
        if (options != null) {
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.textRight(WIDTH - 1, HEIGHT - 1, dialogue);

            StdDraw.setPenColor(Color.WHITE);
            double optionY = HEIGHT - 2;
            for (String option : options) {
                StdDraw.textLeft(1, optionY, option);
                optionY -= 0.7;
            }
        } else {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.textLeft(1, HEIGHT - 1, desc);

            if (!dialogue.isEmpty()) {
                StdDraw.setPenColor(Color.YELLOW);
                StdDraw.textRight(WIDTH - 1, HEIGHT - 1, dialogue);
            }
        }
        StdDraw.show();

    }

    public static void runGame(World world, TERenderer ter, TETile[][] tiles) {
        ter.renderFrame(tiles);
        updateHUD(tiles, currentDialogue, null);
        boolean quitMode = false;

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());

                if (quitMode) {
                    if (c == 'q') {
                        saveGame();
                        AudioManager.stopAllMusic();
                        System.exit(0);
                    } else {
                        quitMode = false;
                    }
                    continue;
                }

                if (inDialogue && currentNPC != null) {
                    if (c == '1' || c == '2' || c == '3') {
                        int choice = c - '1';  // Convert '1','2','3' to 0,1,2
                        currentDialogue = currentNPC.getResponseResult(choice);
                        currentOptions = null;
                        inDialogue = false;
                        currentNPC = null;
                        ter.renderFrame(tiles);
                    }
                    continue;  // Don't process movement while in dialogue
                }

                if (c == ':') {
                    quitMode = true;
                    continue;
                }

                switch (c) {
                    case 'w':
                    case 'a':
                    case 's':
                    case 'd':
                        NPC npc = world.moveAvatar(c);
                        if (npc != null) {
                            currentDialogue = npc.getDialogue();
                            currentOptions = npc.getResponseOptions();
                            currentNPC = npc;
                            inDialogue = true;
                            AudioManager.playDialogueSound();
                        } else {
                            currentDialogue = "";
                            currentOptions = null;
                        }
                        history.append(c);
                        AudioManager.playStepSound();
                        ter.renderFrame(tiles);
                        break;
                }
            }

            updateHUD(tiles, currentDialogue, currentOptions);
            StdDraw.pause(20);
        }
    }
}
