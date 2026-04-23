package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NPC extends Avatar {
    private String name;
    private String dialogue;
    private String[] responseOptions;
    private String[] responseResults;

    private static final Map<String, String> DEFAULT_DIALOGUES = Map.of(
            "Villager", "Hello traveler! How can I help you?",
            "Wizard", "I sense great power in you... What do you seek?",
            "Goblin", "You dare approach me?!"
    );

    private static final Map<String, String[]> DEFAULT_RESPONSES = Map.of(
            "Villager", new String[]{"1: Ask for directions", "2: Trade items", "3: Say goodbye"},
            "Wizard", new String[]{"1: Learn a spell", "2: Ask about the dungeon", "3: Leave"},
            "Goblin", new String[]{"1: Fight", "2: Offer gold", "3: Run away"}
    );

    private static final Map<String, String[]> DEFAULT_RESULTS = Map.of(
            "Villager", new String[]{"The exit is to the north!", "I have nothing to trade.", "Farewell, traveler!"},
            "Wizard", new String[]{"Abracadabra! You learned... nothing.", "Dark secrets lie within these walls.", "Wise choice..."},
            "Goblin", new String[]{"You won! The goblin retreats.", "Fine, take my stuff!", "You escaped!"}
    );

    public NPC(int x, int y, String name) {
        super(x, y);
        this.name = name;

        this.dialogue = DEFAULT_DIALOGUES.getOrDefault(name, "...");
        this.responseOptions = DEFAULT_RESPONSES.getOrDefault(name, new String[]{"1: Continue"});
        this.responseResults = DEFAULT_RESULTS.getOrDefault(name, new String[]{"..."});
    }

    public String getName() { return name; }

    public String getDialogue() {
        return name + ": " + dialogue;
    }

    public String[] getResponseOptions() {
        return responseOptions;
    }

    public String getResponseResult(int choice) {
        if (choice < 0 || choice >= responseResults.length) {
            return name + ": ...";
        }
        return name + ": " + responseResults[choice];
    }
}