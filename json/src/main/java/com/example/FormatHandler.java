package com.example;

import java.util.Locale;

// TODO Ajouter une interface et tester les fonctions
public class FormatHandler {

    // Fonction pour vérifier si la valeur est au format d'heure correct
    public static boolean isTimeFormatValid(String timeString) {
        return timeString.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    // Fonction pour (DMS) en (DD)
    public static double convertDMSToDD(String dmsValue) {
        String[] parts = dmsValue.split("[^0-9.-]+");
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "La chaîne DMS doit contenir des degrés, minutes et secondes. :( ");
        }

        // Conversion des parties en double
        double degrees = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);

        // Calcul de la valeur décimale (DD)
        double ddValue = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return ddValue;
    }

    // Fonction pour convertir (DD) en (DMS)
    public static String convertDDtoDMS(double ddValue) {
        int degrees = (int) Math.abs(ddValue);
        double minutesDouble = (Math.abs(ddValue) - degrees) * 60;
        int minutes = (int) minutesDouble;
        double seconds = (minutesDouble - minutes) * 60;

        // Ajouter le signe négatif au degré si besoin
        String degreeSign = (ddValue < 0) ? "-" : "";

        // Formatte les secondes
        String dmsValue = String.format(Locale.US, "%s%d°%d'%f''", degreeSign, degrees, minutes, seconds);

        // Remplace les virgules par des points
        dmsValue = dmsValue.replace(",", ".");

        return dmsValue;
    }

    // TODO Simplifier la fonction 
    public static boolean isNMEAFormatValid(String input) {
        if (input == null || input.isEmpty() || !input.startsWith("$") || input.length() < 7) {
            return false;
        }

        // Extrait la somme de contrôle hexadécimale (2 caractères) du message NMEA
        String checksum = input.substring(input.lastIndexOf('*') + 1);

        // Vérifie si la somme de contrôle est au format hexadécimal
        try {
            int checksumValue = Integer.parseInt(checksum, 16);
            String sentenceWithoutChecksum = input.substring(1, input.lastIndexOf('*'));
            int calculatedChecksum = 0;
            for (char c : sentenceWithoutChecksum.toCharArray()) {
                calculatedChecksum ^= c;
            }
            return checksumValue == calculatedChecksum;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}