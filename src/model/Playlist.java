package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Playlist представя плейлист с име и списък от песни.
 */
public class Playlist {
    private final String name;
    private final ArrayList<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    /**
     * Връща неизменяем изглед към песните (добра практика).
     */
    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public void addSong(Song song) {
        if (song == null) return;
        songs.add(song);
    }

    public void removeSong(Song song) {
        if (song == null) return;
        songs.remove(song);
    }

    @Override
    public String toString() {
        return name;
    }
}
