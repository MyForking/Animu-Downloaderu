package com.dakusuta.tools.anime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dakusuta.tools.anime.callback.Crawler;
import com.dakusuta.tools.anime.callback.TableObserver;
import com.dakusuta.tools.anime.callback.TableSelectListener;
import com.dakusuta.tools.anime.custom.AnimeLabel;
import com.dakusuta.tools.anime.custom.ConfirmDialog;
import com.dakusuta.tools.anime.custom.EpisodeLabel;
import com.dakusuta.tools.anime.custom.LoadDialog;
import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.DownloadManager;
import com.dakusuta.tools.anime.download.Status;
import com.dakusuta.tools.anime.source.Source;
import com.dakusuta.tools.anime.source.Sources;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

public class MainFXMLController implements TableObserver, Crawler {
	@FXML private VBox root;
	// To search for specific anime
	@FXML private TextField search;

	@FXML private CheckBox cb;

	// To download
	@FXML private Button download;

	// Display episodes of selected anime
	@FXML private Button showEpisodes;

	@FXML ImageView poster;
	// For Anime summary
	@FXML private WebView webView;

	@FXML private ComboBox<String> servers;

	// For displaying downloads
	@FXML private ScrollPane scrollPane;
	@FXML private TableView<DownloadInfo> tableView;
	@FXML private TableColumn<DownloadInfo, String> fileName;
	@FXML private TableColumn<DownloadInfo, Double> size;
	@FXML private TableColumn<DownloadInfo, Double> downloaded;
	@FXML private TableColumn<DownloadInfo, String> progress;
	@FXML private TableColumn<DownloadInfo, Status> status;

	private VBox vBox;
	private WebEngine webEngine;
	private List<AnimeLabel> animeList = new ArrayList<>();
	final ObservableList<EpisodeLabel> episodes = FXCollections.observableArrayList();
	DownloadManager manager = DownloadManager.getInstance();
	private Window window;
	private Sources sources;

	@FXML
	protected void showEpisodes(ActionEvent event) {
		vBox.getChildren().clear();
		if (showEpisodes.getText().equals("Show Episodes")) {
			loadEpisodes();
			download.setDisable(false);
			showEpisodes.setText("Back to Anime list");
		} else {
			webEngine.loadContent("<body bgcolor='#323232'>");
			showEpisodes.setDisable(true);
			search(search.getText());
			poster.setImage(null);
			showEpisodes.setText("Show Episodes");
		}
	}

	ListView<EpisodeLabel> listView;

	private void loadEpisodes() {
		episodes.clear();
		cb.setIndeterminate(false);
		cb.setSelected(false);
		vBox.getChildren().clear();
		episodes.addAll(sources.loadEpisodes());
		listView = new ListView<EpisodeLabel>();
		listView.setItems(episodes);
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				int size = listView.getSelectionModel().getSelectedIndices().size();
				if (size == 0) {
					cb.setIndeterminate(false);
					cb.setSelected(false);
				}
				if (size < episodes.size()) {
					cb.setIndeterminate(true);
				} else {
					cb.setIndeterminate(false);
					cb.setSelected(true);
				}

			}

		});
		VBox.setVgrow(listView, Priority.ALWAYS);
		vBox.getChildren().add(listView);
	}

	@FXML
	protected void download(ActionEvent event) {

		int count = listView.getSelectionModel().getSelectedItems().size();
		Optional<Boolean> result = new ConfirmDialog(count).showAndWait();
		result.ifPresent(res -> {
			if (res) downloadSelected();
		});
	}

	void loadAnime(Window window) {
		if (this.window == null) this.window = window;
		sources.setSource(Source.values()[servers.getSelectionModel().getSelectedIndex()]);
		vBox.getChildren().clear();
		new Thread(() -> {
			animeList = sources.loadAnime(window);
			if (animeList != null) {
				Platform.runLater(() -> {
					vBox.getChildren().addAll(animeList);
					search(search.getText());
				});
			}
		}).start();

	}

	@FXML
	public void initialize() {
		webEngine = webView.getEngine();
		webEngine.loadContent("<html><body bgcolor='#424242'></body></html>");
		vBox = (VBox) scrollPane.getContent().lookup("#list");

		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		tableView.setRowFactory(new TableSelectListener());
		search.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
		sources = Sources.getInstance(this);
		manager.setController(this);
		servers.getSelectionModel().select(0);
		servers.valueProperty().addListener((e) -> {
			loadAnime(window);
			poster.setImage(null);
			webEngine.loadContent("<html><body bgcolor='#424242'></body></html>");
		});

		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> paramObservableValue, Boolean old,
					Boolean flag) {
				if (listView == null) return;
				if (flag) {
					listView.getSelectionModel().selectAll();
				} else {
					listView.getSelectionModel().clearSelection();
				}
			}
		});
	}

	private void search(String text) {
		showEpisodes.setDisable(true);
		download.setDisable(true);
		if (!(animeList.isEmpty())) {
			vBox.getChildren().clear();
			List<AnimeLabel> anime = animeList.stream().filter(label -> label.hasValue(text))
					.collect(Collectors.toList());

			vBox.getChildren().addAll(anime);
		}
	}

	private void downloadSelected() {
		new Thread(() -> {
			listView.getSelectionModel().getSelectedItems().forEach(episode -> {
				manager.addDownloadURL(episode.copy());
			});

		}).start();
	}

	@Override
	public void added(DownloadInfo download) {
		tableView.getItems().add(download);
	}

	@Override
	public void updated(DownloadInfo download) {
		tableView.refresh();
	}

	@Override
	public void loading() {
		LoadDialog.showDialog(window, "Please wait", "Fetching anime details");
	}

	@Override
	public void loaded(String content) {
		Platform.runLater(() -> {
			webEngine.loadContent(content);
			LoadDialog.stopDialog();
			showEpisodes.setDisable(false);
			showEpisodes.setText("Show Episodes");
		});
	}

	@Override
	public void poster(Image image) {
		Platform.runLater(() -> poster.setImage(image));
	}
}