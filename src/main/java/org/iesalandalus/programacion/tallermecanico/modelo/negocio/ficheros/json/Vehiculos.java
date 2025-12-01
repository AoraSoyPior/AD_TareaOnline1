package org.iesalandalus.programacion.tallermecanico.modelo.negocio.ficheros.json;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vehiculos implements IVehiculos {

    private static final String FICHERO_VEHICULOS = "datos/ficheros/json/vehiculos.json";

    private static ObjectMapper mapper;
    private static Vehiculos instancia;

    private Vehiculos(){
        mapper = new ObjectMapper();
    }

    static Vehiculos getInstancia(){
        if (instancia == null){
            instancia = new Vehiculos();
        }
        return instancia;
    }


    @Override
    public void comenzar() {}

    @Override
    public void terminar() {}

    public List<Vehiculo> leer() {
        try {
            File fichero = new File(FICHERO_VEHICULOS);
            if (!fichero.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(fichero, new TypeReference<List<Vehiculo>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo vehiculos.json: " + e.getMessage(), e);
        }
    }

    public void escribir(List<Vehiculo> vehiculos) {
        try {
            File fichero = new File(FICHERO_VEHICULOS);
            fichero.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, vehiculos);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo vehiculos.json: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Vehiculo> get() {
        return leer();
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede insertar un vehículo nulo.");
        List<Vehiculo> vehiculos = leer();
        if (vehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("Ya existe un vehículo con esa matrícula.");
        }
        vehiculos.add(vehiculo);
        escribir(vehiculos);
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede buscar un vehículo nulo.");
        return leer().stream()
                .filter(v -> v.equals(vehiculo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede borrar un vehículo nulo.");
        List<Vehiculo> vehiculos = leer();
        boolean eliminado = vehiculos.remove(vehiculo);
        if (!eliminado) {
            throw new TallerMecanicoExcepcion("No existe ningún vehículo con esa matrícula.");
        }
        escribir(vehiculos);
    }
}
