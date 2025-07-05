module com.music.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.music.musicplayer to javafx.fxml;
    exports com.music.musicplayer;
}