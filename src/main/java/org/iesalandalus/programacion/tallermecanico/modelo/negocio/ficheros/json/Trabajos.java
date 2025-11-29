package org.iesalandalus.programacion.tallermecanico.modelo.negocio.ficheros.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Trabajos implements ITrabajos {

    private static final String FICHERO_TRABAJOS = "datos/ficheros/json/trabajos.json";

    private static Trabajos instancia;

    private final ObjectMapper mapper;

    private Trabajos() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        // No hace falta cargar nada al inicio
    }

    @Override
    public void terminar() {
        // No hace falta guardar nada al final
    }

    private List<Trabajo> leer() {
        try {
            File fichero = new File(FICHERO_TRABAJOS);
            if (!fichero.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(fichero, new TypeReference<List<Trabajo>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo trabajos.json: " + e.getMessage(), e);
        }
    }

    private void escribir(List<Trabajo> trabajos) {
        try {
            File fichero = new File(FICHERO_TRABAJOS);
            fichero.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, trabajos);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo trabajos.json: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Trabajo> get() {
        return leer();
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo.");
        List<Trabajo> trabajosCliente = new ArrayList<>();
        for (Trabajo trabajo : leer()) {
            if (trabajo.getCliente().equals(cliente)) {
                trabajosCliente.add(trabajo);
            }
        }
        return trabajosCliente;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "El vehículo no puede ser nulo.");
        List<Trabajo> trabajosVehiculo = new ArrayList<>();
        for (Trabajo trabajo : leer()) {
            if (trabajo.getVehiculo().equals(vehiculo)) {
                trabajosVehiculo.add(trabajo);
            }
        }
        return trabajosVehiculo;
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Objects.requireNonNull(mes, "El mes no puede ser nulo.");
        Map<TipoTrabajo, Integer> estadisticas = inicializarEstadisticas();
        for (Trabajo trabajo : leer()) {
            LocalDate fecha = trabajo.getFechaInicio();
            if (fecha.getMonthValue() == mes.getMonthValue() && fecha.getYear() == mes.getYear()) {
                TipoTrabajo tipoTrabajo = TipoTrabajo.get(trabajo);
                estadisticas.put(tipoTrabajo, estadisticas.get(tipoTrabajo) + 1);
            }
        }
        return estadisticas;
    }

    private Map<TipoTrabajo, Integer> inicializarEstadisticas() {
        Map<TipoTrabajo, Integer> estadisticas = new EnumMap<>(TipoTrabajo.class);
        for (TipoTrabajo tipoTrabajo : TipoTrabajo.values()) {
            estadisticas.put(tipoTrabajo, 0);
        }
        return estadisticas;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        comprobarTrabajo(trabajo.getCliente(), trabajo.getVehiculo(), trabajo.getFechaInicio(), trabajos);
        trabajos.add(trabajo);
        escribir(trabajos);
    }

    private void comprobarTrabajo(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio, List<Trabajo> trabajos) throws TallerMecanicoExcepcion {
        for (Trabajo trabajo : trabajos) {
            if (!trabajo.estaCerrado()) {
                if (trabajo.getCliente().equals(cliente)) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo en curso.");
                } else if (trabajo.getVehiculo().equals(vehiculo)) {
                    throw new TallerMecanicoExcepcion("El vehículo está actualmente en el taller.");
                }
            } else {
                if (trabajo.getCliente().equals(cliente) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo posterior.");
                } else if (trabajo.getVehiculo().equals(vehiculo) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El vehículo tiene otro trabajo posterior.");
                }
            }
        }
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir horas a un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);
        trabajoEncontrado.anadirHoras(horas);
        escribir(trabajos);
        return trabajoEncontrado;
    }

    private Trabajo getTrabajoAbierto(Vehiculo vehiculo, List<Trabajo> trabajos) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No puedo operar sobre un vehículo nulo.");
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getVehiculo().equals(vehiculo) && !trabajo.estaCerrado()) {
                return trabajo;
            }
        }
        throw new TallerMecanicoExcepcion("No existe ningún trabajo abierto para dicho vehículo.");
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir precio del material a un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);
        if (trabajoEncontrado instanceof Mecanico mecanico) {
            mecanico.anadirPrecioMaterial(precioMaterial);
        } else {
            throw new TallerMecanicoExcepcion("No se puede añadir precio al material para este tipo de trabajos.");
        }
        escribir(trabajos);
        return trabajoEncontrado;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo cerrar un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);
        trabajoEncontrado.cerrar(fechaFin);
        escribir(trabajos);
        return trabajoEncontrado;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        int indice = trabajos.indexOf(trabajo);
        return (indice == -1) ? null : trabajos.get(indice);
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        boolean eliminado = trabajos.remove(trabajo);
        if (!eliminado) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo igual.");
        }
        escribir(trabajos);
    }
}
