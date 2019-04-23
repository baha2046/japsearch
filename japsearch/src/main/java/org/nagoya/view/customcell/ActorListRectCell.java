package org.nagoya.view.customcell;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.system.Systems;

import java.net.URL;

public class ActorListRectCell extends ListCell<ActorV2> {

    private FXActorCell actorCell;

    public ActorListRectCell() {
        this.actorCell = new FXActorCell();
        this.actorCell.setParent(this);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(Insets.EMPTY);
        this.setPrefWidth(248);
        this.setPrefHeight(100);
        //system.out.println("------------ ActorListRectCell Cont ------------");
    }

    public ActorListRectCell(boolean useButton) {
        this();
        if (!useButton) {
            this.actorCell.btnEdit.setVisible(false);
            this.actorCell.btnSave.setVisible(false);
            this.actorCell.btnDel.setVisible(false);
        }
    }

    @Override
    public void updateItem(ActorV2 item, boolean empty) {
        //system.out.println("------------ ActorListRectCell update ------------");
        super.updateItem(item, empty);

        this.setText("");
        if (!empty) {
            this.actorCell.setActor(item);
            this.setGraphic(this.actorCell);
        } else {
            this.setGraphic(null);
        }
    }

    static class FXActorCell extends AnchorPane {
        ListCell<ActorV2> parent;

        @FXML
        ImageView imgActor;
        @FXML
        JFXToggleButton btnEdit;
        @FXML
        JFXTextField txtName;
        @FXML
        JFXButton btnSave;
        @FXML
        JFXButton btnDel;
        @FXML
        JFXButton btnImage;

        ActorV2 inActor;

        FXActorCell() {
            GUICommon.loadFXMLRoot(this);

            this.imgActor.setPreserveRatio(true);
            this.txtName.editableProperty().bind(this.btnEdit.selectedProperty());
            this.btnSave.disableProperty().bind(this.txtName.editableProperty().not());
            this.btnDel.disableProperty().bind(this.txtName.editableProperty().not());
            //system.out.println("------------ FXActorCell Cont ------------");
        }

        @FXML
        void saveAction() {
            this.inActor.setName(this.txtName.getText());
            this.btnEdit.setSelected(false);
        }

        @FXML
        void deleteAction() {
            GUICommon.showDialog("Confirmation :", new Text("Are you sure you want to delete the actor?"), "No", "Yes", () -> {
                this.parent.getListView().getItems().remove(this.parent.getItem());
            });
        }

        @FXML
        void editImageAction() {
            if (this.btnEdit.isSelected()) {
                JFXTextField newUrl = new JFXTextField();

                newUrl.setText(this.inActor.getNetImage().map(FxThumb::getThumbURL).map(URL::toString).getOrElse(""));

                GUICommon.showDialog("Edit actor image URL : (Auto Save)", newUrl, "Cancel", "Confirm", () -> {
                    this.btnEdit.setSelected(false);
                    if (newUrl.getText().equals("")) {
                        this.imgActor.setImage(null);
                        this.inActor.clearSource();
                    } else {
                        Systems.useExecutors(() -> {
                            boolean success = false;
                            try {
                                if (FxThumb.fileExistsAtUrl(newUrl.getText())) {
                                    this.inActor.addRecord(ActorV2.Source.LOCAL, "", newUrl.getText(), "");
                                    this.imgActor.setImage(inActor.getImage().map(FxThumb::getImage).getOrNull());
                                    success = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!success) {

                                //Thread.sleep(800);
                                Platform.runLater(() -> {
                                    GUICommon.showDialog("Error :", new Text("Image not found!"), "Close", null, null);
                                });
                            }
                        });
                    }
                });
            }
        }

        void setActor(ActorV2 actor) {
            this.inActor = actor;
            this.txtName.setText(actor.getName());
            this.btnEdit.setSelected(false);
            this.imgActor.setImage(actor.getImage().map(FxThumb::getImage).getOrNull());
        }

        public void setParent(ListCell<ActorV2> parent) {
            this.parent = parent;
        }
    }
}
