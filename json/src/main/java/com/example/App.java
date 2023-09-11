package com.example;

import java.io.IOException;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws IOException {
        String sourceUrl = "https://secret-challenge.sas-impact.fr/7224e833a764590ec0dc6ac65fdee7e7f200322a2/play";
        String targetUrl = "https://secret-challenge.sas-impact.fr/7224e833a764590ec0dc6ac65fdee7e7f200322a2/submit";

        JsonManager jsonManager = new JsonManager(sourceUrl, targetUrl);

        jsonManager.receiveJSON();

        long startTime = System.currentTimeMillis(); // Enregistrez le temps de début
        jsonManager.saveJSONToLocalFile(jsonManager.getOriginalJson(), "./origin.json");
        jsonManager.transformJSON();
        long endTime = System.currentTimeMillis(); // Enregistrez le temps de fin
        jsonManager.saveJSONToLocalFile(jsonManager.getRearrangedJson(), "./final.json");
        jsonManager.sendJSONAndSaveResponse("./response.json");

        long totalTime = endTime - startTime; // Calculez le temps total en millisecondes

        // Calculez les secondes et les millisecondes
        long seconds = totalTime / 1000;
        long milliseconds = totalTime % 1000;

        // Affichez le temps total en secondes et millisecondes
        System.out.println("Temps de traitement : " + seconds + " secondes " + milliseconds + " millisecondes");
    }
}
