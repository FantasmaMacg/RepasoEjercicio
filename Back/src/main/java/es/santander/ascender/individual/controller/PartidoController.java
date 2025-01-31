package es.santander.ascender.individual.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.santander.ascender.individual.model.Partido;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/partidos")
public class PartidoController {
    // Sin Base de Datos verdadera, no pretende ser ni tan siquiera seguro respecto a hilos
    private Map<Long, Partido> partidos = new HashMap<>();

    public PartidoController() {
        partidos.put(1L, crearPartidoConArchivo(1, "Racing vs Osasuna", "Partido de LaLiga Santander. Racing de Santander recibe a Osasuna en El Sardinero.", "fútbol", 0, 25.0f));
        partidos.put(2L, crearPartidoConArchivo(2, "Nadal vs Djokovic", "Final del Abierto de Australia. ", "tenis", 0, 50.0f));
        partidos.put(3L, crearPartidoConArchivo(3, "Lakers vs Warriors", "Partido de la NBA. ", "baloncesto", 1, 30.0f));
        partidos.put(4L, crearPartidoConArchivo(4, "España vs Argentina", "Partido amistoso de voleibol.", "voleibol", -1, 15.0f));
        partidos.put(5L, crearPartidoConArchivo(5, "All Blacks vs Springboks", "Partido de rugby. Nueva Zelanda (All Blacks) juega contra Sudáfrica (Springboks) ", "rugby", 0, 40.0f));
        partidos.put(6L, crearPartidoConArchivo(6, "Canadá vs Estados Unidos", "Partido de hockey sobre hielo.", "hockey", 0, 20.0f));
    }
    
    private Partido crearPartidoConArchivo(long id, String nombre, String descripcion, String deporte, int resultado, float apuesta) {
        String nombreFichero = "archivo_partido_" + id + ".txt"; // El nombre del archivo sigue usando el ID
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), nombreFichero);
    
        try {
            // Crear un archivo predeterminado con toda la información del partido (sin el ID)
            String contenido = "Nombre: " + nombre + "\n" +
                               "Descripción: " + descripcion + "\n" +
                               "Deporte: " + deporte + "\n" +
                               "Resultado: " + resultado + "\n" +
                               "Apuesta: " + apuesta + "\n";
            Files.write(tempFile, contenido.getBytes());
        } catch (IOException e) {
            System.err.println("Error al crear el archivo predeterminado: " + e.getMessage());
        }
    
        return new Partido(id, nombre, descripcion, deporte, resultado, apuesta, nombreFichero);
    }


    @GetMapping("/{id}")
    public HttpEntity<Partido> get(@PathVariable("id") long id) {
        if (!partidos.containsKey(id)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().body(partidos.get(id));
        }
    }

    @GetMapping
    public HttpEntity<Collection<Partido>> get() {
        return ResponseEntity.ok().body(partidos.values());
    }

    @PostMapping
    public ResponseEntity<Partido> create(@RequestBody Partido partido) {
        // Generar el ID del nuevo partido
        long maxId = partidos.keySet().stream()
                             .mapToLong(id -> id)
                             .max()
                             .orElse(0);
        
        partido.setId(maxId + 1);
    
        // Crear el archivo asociado al partido
        String nombreFichero = "archivo_partido_" + partido.getId() + ".txt";
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), nombreFichero);
    
        try {
            String contenido = "Nombre: " + partido.getNombre() + "\n" +
                               "Descripción: " + partido.getDescripcion() + "\n" +
                               "Deporte: " + partido.getDeporte() + "\n" +
                               "Resultado: " + partido.getResultado() + "\n" +
                               "Apuesta: " + partido.getApuesta() + "\n";
            Files.write(tempFile, contenido.getBytes());
        } catch (IOException e) {
            System.err.println("Error al crear el archivo predeterminado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    
        // Asignar el nombre del archivo al partido
        partido.setNombreFichero(nombreFichero);
    
        // Guardar el partido en el mapa
        partidos.put(partido.getId(), partido);
    
        return ResponseEntity.status(HttpStatus.CREATED).body(partido);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Partido> update(@PathVariable Long id, @Valid @RequestBody Partido partidoActualizado) {
        Partido partidoExistente = partidos.get(id);
        
        if (partidoExistente == null) {
            return ResponseEntity.notFound().build();
        }
    
        // Eliminar el archivo anterior si existe
        if (partidoExistente.getNombreFichero() != null) {
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), partidoExistente.getNombreFichero());
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                System.err.println("Error al eliminar el archivo anterior: " + e.getMessage());
            }
        }
    
        // Actualizar los campos del partido
        partidoExistente.setNombre(partidoActualizado.getNombre());
        partidoExistente.setDescripcion(partidoActualizado.getDescripcion());
        partidoExistente.setDeporte(partidoActualizado.getDeporte());
        partidoExistente.setResultado(partidoActualizado.getResultado());
        partidoExistente.setApuesta(partidoActualizado.getApuesta());
    
        // Crear un nuevo archivo con la información actualizada
        String nombreFichero = "archivo_partido_" + id + ".txt";
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), nombreFichero);
    
        try {
            String contenido = "Nombre: " + partidoExistente.getNombre() + "\n" +
                               "Descripción: " + partidoExistente.getDescripcion() + "\n" +
                               "Deporte: " + partidoExistente.getDeporte() + "\n" +
                               "Resultado: " + partidoExistente.getResultado() + "\n" +
                               "Apuesta: " + partidoExistente.getApuesta() + "\n";
            Files.write(tempFile, contenido.getBytes());
        } catch (IOException e) {
            System.err.println("Error al crear el archivo actualizado: " + e.getMessage());
        }
    
        // Asignar el nuevo nombre del archivo al partido
        partidoExistente.setNombreFichero(nombreFichero);
    
        return ResponseEntity.ok(partidoExistente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Partido partidoExistente = partidos.get(id);

        if (partidoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Eliminar el archivo asociado al partido
        if (partidoExistente.getNombreFichero() != null) {
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), partidoExistente.getNombreFichero());
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                System.err.println("Error al eliminar el archivo asociado: " + e.getMessage());
            }
        }

        partidos.remove(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/gana")
    public ResponseEntity<String> gana(@PathVariable Long id) {
        Partido partido = partidos.get(id);
    
        if (partido == null) {
            return ResponseEntity.notFound().build();
        }
    
        if (partido.getResultado() > 0) {
            return ResponseEntity.badRequest().body("No se puede superar 1");
        }
    
        partido.setResultado(partido.getResultado() + 1);
    
        // Actualizar el archivo .txt asociado
        actualizarArchivoPartido(partido);
    
        return ResponseEntity.ok("Gana: " + partido.getNombre());
    }
    
    @PostMapping("/{id}/pierde")
    public ResponseEntity<String> pierde(@PathVariable Long id) {
        Partido partido = partidos.get(id);
    
        if (partido == null) {
            return ResponseEntity.notFound().build();
        }
    
        if (partido.getResultado() < 0) {
            return ResponseEntity.badRequest().body("No se puede bajar de -1");
        }
    
        partido.setResultado(partido.getResultado() - 1);
    
        // Actualizar el archivo .txt asociado
        actualizarArchivoPartido(partido);
    
        return ResponseEntity.ok("Pierde: " + partido.getNombre());
    }
    
    // Método para actualizar el archivo .txt asociado al partido
    private void actualizarArchivoPartido(Partido partido) {
        String nombreFichero = partido.getNombreFichero();
        if (nombreFichero != null && !nombreFichero.isEmpty()) {
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), nombreFichero);
    
            try {
                String contenido = "Nombre: " + partido.getNombre() + "\n" +
                                   "Descripción: " + partido.getDescripcion() + "\n" +
                                   "Deporte: " + partido.getDeporte() + "\n" +
                                   "Resultado: " + partido.getResultado() + "\n" +
                                   "Apuesta: " + partido.getApuesta() + "\n";
                Files.write(tempFile, contenido.getBytes());
            } catch (IOException e) {
                System.err.println("Error al actualizar el archivo del partido: " + e.getMessage());
            }
        }
    }

    // Exponemos un endpoint donde se pueden subir ficheros que en base al nombre del fichero (prestando atención 
    // a que no haya problemas de seguridad) y que se almacenen en la carpeta temporal del sistema operativo.
    // Para ello, se puede utilizar la clase MultipartFile de Spring.
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // Validaciones básicas del archivo
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }
        if (!file.getOriginalFilename().endsWith(".txt")) {
            return ResponseEntity.badRequest().body("Solo se permiten archivos .txt");
        }
        if (file.getSize() > 1048576) { // 1MB máximo
            return ResponseEntity.badRequest().body("El archivo no puede superar 1MB.");
        }
    
        try {
            // Leer el contenido del archivo
            String contenido = new String(file.getBytes());
            String[] lineas = contenido.split("\n");
    
            // Parsear los datos del archivo
            String nombre = obtenerValor(lineas, "Nombre");
            String descripcion = obtenerValor(lineas, "Descripción");
            String deporte = obtenerValor(lineas, "Deporte");
            int resultado = Integer.parseInt(obtenerValor(lineas, "Resultado"));
            float apuesta = Float.parseFloat(obtenerValor(lineas, "Apuesta"));
    
            // Generar el ID del nuevo partido
            long maxId = partidos.keySet().stream()
                                 .mapToLong(id -> id)
                                 .max()
                                 .orElse(0);
            long nuevoId = maxId + 1;
    
            // Crear el nuevo partido
            Partido nuevoPartido = new Partido(nuevoId, nombre, descripcion, deporte, resultado, apuesta, file.getOriginalFilename());
    
            // Guardar el archivo en el servidor
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, file.getOriginalFilename());
            Files.write(tempFile, file.getBytes());
    
            // Asociar el archivo al partido
            nuevoPartido.setNombreFichero(file.getOriginalFilename());
    
            // Guardar el partido en el mapa
            partidos.put(nuevoId, nuevoPartido);
    
            return ResponseEntity.ok("Partido creado y archivo subido con éxito: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el fichero: " + e.getMessage());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Formato incorrecto en el archivo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar el archivo: " + e.getMessage());
        }
    }
        // Método auxiliar para obtener valores del archivo
    private String obtenerValor(String[] lineas, String clave) {
    for (String linea : lineas) {
        if (linea.startsWith(clave + ":")) {
            return linea.split(":")[1].trim();
        }
    }
    throw new IllegalArgumentException("No se encontró la clave: " + clave);
    }
    // Y ahora descargamos el fichero subido
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("id") long id) {
        Partido partido = partidos.get(id);
        if (partido == null) {
            return ResponseEntity.notFound().build();
        }
    
        String nombreFichero = partido.getNombreFichero();
        if (nombreFichero == null || nombreFichero.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El partido no tiene un archivo asociado.".getBytes());
        }
    
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, nombreFichero);
    
            if (!Files.exists(tempFile)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("El archivo no existe en el servidor.".getBytes());
            }
    
            byte[] contenido = Files.readAllBytes(tempFile);
    
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + nombreFichero + "\"")
                    .body(contenido);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error al descargar el archivo: " + e.getMessage()).getBytes());
        }
    }

    public Map<Long, Partido> getPartidos() {
        return partidos;
    }

    public void setPartidos(Map<Long, Partido> partidos) {
        this.partidos = partidos;
    }    
}
