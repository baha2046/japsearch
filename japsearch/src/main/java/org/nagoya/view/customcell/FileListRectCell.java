package org.nagoya.view.customcell;

import com.jfoenix.controls.JFXListCell;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import org.nagoya.GUICommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.model.dataitem.Studio;

public class FileListRectCell extends JFXListCell<DirectoryEntry> {
    private FXFileListCell normalRectCell;
    private FXFileListMovieCell movieRectCell;

    public FileListRectCell() {
        this.normalRectCell = new FXFileListCell();
        this.movieRectCell = new FXFileListMovieCell();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(Insets.EMPTY);
        this.setPrefWidth(460);
        //system.out.println("------------ Creating Cell ------------");
    }

    @Override
    public void updateItem(DirectoryEntry item, boolean empty) {
        super.updateItem(item, empty);

        this.setText("");

        if (!empty) {
        /*    if (item.isDirectory()) {
                //this.setStyle("-fx-control-inner-background: #DDDDDD ; -fx-control-inner-background-alt: derive(-fx-control-inner-background, 30%);");
            } else {
                this.setStyle("");
            }*/

            if (!item.getNeedCheck() && item.hasNfo()) {
                this.ShowMovie(item);
            } else {
                this.ShowNormal(item);
            }
        } else {
            this.setGraphic(null);
        }
    }

    private void ShowMovie(DirectoryEntry item) {
        this.setPrefHeight(80);
        this.movieRectCell.setModel(item);
        this.movieRectCell.iconView.setImage(item.getFileIcon());
        this.setGraphic(this.movieRectCell);
    }

    private void ShowNormal(DirectoryEntry item) {
        this.setPrefHeight(40);
        this.normalRectCell.setModel(item);
        this.normalRectCell.iconView.setImage(item.getFileIcon());
        this.setGraphic(this.normalRectCell);
    }

    static class FXFileListCell extends AnchorPane {
        @FXML
        public ImageView iconView;
        @FXML
        private Label nameLabel;
        @FXML
        private Label typeLabel;
        private final ObjectProperty<DirectoryEntry> model = new SimpleObjectProperty<DirectoryEntry>(this, "model") {
            private DirectoryEntry currentModel;

            @Override
            protected void invalidated() {
                if (this.currentModel != null) {
                    FXFileListCell.this.unbind();
                }
                this.currentModel = this.get();
                if (this.currentModel != null) {
                    FXFileListCell.this.bind(this.currentModel);
                }
            }
        };
        @FXML
        private Rectangle dirMask;
        @FXML
        private Rectangle rect;

        FXFileListCell() {
            GUICommon.loadFXMLRoot(this);
            this.iconView.setPreserveRatio(true);
        }

        //public final DirectoryEntry getModel() { return model.of(); }

        void setModel(DirectoryEntry model) {
            this.model.set(model);
            if (model.hasMovie()) {
                this.rect.setFill(javafx.scene.paint.Color.web("#f87849"));
            } else {
                this.rect.setFill(javafx.scene.paint.Color.web("#1f5cff"));
            }
            if (model.isDirectory()) {
                this.dirMask.setVisible(true);
            } else {
                this.dirMask.setVisible(false);
            }
        }

        void bind(DirectoryEntry model) {
            //genderImageView.imageProperty().bind(Bindings.<Image>select(modelProperty(), "gender", "image"));
            this.nameLabel.textProperty().bind(model.getFileNameProperty());
            this.typeLabel.textProperty().bind(model.getFileExtenionProperty());
        }

        void unbind() {
            //genderImageView.imageProperty().unbind();
            this.nameLabel.textProperty().unbind();
            this.typeLabel.textProperty().unbind();
        }
    }

    static class FXFileListMovieCell extends AnchorPane {
        //public ObjectProperty<DirectoryEntry> modelProperty() { return model; }

        @FXML
        ImageView iconView;
        @FXML
        private Rectangle dirMask;
        //private ImageView genderImageView;
        @FXML
        private Label nameLabel;
        @FXML
        private Label idLabel;
        @FXML
        private Label studioLabel;
        @FXML
        private Label typeLabel;
        @FXML
        private Label pathLabel;
        @FXML
        private ImageView imageView;


        FXFileListMovieCell() {
            GUICommon.loadFXMLRoot(this);
            //this.initializeComponent();
            //getStyleClass().add("user-view");
            this.imageView.setPreserveRatio(true);
        }

        //public final DirectoryEntry getModel() { return model.of(); }
        void setModel(DirectoryEntry model) {
            //this.model.set(model);
            if (model.isDirectory()) {
                this.dirMask.setVisible(true);
                if ("VIRTUAL".equals(model.getFileExtenion())) {
                    //this.setStyle("-fx-control-inner-background: #DDDDDD ; -fx-control-inner-background-alt: derive(-fx-control-inner-background, 30%);");
                    this.dirMask.setFill(Paint.valueOf("#FF2222"));
                } else {
                    this.dirMask.setFill(Paint.valueOf("#c4c4c6"));
                }
            } else {
                this.dirMask.setVisible(false);
            }

            if (model.getMovieData() != null) {
                //    this.titledPane.setText(model.getMovieData().getId().getId());
                this.idLabel.setText(model.getMovieData().getMovieID().getId());
                this.nameLabel.setText(model.getMovieData().getMovieTitle().getTitle());
                this.studioLabel.setText(model.getMovieData().getStudios().map(Studio::getStudio).getOrElse(""));
                this.pathLabel.setText(model.getFilePath().toString());
                if (model.getFileExtenion() != null) {
                    //   if (model.getFileExtenion().equals("Folder")) {
                    this.typeLabel.setText(String.valueOf(model.getMovieSize()) + " MB");
                    // } else {
                    //     int x = model.getFileExtenion().indexOf("/");
                    //this.typeLabel.setText(x > 0 ? model.getFileExtenion().substring(0, x) : model.getFileExtenion());
                    //     this.typeLabel.setText(x > 0 ? model.getFileExtenion().substring(x+1) : model.getFileExtenion());
                    // }
                } else {
                    this.typeLabel.setText("");
                }

                if (model.getMovieData().hasFrontCover()) {
                    model.getMovieData().getImgFrontCover().peek(t -> t.getImage(this.imageView::setImage));
                    this.imageView.setViewport(null);
                } else if (model.getMovieData().hasBackCover()) {
                    this.imageView.setVisible(false);
                    model.getMovieData().getImgBackCover().peek(t -> t.getImage((img) -> {
                        this.imageView.setImage(img);
                        this.imageView.setViewport(FxThumb.getCoverCrop(img.getWidth(),
                                img.getHeight(), model.getMovieData().getMovieID().toString()));
                        this.imageView.setVisible(true);
                    }));
                } else {
                    this.imageView.setImage(null);
                }
            }
        }

        void initializeComponent() {
            //FXController.of(this).fromDefaultLocation().load();
            this.nameLabel = new Label();
            this.nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-overrun: ellipsis; -fx-effect: dropshadow( three-pass-box, rgba(0,0,0,0.4), 4, 0.3, 2, 2);");
            this.nameLabel.setPrefWidth(350);

            this.idLabel = new Label();
            this.idLabel.setFont(javafx.scene.text.Font.font(javafx.scene.text.Font.getDefault().getName(), FontWeight.BOLD, 12));
            this.idLabel.setPrefWidth(70);

            //this.studioLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));

            this.typeLabel = new Label();
            this.typeLabel.setPrefWidth(60);
            this.typeLabel.setStyle("  -fx-font-size: 10px;\n" +
                    "    -fx-background-color: linear-gradient(#D3D3D3, #778899);\n" +
                    "    -fx-text-fill: #ffffff;\n" +
                    "  -fx-alignment: center;\n" +
                    "  -fx-background-radius: 6;\n" +
                    "    -fx-padding: 1px;");
            //this.typeLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
            //this.typeLabel.setTextFill(Color.GRAY);
            //nameLabel.setStroke(Color.web("#7080A0"));

            this.studioLabel = new Label();
            this.studioLabel.setStyle("-fx-font-size: 16;  -fx-text-fill: linear-gradient(to bottom right, red, black); -fx-effect: dropshadow( three-pass-box, rgba(0,0,0,0.4), 3, 0.0, 2, 2);");
            this.studioLabel.setPrefWidth(130);

            this.idLabel.setPrefWidth(150);
            this.idLabel.setStyle("  -fx-border-color: derive(#A4C6FF, -20%);\n" +
                    "  -fx-border-width: 2;\n" +
                    "  -fx-alignment: center;\n" +
                    "-fx-border-style: dotted solid dotted solid;\n" +
                    //"  -fx-background-insets: 8;\n" +
                    "  -fx-border-insets: 8;\n" +
                    "  -fx-border-radius: 6;");

		    /*StackPane stack1 = new StackPane();
		    stack1.getChildren().addAll(rec1, nameLabel);
		    stack1.setAlignment(Pos.TOP_LEFT);     // Right-justify nodes in stack
		    StackPane.setMargin(nameLabel, new Insets(1, 10, 0, 28)); // Center "?"*/
            //this.add(this.statsRec, 0, 2);

            this.iconView = new ImageView();
            this.iconView.setStyle("-fx-text-fill: #444444;\n" +
                    "    -fx-effect: dropshadow( three-pass-box, rgba(0,0,0,0.4), 3, 0.0, 1, 1);");

            this.pathLabel = new Label();
            this.pathLabel.setStyle("-fx-font-size: 10; -fx-text-overrun: leading-ellipsis;");
            this.pathLabel.setPrefWidth(375);

            HBox hBox1 = new HBox();
            hBox1.setSpacing(12);
            hBox1.setAlignment(Pos.CENTER_LEFT);
            hBox1.getChildren().addAll(this.idLabel, this.studioLabel, this.typeLabel);

            HBox hBox2 = new HBox();
            hBox2.setSpacing(12);
            hBox2.setAlignment(Pos.CENTER_LEFT);
            hBox2.getChildren().addAll(this.iconView, this.nameLabel);

            hBox2.setPadding(new Insets(0, 0, 0, 8));
            this.pathLabel.setPadding(new Insets(5, 0, 0, 10));

           /* this.titledPane = new TitledPane();
            this.titledPane.setCollapsible(false);//remove closing action
            this.titledPane.setAnimated(false);//stop animating
            this.titledPane.setPrefWidth(420);
            this.titledPane.setFocusTraversable(false);

            this.titledPane.setText("");
            this.titledPane.setContent(hBox);*/

            this.imageView = new ImageView();
            this.imageView.setFitHeight(60);
            this.imageView.setPreserveRatio(true);

            // this.add(separator, 1, 1);
            // this.add(this.typeLabel, 1, 2);

            //setMargin(separator, new Insets(4, 0, 0, 0));
            //setMargin(nameLabel, new Insets(2, 0, 0, 2));
        }

    }
}
