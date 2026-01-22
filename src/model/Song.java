package model;

/**
 * Клас Song описва една песен в музикалната библиотека.
 * Освен информация (заглавие, изпълнител...), пазим и път до mp3 файл.
 */
public class Song {
    private final String title;
    private final String artist;
    private final String genre;
    private final int durationSeconds; // продължителност в секунди (за информация)
    private final String filePath;     // относителен път, напр. "audio/preslava_pazi_se.mp3"

    public Song(String title, String artist, String genre, int durationSeconds, String filePath) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * Кратък текст за показване в списъци (ListView).
     */
    @Override
    public String toString() {
        return title + " — " + artist;
    }
}
