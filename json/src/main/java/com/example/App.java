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

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */

    public static void main(String[] args) {
        try {
            // URL source du JSON à récupérer
            String sourceUrl = "https://secret-challenge.sas-impact.fr/7224e833a764590ec0dc6ac65fdee7e7f200322a2/play";
            
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
            
            // Analyser le JSON d'origine
            JSONObject originalJson = new JSONObject(jsonFromSource);
            JSONArray originalItems = originalJson.getJSONArray("items");

            // Sauvegarder la réponse JSON dans un fichier
            try {
                String filePath = "./origine.json"; // Spécifiez le chemin et le nom du fichier de sortie.
                FileWriter fileWriter = new FileWriter(filePath);

                // Écrivez la réponse JSON dans le fichier.
                fileWriter.write(originalJson.toString());

                // Fermez le fichier FileWriter pour finaliser l'écriture.
                fileWriter.close();

                System.out.println("Réponse JSON sauvegardée dans le fichier : " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Créer un nouveau JSON avec le format souhaité
            JSONObject newJson = new JSONObject();
            
            
            JSONArray newItems = new JSONArray();
            for (int i = 0; i < originalItems.length(); i++) {
                JSONObject originalItem = originalItems.getJSONObject(i);
                JSONObject newItem = new JSONObject();
                newItem.put("id", originalItem.getInt("id"));
                
                // Modifier la valeur selon les besoins
                if (originalItem.getString("subject").equals("km->m")) {
                    double value = originalItem.getDouble("value") * 1000;
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");
                    String formattedValue = decimalFormat.format(value);
                    newItem.put("value", String.valueOf(formattedValue));
                } else if (originalItem.getString("subject").equals("hour")) {
                    String originalValue = originalItem.getString("value");
                    boolean isTimeFormatValid = isTimeFormatValid(originalValue);

                    // Modifier en fonction de la logique nécessaire
                    newItem.put("value", String.valueOf(isTimeFormatValid));
                } else if (originalItem.getString("subject").equals("m->km")) {
                    double value = originalItem.getDouble("value") / 1000;
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");
                    String formattedValue = decimalFormat.format(value);
                    newItem.put("value", String.valueOf(formattedValue));
                } else if (originalItem.getString("subject").equals("knot->km/h")) {
                    double value = originalItem.getDouble("value") * 1.852;
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");
                    String formattedValue = decimalFormat.format(value);
                    newItem.put("value", String.valueOf(formattedValue));
                } else if (originalItem.getString("subject").equals("km/h->knot")) {
                    double value = originalItem.getDouble("value") / 1.852;
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");
                    String formattedValue = decimalFormat.format(value);
                    newItem.put("value", String.valueOf(formattedValue));
                } else if (originalItem.getString("subject").equals("dms->dd")) {
                    String dmsValue = originalItem.getString("value");
                    double ddValue = convertDMSToDD(dmsValue);
                    newItem.put("value", String.valueOf(ddValue));
                } else if (originalItem.getString("subject").equals("dd->dms")) {
                    double ddValue = originalItem.getDouble("value");
                    String dmsValue = convertDDtoDMS(ddValue);
                    newItem.put("value", dmsValue);
                } else if (originalItem.getString("subject").equals("nmea")) {
                    String originalValue = originalItem.getString("value");
                    boolean isNMEAFormatValid = isNMEAFormatValid(originalValue);
                    newItem.put("value", String.valueOf(isNMEAFormatValid));
                }

                newItems.put(newItem);
            }
            
            newJson.put("items", newItems);
            newJson.put("token", originalJson.getString("token"));

            JSONObject rearrangedJson = new JSONObject();

            // Copiez la clé "token" de l'original vers le nouvel objet JSON.
            rearrangedJson.put("token", originalJson.get("token"));
        
            // Copiez la clé "items" de l'original vers le nouvel objet JSON.
            rearrangedJson.put("items", originalJson.get("items"));

            // Sauvegarder la réponse JSON dans un fichier
            try {
                String filePath = "./cible.json"; // Spécifiez le chemin et le nom du fichier de sortie.
                FileWriter fileWriter = new FileWriter(filePath);

                // Écrivez la réponse JSON dans le fichier.
                fileWriter.write(rearrangedJson.toString());

                // Fermez le fichier FileWriter pour finaliser l'écriture.
                fileWriter.close();

                System.out.println("Réponse JSON sauvegardée dans le fichier : " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // URL cible où envoyer le JSON modifié
            String targetUrl = "https://secret-challenge.sas-impact.fr/7224e833a764590ec0dc6ac65fdee7e7f200322a2/submit";
            
            // Ouvrir une connexion à l'URL cible
            URL target = new URL(targetUrl);
            HttpURLConnection targetConnection = (HttpURLConnection) target.openConnection();
            targetConnection.setRequestMethod("POST"); // Utilisez POST pour envoyer le JSON
            
            // Définir les en-têtes de la requête
            targetConnection.setRequestProperty("Content-Type", "application/json");
            targetConnection.setDoOutput(true);
            
            // Écrire le JSON modifié dans la requête vers l'URL cible
            OutputStream os = targetConnection.getOutputStream();
            byte[] input = rearrangedJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            
            // Récupérer la réponse de l'URL cible (si nécessaire)
            BufferedReader targetReader = new BufferedReader(new InputStreamReader(targetConnection.getInputStream()));
            StringBuilder targetResponse = new StringBuilder();
            String targetLine;
            while ((targetLine = targetReader.readLine()) != null) {
                targetResponse.append(targetLine);
            }

            // Sauvegarder la réponse JSON dans un fichier
            try {
                String filePath = "./response.json"; // Spécifiez le chemin et le nom du fichier de sortie.
                FileWriter fileWriter = new FileWriter(filePath);

                // Écrivez la réponse JSON dans le fichier.
                fileWriter.write(targetResponse.toString());

                // Fermez le fichier FileWriter pour finaliser l'écriture.
                fileWriter.close();

                System.out.println("Réponse JSON sauvegardée dans le fichier : " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            targetReader.close();
            
            // Fermer la connexion
            targetConnection.disconnect();
            
            // Afficher la réponse de l'URL cible (si nécessaire)
            System.out.println("Réponse de l'URL cible : " + targetResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fonction pour vérifier si la valeur est au format d'heure correct
    private static boolean isTimeFormatValid(String timeString) {
        // Vous pouvez utiliser une expression régulière pour vérifier le format de l'heure.
        // Par exemple, vérifier si la chaîne est au format "hh:mm".
        // Cette expression régulière suppose que l'heure est au format 24 heures.
        String timePattern = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
        return timeString.matches(timePattern);
    }

    // Fonction pour convertir degrés, minutes, secondes (DMS) en degrés décimaux (DD)
    private static double convertDMSToDD(String dmsValue) {
        dmsValue = dmsValue.replaceAll("[^0-9.]", "");

        // Vous pouvez maintenant continuer avec la logique de conversion
        // Exemple : si dmsValue est "16157.51", vous pouvez le convertir en DD.
        // ...

        // Assurez-vous de gérer les cas positifs et négatifs et degrés décimaux.
        // À titre d'exemple, voici une implémentation simple qui ne gère que les valeurs positives :
        double ddValue = Double.parseDouble(dmsValue) / 100;
        return ddValue;
    }

    // Fonction pour convertir degrés décimaux (DD) en degrés, minutes, secondes (DMS)
    private static String convertDDtoDMS(double ddValue) {
        // Vous devez implémenter la logique de conversion ici.
        // Exemple : si ddValue est 45.504167, vous devez le convertir en DMS.
        // Vous pouvez calculer les degrés, les minutes et les secondes en fonction de ddValue.
        // Assurez-vous de gérer les cas positifs et négatifs et degrés décimaux.
        // À titre d'exemple, voici une implémentation simple qui ne gère que les valeurs positives :
        int degrees = (int) ddValue;
        double minutesDouble = (ddValue - degrees) * 60;
        int minutes = (int) minutesDouble;
        double seconds = (minutesDouble - minutes) * 60;
        String dmsValue = String.format("%d°%d'%f\"", degrees, minutes, seconds);
        return dmsValue;
    }

    private static boolean isNMEAFormatValid(String nmeaValue) {
    // Vous devez implémenter la logique de validation du format NMEA ici.
    // Vérifiez si la chaîne respecte le format NMEA.
    // Vous pouvez utiliser des expressions régulières ou d'autres méthodes pour effectuer cette vérification.
    // Assurez-vous de gérer les cas de format corrects et incorrects.
    // À titre d'exemple, voici une vérification simple :
    // Le format NMEA commence généralement par "$" suivi de données ASCII et se termine par "*XX" où XX est un checksum en hexadécimal.
    return nmeaValue.matches("^\\$[^\\*]+\\*([0-9A-Fa-f]{2})$");
    }

}
