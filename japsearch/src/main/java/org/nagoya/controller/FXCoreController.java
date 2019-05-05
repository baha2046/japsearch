package org.nagoya.controller;

import io.vavr.Tuple2;
import io.vavr.collection.Vector;
import javafx.beans.value.ChangeListener;
import org.jetbrains.annotations.Contract;
import org.nagoya.GUICommon;
import org.nagoya.system.FXContext;

public class FXCoreController {

    private static FXCoreController instance = new FXCoreController();

    private Vector<Tuple2<FXContext, ChangeListener>> controllerVector = Vector.empty();
    private Vector<Tuple2<String, FXContext>> eventListener = Vector.empty();

    private FXCoreController() {
    }

    @Contract(pure = true)
    public static FXCoreController getInstance() {
        return instance;
    }


    public void initialize() {
        GUICommon.debugMessage("fxCoreController.class initialize start");

        GUICommon.debugMessage("fxCoreController.class initialize complete");
    }

    public void addContext(FXContext fxContext) {

        fxContext.registerListener();
    }

    /*
    public <R,T> void addContext(FXContext<R,T> fxContext) {
        ChangeListener<R> changeListener = (o, oV, nV) -> this.changeEventHandler(fxContext.getClass().getName(), nV);
        fxContext.addListener(changeListener);

        Vector<String> stringVector = fxContext.getListenList();
        if (!stringVector.equals(Vector.empty())) {
            stringVector.forEach((name) -> this.eventListener = this.eventListener.append(Tuple.of(name, fxContext)));
            this.controllerVector = this.controllerVector.append(Tuple.of(fxContext, changeListener));
        }
    }

    @SuppressWarnings("unchecked")
    public void removeContext(FXContext fxContext)
    {
        this.eventListener = this.eventListener.filter(el-> !el._2().equals(fxContext));
        this.controllerVector.find(cx->cx._1().equals(fxContext)).peek(cx->cx._1().removeListener(cx._2()));
        this.controllerVector = this.controllerVector.filter(cx-> !cx._1().equals(fxContext));
    }

    @SuppressWarnings("unchecked")
    private void changeEventHandler(String className, Object object) {
        this.eventListener.filter(el -> el._1().equals(className)).peek(el -> el._2().updateEvent(el._1(), object));
    }*/

}
