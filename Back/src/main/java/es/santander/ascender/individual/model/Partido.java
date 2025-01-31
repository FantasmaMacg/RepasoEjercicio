package es.santander.ascender.individual.model;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class Partido {
    private long id;

    @Size(max = 30)
    private String nombre;
    
    @Size(max = 150)
    @NonNull
    private String descripcion;
    
    @NonNull
    @Size(max = 30)
    private String deporte;

    @Min(value = -1)
    @Max(value = 1)
    private int resultado;
    
    @Min(value = 0)
    private float apuesta;

    private String nombreFichero;

    public Partido() {
    }

    public Partido(long id, @Size(max = 30) String nombre, @Size(max = 150) String descripcion,
    @Size(max = 30) String deporte, 
    @Min(value = -1) @Max(value = 1) int resultado, 
    @Min(value = 0) float apuesta, String nombreFichero) {    
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.deporte = deporte;
    this.resultado = resultado;
    this.apuesta = apuesta;
    this.nombreFichero = nombreFichero; // Asignar el nombre del fichero
    }
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDeporte() {
        return deporte;
    }
    public void setDeporte(String deporte) {
        this.deporte = deporte;
    }
    public int getResultado() {
        return resultado;
    }
    public void setResultado(int resultado) {
        this.resultado = resultado;
    }
    public float getApuesta() {
        return apuesta;
    }
    public void setApuesta(float apuesta) {
        this.apuesta = apuesta;
    }
    public String getNombreFichero() {
        return nombreFichero;
    }
    public void setNombreFichero(String nombreFichero) {
        this.nombreFichero = nombreFichero;
    }
}
