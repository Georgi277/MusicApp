package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import model.*;

import java.io.File;

/**
 * Главен JavaFX клас за музикалното приложение.
 * Функции:
 * - Показва музикална библиотека
 * - Позволява добавяне/премахване на песни от плейлист
 * - Реално възпроизвеждане на mp3 (Play/Pause/Stop)
 * - Прогрес бар + време + контрол на звука
 */
public class MusicApp extends Application {

    // ====== МОДЕЛ (данни) ======
    private final MusicLibrary library = new MusicLibrary();
    private final User user = new User("Gogo");
    private Playlist selectedPlaylist;

    // ====== PLAYER ======
    private MediaPlayer player;
    private Song nowPlaying;

    // ====== UI ======
    private ListView<Song> libraryView;
    private ListView<Song> playlistView;

    private Label nowPlayingLabel;
    private Label timeLabel;

    private Slider progressSlider;
    private Slider volumeSlider;

    // Таймер за обновяване на прогреса
    private Timeline progressTimeline;

    @Override
    public void start(Stage stage) {
        // Създаваме един плейлист по подразбиране
        user.addPlaylist(new Playlist("My Playlist"));
        selectedPlaylist = user.getPlaylists().get(0);

        // ===== ЛЯВО: Библиотека =====
        libraryView = new ListView<>();
        libraryView.setItems(FXCollections.observableArrayList(library.getSongs()));

        Button addButton = new Button("Add to Playlist");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> addSelectedSongToPlaylist());

        VBox left = new VBox(10,
                new Label("Music Library"),
                libraryView,
                addButton
        );
        left.setPadding(new Insets(10));
        left.setPrefWidth(320);

        // ===== ДЯСНО: Плейлист =====
        playlistView = new ListView<>();
        refreshPlaylistView();

        Button removeButton = new Button("Remove from Playlist");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(e -> removeSelectedSongFromPlaylist());

        VBox right = new VBox(10,
                new Label("Playlist"),
                playlistView,
                removeButton
        );
        right.setPadding(new Insets(10));
        right.setPrefWidth(320);

        // ===== ДОЛУ: Player контроли =====
        nowPlayingLabel = new Label("Now playing: (none)");
        timeLabel = new Label("00:00 / 00:00");

        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button stopBtn = new Button("⏹ Stop");

        playBtn.setOnAction(e -> playSelected());
        pauseBtn.setOnAction(e -> pause());
        stopBtn.setOnAction(e -> stopPlayback());

        progressSlider = new Slider(0, 100, 0);
        progressSlider.setDisable(true);
        progressSlider.setOnMouseReleased(e -> seekToSlider());

        volumeSlider = new Slider(0, 1, 0.7);
        Label volLabel = new Label("🔊");

        HBox controls = new HBox(10, playBtn, pauseBtn, stopBtn, volLabel, volumeSlider);
        VBox bottom = new VBox(8, nowPlayingLabel, timeLabel, progressSlider, controls);
        bottom.setPadding(new Insets(10));

        // ===== Главен layout =====
        HBox center = new HBox(20, left, right);
        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setBottom(bottom);

        stage.setTitle("Music Library App");
        stage.setScene(new Scene(root, 760, 520));
        stage.show();

        // Добра практика: при затваряне спри плейъра
        stage.setOnCloseRequest(e -> {
            cleanupPlayer();
            Platform.exit();
        });
    }

    /**
     * Пуска песента, избрана от библиотеката или плейлиста.
     */
    private void playSelected() {
        Song selected = libraryView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selected = playlistView.getSelectionModel().getSelectedItem();
        }

        if (selected == null) {
            showAlert("No song selected", "Select a song from the library or playlist, then press Play.");
            return;
        }

        playSong(selected);
    }

    /**
     * Реално пускане на песен чрез JavaFX MediaPlayer.
     */
    private void playSong(Song song) {
        // Ако е същата песен и е паузирана — продължи
        if (player != null && nowPlaying == song) {
            player.play();
            return;
        }

        // Спри стара песен
        cleanupPlayer();

        // Проверка за файл
        File file = new File(song.getFilePath());
        if (!file.exists()) {
            showAlert("File not found",
                    "Cannot find mp3 file:\n" + file.getPath() +
                            "\n\nMake sure it is in src/audio/ and the filename matches.");
            return;
        }

        try {
            Media media = new Media(file.toURI().toString());
            player = new MediaPlayer(media);

            nowPlaying = song;
            nowPlayingLabel.setText("Now playing: " + song.getTitle() + " — " + song.getArtist());

            // Volume
            player.setVolume(volumeSlider.getValue());
            volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
                if (player != null) player.setVolume(newV.doubleValue());
            });

            // Ready -> активираме прогреса
            player.setOnReady(() -> {
                progressSlider.setDisable(false);
                progressSlider.setMin(0);
                progressSlider.setMax(player.getTotalDuration().toSeconds());
                updateTimeLabel(Duration.ZERO, player.getTotalDuration());
                startProgressTimer();
            });

            // Край на песента
            player.setOnEndOfMedia(() -> stopPlayback());

            player.play();

        } catch (Exception ex) {
            showAlert("Playback error", "Could not play the file.\nReason: " + ex.getMessage());
        }
    }

    private void pause() {
        if (player != null) player.pause();
    }

    /**
     * Спира текущото възпроизвеждане и връща песента в началото.
     * Името е stopPlayback(), за да НЕ конфликтва с Application.stop().
     */
    private void stopPlayback() {
        if (player != null) {
            player.stop();
            progressSlider.setValue(0);
            updateTimeLabel(Duration.ZERO, player.getTotalDuration());
        }
    }

    /**
     * Превъртане при местене на слайдера.
     */
    private void seekToSlider() {
        if (player == null || player.getTotalDuration() == null) return;
        if (progressSlider.isDisabled()) return;

        double seconds = progressSlider.getValue();
        player.seek(Duration.seconds(seconds));
    }

    /**
     * Добавя избрана песен от библиотеката към плейлиста.
     */
    private void addSelectedSongToPlaylist() {
        Song selected = libraryView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No song selected", "Select a song from the library to add.");
            return;
        }
        selectedPlaylist.addSong(selected);
        refreshPlaylistView();
    }

    /**
     * Премахва избрана песен от плейлиста.
     */
    private void removeSelectedSongFromPlaylist() {
        Song selected = playlistView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No song selected", "Select a song from the playlist to remove.");
            return;
        }
        selectedPlaylist.removeSong(selected);
        refreshPlaylistView();
    }

    private void refreshPlaylistView() {
        playlistView.setItems(FXCollections.observableArrayList(selectedPlaylist.getSongs()));
    }

    /**
     * Таймер за обновяване на прогрес и време.
     */
    private void startProgressTimer() {
        if (progressTimeline != null) {
            progressTimeline.stop();
        }

        progressTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (player == null) return;

            Duration current = player.getCurrentTime();
            Duration total = player.getTotalDuration();

            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(current.toSeconds());
            }

            updateTimeLabel(current, total);
        }));

        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();
    }

    private void updateTimeLabel(Duration current, Duration total) {
        if (current == null) current = Duration.ZERO;
        if (total == null || total.isUnknown()) total = Duration.ZERO;

        timeLabel.setText(format(current) + " / " + format(total));
    }

    private String format(Duration d) {
        int totalSeconds = (int) Math.floor(d.toSeconds());
        if (totalSeconds < 0) totalSeconds = 0;

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Освобождава ресурси на MediaPlayer.
     */
    private void cleanupPlayer() {
        if (progressTimeline != null) {
            progressTimeline.stop();
            progressTimeline = null;
        }
        if (player != null) {
            try {
                player.stop();
                player.dispose();
            } catch (Exception ignored) {
            }
            player = null;
        }
        nowPlaying = null;

        progressSlider.setDisable(true);
        progressSlider.setValue(0);
        nowPlayingLabel.setText("Now playing: (none)");
        timeLabel.setText("00:00 / 00:00");
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    /**
     * Това е истинският stop() на JavaFX Application (вика се при затваряне).
     */
    @Override
    public void stop() {
        cleanupPlayer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
