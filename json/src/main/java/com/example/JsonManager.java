package com.example;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO Ajouter une interface et tester les fonctions
public class JsonManager {
    // TODO enlever certains attributs
    private String sourceUrl;
    private String targetUrl;
    private JSONObject originalJson;
    private JSONArray originalItems;
    private JSONObject rearrangedJson;

    public JsonManager(String _originUrl, String _targetUrl) {
        this.sourceUrl = _originUrl;
        this.targetUrl = _targetUrl;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public String getTargetUrl() {
        return this.targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public JSONObject getOriginalJson() {
        return this.originalJson;
    }

    public void setOriginalJson(JSONObject originalJson) {
        this.originalJson = originalJson;
    }

    public JSONArray getOriginalItems() {
        return this.originalItems;
    }

    public void setOriginalItems(JSONArray originalItems) {
        this.originalItems = originalItems;
    }

    public JSONObject getRearrangedJson() {
        return this.rearrangedJson;
    }

    public void setRearrangedJson(JSONObject rearrangedJson) {
        this.rearrangedJson = rearrangedJson;
    }

    public void setSourceUrl(String _sourceUrl) {
        this.sourceUrl = _sourceUrl;
    }

    public void sendJSONAndSaveResponse(String responseFilePath) throws IOException {
        // Ouvrir une connexion à l'URL cible
        URL target = new URL(targetUrl);
        HttpURLConnection targetConnection = (HttpURLConnection) target.openConnection();
        targetConnection.setRequestMethod("POST"); // Utilisez POST pour envoyer le JSON
    
        // Définir les en-têtes de la requête
        targetConnection.setRequestProperty("Content-Type", "application/json");
        targetConnection.setDoOutput(true);
    
        // Écrire le JSON modifié dans la requête vers l'URL cible
        try (OutputStream os = targetConnection.getOutputStream()) {
            byte[] input = rearrangedJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        // Récupérer la réponse de l'URL cible
        try (BufferedReader targetReader = new BufferedReader(new InputStreamReader(targetConnection.getInputStream()))) {
            StringBuilder targetResponse = new StringBuilder();
            String targetLine;
            while ((targetLine = targetReader.readLine()) != null) {
                targetResponse.append(targetLine);
            }
    
            // Écrire la réponse dans un fichier local
            try (FileWriter fileWriter = new FileWriter(responseFilePath)) {
                fileWriter.write(targetResponse.toString());
            }
            
            System.out.println("Réponse de l'URL cible sauvegardée dans le fichier : " + responseFilePath);

            targetReader.close();
        }
    
        // Fermer la connexion
        targetConnection.disconnect();
    }

    public void receiveJSON() throws IOException {
        // Ouvrir une connexion à l'URL source
        URL url = new URL(sourceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Lire le JSON depuis l'URL source
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Le JSON récupéré depuis l'URL source
        String jsonFromSource = response.toString();

        originalJson = new JSONObject(jsonFromSource);
        originalItems = originalJson.getJSONArray("items");
    }

    public void transformJSON() {
        JSONArray newItems = new JSONArray();
        for (int i = 0; i < originalItems.length(); i++) {
            JSONObject originalItem = originalItems.getJSONObject(i);
            JSONObject newItem = new JSONObject();
            newItem.put("id", originalItem.getInt("id"));

            if (originalItem.getString("subject").equals("km->m")) {
                double value = originalItem.getDouble("value") * 1000;
                DecimalFormat decimalFormat = new DecimalFormat("#.######");
                String formattedValue = decimalFormat.format(value);
                formattedValue = formattedValue.replace(',', '.');
                double valeurDouble = Double.parseDouble(formattedValue);
                newItem.put("value", Double.valueOf(valeurDouble));
            } else if (originalItem.getString("subject").equals("hour")) {
                String originalValue = originalItem.getString("value");
                boolean isTimeFormatValid = FormatHandler.isTimeFormatValid(originalValue);
                newItem.put("value", isTimeFormatValid);
            } else if (originalItem.getString("subject").equals("m->km")) {
                double value = originalItem.getDouble("value") / 1000;
                DecimalFormat decimalFormat = new DecimalFormat("#.######");
                String formattedValue = decimalFormat.format(value);
                formattedValue = formattedValue.replace(',', '.');
                double valeurDouble = Double.parseDouble(formattedValue);
                newItem.put("value", Double.valueOf(valeurDouble));
            } else if (originalItem.getString("subject").equals("knot->km/h")) {
                double value = originalItem.getDouble("value") * 1.852;
                DecimalFormat decimalFormat = new DecimalFormat("#.######");
                String formattedValue = decimalFormat.format(value);
                formattedValue = formattedValue.replace(',', '.');
                double valeurDouble = Double.parseDouble(formattedValue);
                newItem.put("value", Double.valueOf(valeurDouble));
            } else if (originalItem.getString("subject").equals("km/h->knot")) {
                double value = originalItem.getDouble("value") / 1.852;
                DecimalFormat decimalFormat = new DecimalFormat("#.######");
                String formattedValue = decimalFormat.format(value);
                formattedValue = formattedValue.replace(',', '.');
                double valeurDouble = Double.parseDouble(formattedValue);
                newItem.put("value", Double.valueOf(valeurDouble));
            } else if (originalItem.getString("subject").equals("dms->dd")) { // to correct
                String dmsValue = originalItem.getString("value");
                double ddValue = FormatHandler.convertDMSToDD(dmsValue);
                newItem.put("value", ddValue);
            } else if (originalItem.getString("subject").equals("dd->dms")) { // to correct
                double ddValue = originalItem.getDouble("value");
                String dmsValue = FormatHandler.convertDDtoDMS(ddValue);
                newItem.put("value", dmsValue);
            } else if (originalItem.getString("subject").equals("nmea")) { // to correct
                String originalValue = originalItem.getString("value");
                boolean isNMEAFormatValid = FormatHandler.isNMEAFormatValid(originalValue);
                newItem.put("value", isNMEAFormatValid);
            }

            newItems.put(newItem);
        }

        rearrangedJson = new JSONObject();
        rearrangedJson.put("items", newItems);
        rearrangedJson.put("token", originalJson.getString("token"));
    }

    public void saveJSONToLocalFile(JSONObject jsonFile, String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedJson = gson.toJson(jsonFile);

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(formattedJson);
        }

        System.out.println("Réponse JSON sauvegardée dans le fichier : " + filePath);
    }
}