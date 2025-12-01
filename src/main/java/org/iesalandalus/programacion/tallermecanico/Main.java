package org.iesalandalus.programacion.tallermecanico;

import javafx.util.Pair;
import org.iesalandalus.programacion.tallermecanico.controlador.Controlador;
import org.iesalandalus.programacion.tallermecanico.controlador.IControlador;
import org.iesalandalus.programacion.tallermecanico.modelo.FabricaModelo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.FabricaFuenteDatos;
import org.iesalandalus.programacion.tallermecanico.vista.FabricaVista;

public class Main {
    public static void main(String[] args) {
        Pair<FabricaVista, FabricaFuenteDatos> fabricas = procesarArgumentosFuenteDatos(args);
        IControlador controlador = new Controlador(FabricaModelo.CASCADA, fabricas.getValue(), fabricas.getKey());
        controlador.comenzar();
    }

    private static Pair<FabricaVista, FabricaFuenteDatos> procesarArgumentosFuenteDatos(String[] args) {
        // Por defecto: Vista ventanas y fuente de datos JSON
        FabricaVista fabricaVista = FabricaVista.VENTANAS;
        FabricaFuenteDatos fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_JSON;

        for (String argumento : args) {
            if (argumento.equalsIgnoreCase("-vventanas")) {
                fabricaVista = FabricaVista.VENTANAS;
            } else if (argumento.equalsIgnoreCase("-vtexto")) {
                fabricaVista = FabricaVista.TEXTO;
            } else if (argumento.equalsIgnoreCase("-fdficherosxml")) {
                fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_XML;
            } else if (argumento.equalsIgnoreCase("-fdficherosjson")) {
                fabricaFuenteDatos = FabricaFuenteDatos.FICHEROS_JSON;
            }
        }

        return new Pair<>(fabricaVista, fabricaFuenteDatos);
    }
}
