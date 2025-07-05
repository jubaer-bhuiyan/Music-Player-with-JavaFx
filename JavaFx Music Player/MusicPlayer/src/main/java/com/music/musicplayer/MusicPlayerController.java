package com.music.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Slider;
import javafx.stage.DirectoryChooser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;

public class MusicPlayerController {

    @FXML
    private Label choseMusic;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label totalTimeLabel;

    private Timeline timeline;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ListView<File> musicListView;

    @FXML
    private Slider timeSlider; // Slider for time period of music

    private MediaPlayer mediaPlayer;

    private int currentSongIndex = -1; // Index of the currently playing song

    @FXML
    void initialize() {
        // Set up listener for volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });

        // Set up listener for timeSlider
        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null && Math.abs(newValue.doubleValue() - mediaPlayer.getCurrentTime().toSeconds()) > 0.5) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });

        // Initialize timeline to update time labels and slider
        timeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> updateTime())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Set initial state for timeSlider
        timeSlider.setMin(0);
        timeSlider.setValue(0);
    }

    private void updateTime() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            double totalTime = mediaPlayer.getTotalDuration().toSeconds();
            currentTimeLabel.setText(formatTime(currentTime));
            totalTimeLabel.setText(formatTime(totalTime));
        }
    }

    private String formatTime(double seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @FXML
    void choseMusic(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Directory");
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav"));
            if (files != null && files.length > 0) {
                musicListView.getItems().clear(); // Clear existing items
                musicListView.getItems().addAll(files); // Add new items
                musicListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentSongIndex = musicListView.getItems().indexOf(newValue);
                        playMusic(newValue);
                    }
                });
            } else {
                choseMusic.setText("No music files found in selected directory");
            }
        }
    }

    private void playMusic(File musicFile) {
        Media media = new Media(musicFile.toURI().toString());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(() -> {
            // Play the next song automatically when the current song ends
            next(null);
        });
        mediaPlayer.play();
        choseMusic.setText(musicFile.getName());
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

        // Set up listener to update timeSlider's max value when mediaPlayer changes
        mediaPlayer.setOnReady(() -> {
            double durationInSeconds = mediaPlayer.getTotalDuration().toSeconds();
            timeSlider.setMax(durationInSeconds);
        });

        // Bind timeSlider to mediaPlayer's currentTime
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newValue.toSeconds());
            }
        });
    }

    @FXML
    void pause(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    void play(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    void stop(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @FXML
    void next(MouseEvent event) {
        if (musicListView.getItems().isEmpty()) {
            return;
        }
        currentSongIndex++;
        if (currentSongIndex >= 0 && currentSongIndex < musicListView.getItems().size()) {
            File nextSong = musicListView.getItems().get(currentSongIndex);
            playMusic(nextSong);
            musicListView.getSelectionModel().select(currentSongIndex);
        } else {
            // Reached the end of the list, loop back to the beginning
            currentSongIndex = 0;
            File nextSong = musicListView.getItems().get(currentSongIndex);
            playMusic(nextSong);
            musicListView.getSelectionModel().select(currentSongIndex);
        }
    }

    @FXML
    void previous(MouseEvent event) {
        if (musicListView.getItems().isEmpty()) {
            return;
        }
        currentSongIndex--;
        if (currentSongIndex >= 0 && currentSongIndex < musicListView.getItems().size()) {
            File previousSong = musicListView.getItems().get(currentSongIndex);
            playMusic(previousSong);
            musicListView.getSelectionModel().select(currentSongIndex);
        } else {
            // Reached the beginning of the list, loop to the end
            currentSongIndex = musicListView.getItems().size() - 1;
            File previousSong = musicListView.getItems().get(currentSongIndex);
            playMusic(previousSong);
            musicListView.getSelectionModel().select(currentSongIndex);
        }
    }
}
