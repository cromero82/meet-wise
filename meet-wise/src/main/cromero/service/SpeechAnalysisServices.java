package main.cromero.service;

import com.darkprograms.speech.microphone.*;
import com.darkprograms.speech.recognizer.*;
import com.google.gson.*;
import lombok.*;
import net.sourceforge.javaflacencoder.*;

import java.beans.*;


public class SpeechAnalysisServices implements GSpeechResponseListener {

    //region Fields

    final Microphone mic = new Microphone(FLACFileWriter.FLAC);
    GSpeechDuplex duplex;

    Gson gson = new Gson();

    @Getter
    private String currentText = "";

    @Getter
    private String oldText = "";

    private PropertyChangeSupport support;
    //endregion

    //region Initialization and basic actions

    public SpeechAnalysisServices(){
        duplex = new GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
        duplex.setLanguage("en");
        duplex.addResponseListener(this);
        support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void setCurrentText(String currentText) {
        String oldCurrentText = this.currentText;
        this.currentText = currentText;
        support.firePropertyChange("currentText", oldCurrentText, currentText);
    }

    public void setOldText(String oldText) {
        String oldOldText = this.oldText;
        this.oldText = oldText;
        support.firePropertyChange("oldText", oldOldText, oldText);
    }

    public void record(){
        new Thread(() -> {
            try {
                duplex.recognize(mic.getTargetDataLine(), mic.getAudioFormat());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void stop(){
        mic.close();
        duplex.stopSpeechRecognition();
    }
    //endregion

    public void onResponse(GoogleResponse gr) {
        String responseText = gr.getResponse();

        if( gr.isFinalResponse() ){
            setOldText( responseText);
            setCurrentText("");
        }else{
            setCurrentText( responseText);
        }

    }
}