package core;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class AudioManager {

    private static Clip menuClip;
    private static Clip gameClip;

    private static final String MENU_MUSIC = "/audio/menu_music.wav";
    private static final String GAME_MUSIC = "/audio/game_music.wav";
    private static final String STEP_SOUND = "/audio/step.wav";
    private static final String DIALOGUE_SOUND = "/audio/dialogue.wav";

    private static Clip startClip(String resourcePath, boolean loop) {
        try {
            InputStream rawIn = AudioManager.class.getResourceAsStream(resourcePath);
            if (rawIn == null) {
                System.out.println("Audio not found: " + resourcePath);
                return null;
            }

            AudioInputStream in = AudioSystem.getAudioInputStream(rawIn);
            Clip clip = AudioSystem.getClip();
            clip.open(in);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            clip.start();
            return clip;
        } catch (Exception e) {
            System.out.println("Audio error");
            return null;
        }
    }

    private static void stopClip(Clip c) {
        if (c != null) {
            c.stop();
            c.close();
        }
    }

    public static void playMenuMusic() {
        stopClip(menuClip);
        menuClip = startClip(MENU_MUSIC, true);
    }


    public static void playDialogueSound() {
        startClip(DIALOGUE_SOUND, false);
    }


    public static void playGameMusic() {
        stopClip(gameClip);
        gameClip = startClip(GAME_MUSIC, true);

        if (gameClip != null && gameClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) gameClip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(-15.0f);
        }
    }

    public static void stopAllMusic() {
        stopClip(menuClip);
        stopClip(gameClip);
        menuClip = null;
        gameClip = null;
    }

    public static void playStepSound() {
        startClip(STEP_SOUND, false);
    }
}
