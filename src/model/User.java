package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User пази потребителско име и неговите плейлисти.
 * За проекта: един текущ потребител.
 */
public class User {
    private final String username;
    private final ArrayList<Playlist> playlists;

    public User(String username) {
        this.username = username;
        this.playlists = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    public void addPlaylist(Playlist playlist) {
        if (playlist == null) return;
        playlists.add(playlist);
    }

    public void removePlaylist(Playlist playlist) {
        if (playlist == null) return;
        playlists.remove(playlist);
    }

    public Playlist findPlaylistByName(String name) {
        if (name == null) return null;
        for (Playlist p : playlists) {
            if (p.getName().equalsIgnoreCase(name.trim())) return p;
        }
        return null;
    }
}
