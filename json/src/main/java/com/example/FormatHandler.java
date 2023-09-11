package com.example;

import java.util.Locale;

public class FormatHandler {

    // Fonction pour vérifier si la valeur est au format d'heure correct
    public static boolean isTimeFormatValid(String timeString) {
        String timePattern = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
        return timeString.matches(timePattern);
    }

    // Fonction pour convertir degrés, minutes, secondes (DMS) en degrés décimaux
    // (DD)
    public static double convertDMSToDD(String dmsValue) {
        // Utilisation d'une expression régulière pour extraire les chiffres, les points
        // et le signe négatif
        String[] parts = dmsValue.split("[^0-9.-]+");

        // Vérification du nombre de parties pour s'assurer de la présence des
        // composants DMS
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "La chaîne DMS doit contenir des composants de degrés, minutes et secondes.");
        }

        // Conversion des parties en double
        double degrees = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);

        // Calcul de la valeur décimale (DD)
        double ddValue = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return ddValue;
    }

    // Fonction pour convertir degrés décimaux (DD) en degrés, minutes, secondes
    // (DMS)
    public static String convertDDtoDMS(double ddValue) {
        int degrees = (int) Math.abs(ddValue);
        double minutesDouble = (Math.abs(ddValue) - degrees) * 60;
        int minutes = (int) minutesDouble;
        double seconds = (minutesDouble - minutes) * 60;

        // Ajouter le signe négatif au degré si nécessaire
        String degreeSign = (ddValue < 0) ? "-" : "";

        // Format seconds with a period (.) as the decimal separator
        String dmsValue = String.format(Locale.US, "%s%d°%d'%f''", degreeSign, degrees, minutes, seconds);

        // Remplace les virgules par des points pour le séparateur décimal
        dmsValue = dmsValue.replace(",", ".");

        return dmsValue;
    }

    public static boolean isNMEAFormatValid(String nmeaValue) {
        // Condition pour accepter les valeurs NMEA avec une lettre à la fin
        return nmeaValue.matches("^\\$[A-Z]+,[0-9.]+,[0-9.]+\\*[0-9A-Fa-f]{2}[A-Za-z]$");
    }
}