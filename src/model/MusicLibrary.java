package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MusicLibrary съдържа всички налични песни в приложението.
 * Песните са само български поп-фолк:
 * - Преслава (2 песни)
 * - Галена (2 песни)
 *
 * MP3 файловете трябва да се намират в src/audio/
 */
public class MusicLibrary {

    private final ArrayList<Song> songs;

    public MusicLibrary() {
        songs = new ArrayList<>();

        // ===== ПРЕСЛАВА =====
        songs.add(new Song(
                "Моето слабо място",
                "Преслава",
                "Pop-Folk",
                215,
                "audio/preslava_moeto_slabo_myasto.mp3"
        ));

        songs.add(new Song(
                "Пиян",
                "Преслава",
                "Pop-Folk",
                205,
                "audio/preslava_piyan.mp3"
        ));

        // ===== ГАЛЕНА =====
        songs.add(new Song(
                "Стара каравана",
                "Галена",
                "Pop-Folk",
                220,
                "audio/galena_stara_karavana.mp3"
        ));

        songs.add(new Song(
                "Euphoria",
                "Галена",
                "Pop-Folk",
                210,
                "audio/galena_euphoria.mp3"
        ));
    }

    /**
     * Връща списък с всички песни (read-only).
     */
    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }
}
