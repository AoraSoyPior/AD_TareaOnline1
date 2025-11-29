package org.iesalandalus.programacion.tallermecanico.modelo.negocio.ficheros.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clientes implements IClientes {

    private static final String FICHERO_CLIENTES = "datos/ficheros/json/clientes.json";

    private static ObjectMapper mapper;
    private static Clientes instancia;

    private Clientes(){ mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); }

    static Clientes getInstancia(){
        if (instancia == null){
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {}

    @Override
    public void terminar() {}

    private List<Cliente> leer() {
        try {
            File fichero = new File(FICHERO_CLIENTES);
            if (!fichero.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(fichero, new TypeReference<List<Cliente>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo clientes.json: " + e.getMessage());
        }
    }

    private void escribir(List<Cliente> clientes) throws TallerMecanicoExcepcion {
        try {
            File fichero = new File(FICHERO_CLIENTES);
            fichero.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, clientes);
        } catch (IOException e) {
            throw new TallerMecanicoExcepcion("Error escribiendo clientes.json: " + e.getMessage());
        }
    }

    @Override
    public List<Cliente> get() {
        return leer();
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede insertar un cliente nulo.");
        List<Cliente> clientes = leer();
        if (clientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }
        clientes.add(cliente);
        escribir(clientes);
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede modificar un cliente nulo.");
        List<Cliente> clientes = leer();
        Cliente clienteModificado = null;

        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            if (c.equals(cliente)) {
                String nuevoNombre = (nombre != null && !nombre.isBlank()) ? nombre : c.getNombre();
                String nuevoTelefono = (telefono != null && !telefono.isBlank()) ? telefono : c.getTelefono();
                clienteModificado = new Cliente(nuevoNombre, c.getDni(), nuevoTelefono);
                clientes.set(i, clienteModificado);
                break;
            }
        }

        if (clienteModificado == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        escribir(clientes);
        return clienteModificado;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede buscar un cliente nulo.");
        return leer().stream()
                .filter(c -> c.equals(cliente))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        List<Cliente> clientes = leer();
        boolean eliminado = clientes.removeIf(c -> c.getDni().equalsIgnoreCase(cliente.getDni()));
        if (!eliminado) {
            throw new IllegalArgumentException("No existe cliente con ese DNI.");
        }
        escribir(clientes);
    }
}
