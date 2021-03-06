package com.codingotaku.apps.custom;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

public class DownloadDialog extends Dialog<Boolean> {
	public DownloadDialog(int count) {
		ButtonType buttonType = new ButtonType("Yes", ButtonData.YES);
		DialogPane dialogPane = getDialogPane();
		initStyle(StageStyle.UNDECORATED);
		dialogPane.getStylesheets().add(getClass().getResource("/css/combo.css").toExternalForm());

		HBox header = new HBox();
		Label title = new Label("Confirm Download");
		title.setId("head");
		Label close = new Label("X");
		close.setId("head");
		Pane space = new Pane();
		HBox.setHgrow(space, Priority.ALWAYS);
		close.setAlignment(Pos.TOP_RIGHT);
		close.setOnMouseEntered(e -> close.setId("none"));
		close.setOnMouseExited(e -> close.setId("head"));
		close.setOnMouseClicked(e -> close());
		header.getChildren().addAll(title, space, close);
		GridPane gridPane = new GridPane();
		gridPane.setVgap(10);
		String question;
		if (count == 0) {
			question = "Please select at least one video";
			dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
		} else {
			question = String.format("Do you want to download %d episode(s)?", count);
			dialogPane.getButtonTypes().addAll(buttonType, ButtonType.NO);
		}

		Label label = new Label(question);
		GridPane.setConstraints(header, 0, 0, 2, 1);
		GridPane.setConstraints(label, 1, 2);
		gridPane.getChildren().addAll(header, label);
		dialogPane.setContent(gridPane);
		setResultConverter(dialogButton -> dialogButton == buttonType);
	}

}
