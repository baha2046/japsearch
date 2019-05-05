package org.nagoya.system;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A pool of {@link JFXDialog}
 *
 * @author Eric Chan
 * @version 1.0
 * @since 2018-11-15
 */
public class JFXDialogPool {

    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/
    private final List<JFXDialog> dialogs = new ArrayList<>();


    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/
    public JFXDialogPool() {
    }

    public JFXDialogPool(int size, StackPane stackPane) {
        this.initialize(size, stackPane);
    }

    /**************************************************************************
     *
     * Methods
     *
     **************************************************************************/

    public void initialize(int size, StackPane stackPane) {
        for (int x = 0; x < size; x++) {
            this.dialogs.add(new JFXDialog(stackPane, null, /*JFXDialog.DialogTransition.CENTER*/ JFXDialog.DialogTransition.NONE));
        }
    }

    public int size() {
        return this.dialogs.size();
    }

    public synchronized JFXDialog taketDialog() {
        if (this.size() == 0) {
            System.out.println("* JFXDialogPool::getAvailableDialog <<ERROR>> not available *");
            return null;
        }
        return this.dialogs.remove(0);
    }

    public void returnDialog(JFXDialog dialog) {
        if (dialog.isVisible()) {
            dialog.setOnDialogClosed((e) -> this.dialogs.add((JFXDialog) e.getSource()));
            dialog.close();
        } else {
            this.dialogs.add(dialog);
        }
    }

    public void showDialog(String heading, Node body, String strBtnCancel, String strBtnOkay, Runnable runnable) {
        this.showDialog(this.taketDialog(), heading, body, strBtnCancel, strBtnOkay, runnable);
    }

    public void showDialog(JFXDialog useDialog, String heading, Node body, String strBtnCancel, String strBtnOkay, Runnable runnable) {
        if (useDialog == null) {
            System.out.println("* JFXDialogPool::showDialog <<ERROR>> no dialog given *");
            return;
        }

        if (heading.length() > 90) {
            heading = heading.substring(0, 90) + "...";
        }

        if(body == null) body = new Text();

        JFXDialogLayout content = new JFXDialogLayout();

        if(!heading.equals("")) content.setHeading(new Text(heading));
        content.setBody(body);

        useDialog.setContent(content);
        useDialog.setOnDialogClosed((e) -> this.dialogs.add((JFXDialog) e.getSource()));

        JFXButton buttonCancel = new JFXButton(strBtnCancel);
        buttonCancel.setOnAction(event -> useDialog.close());

        if (null == strBtnOkay) {
            content.setActions(buttonCancel);
        } else {
            JFXButton buttonOk = new JFXButton(strBtnOkay);
            buttonOk.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE));
            buttonOk.setStyle(" -fx-border-color: #AAAAAA; -fx-border-insets: 1; -fx-border-radius: 4;");
            buttonOk.setOnAction(event -> {
                useDialog.close();
                if (runnable != null) {
                    runnable.run();
                }
            });
            content.setActions(buttonCancel, buttonOk);
        }

        useDialog.show();
    }
}
