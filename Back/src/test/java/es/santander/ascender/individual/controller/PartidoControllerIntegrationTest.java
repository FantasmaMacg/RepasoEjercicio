package es.santander.ascender.individual.controller;

import es.santander.ascender.individual.model.Partido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PartidoControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private PartidoController partidoController;

        private Partido partido1;
        private Partido partido2;
        private Partido partido3;


        @BeforeEach
        void setUp() {
                partido1 = new Partido(1, "Partido A", "Descripción A", "futbol", 0, 55.5f);
                partido2 = new Partido(2, "Partido B", "Descripción B", "tenis", 1, 10.0f); // Partido ya ganado
                partido3 = new Partido(3, "Partido C", "Descripción C", "tenis", -1, 10.0f); // Partido ya perdido

                partidoController.getPartidos().put(1l, partido1);
                partidoController.getPartidos().put(2l, partido2);
        }

        @Test
        void testCrearPartido() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());
        }


        @Test
        void testObtenerPartidoExistente() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());

                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/1"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.nombre").value("Partido A"));
        }

        @Test
        void testObtenerPartidoNoExistente() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testGana() throws Exception {
                // Crear el partido
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());

                // Ganar el partido (debe incrementar el resultado)
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos/1/gana"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.content()
                                                .string("Gana: Partido A"));

                // Verificar que el esta ganado
                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/1"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.resultado").value(1));
        }

        @Test
        void testGanaYaGanado() throws Exception {
                // Crear el partido sin stock
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido2)))
                                .andExpect(status().isCreated());

                // Intentar ganar el partido (debe devolver BadRequest debido a que ya está ganado
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos/2/gana"))
                                .andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.content().string("No se puede superar 1"));
        }

        @Test
        void testPierde() throws Exception {
                // Crear el partido
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());

                // Ganar el partido (debe decrementar el resultado)
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos/2/pierde"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.content()
                                                .string("Pierde: Partido B"));

                // Verificar que el esta perdido
                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/2"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.resultado").value(0));
        }

        @Test
        void testGanaYaPerdido() throws Exception {
                // Crear el partido sin stock
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido2)))
                                .andExpect(status().isCreated());

                // Intentar perder el partido (debe devolver BadRequest debido a que ya está perdido
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos/3/pierde"))
                                .andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.content().string("No se puede bajar de -1"));
        }


        @Test
        void testActualizarPartida() throws Exception {
                // Crear el partido
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());

                // Crear el partido actualizado
                Partido partidoActualizado = new Partido(1, "Partido A Actualizado", "Descripción Actualizada",
                        "futbol", -1, 55.5f);

                // Actualizar el partido
                mockMvc.perform(MockMvcRequestBuilders.put("/partidos/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partidoActualizado)))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.nombre").value("Partido A Actualizado"))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.descripcion")
                                                .value("Descripción Actualizada"))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.resultado").value(-1));
        }

        @Test
        void testEliminarPartido() throws Exception {
                // Crear el partido
                mockMvc.perform(MockMvcRequestBuilders.post("/partidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partido1)))
                                .andExpect(status().isCreated());

                // Eliminar el partido
                mockMvc.perform(MockMvcRequestBuilders.delete("/partidos/1"))
                                .andExpect(status().isNoContent());

                // Verificar que el partido ya no existe
                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/1"))
                                .andExpect(status().isNotFound());
        }

        // Test para comprobar que se suben los ficheros correctamente
        @Test
        void testSubirFichero() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                        "file", 
                        "fichero.txt", 
                        "text/plain", 
                        "Contenido del archivo".getBytes()
                );

                mockMvc.perform(MockMvcRequestBuilders.multipart("/partidos/upload?id=1")
                                .file(file)
                                .param("nombre", "fichero.txt"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.content().string("Fichero subido con éxito: fichero.txt"));
        }

        // Descargar fichero
        @Test
        void testDescargarFichero() throws Exception {
                // Subir el fichero
                MockMultipartFile file = new MockMultipartFile(
                        "file", 
                        "fichero.txt", 
                        "text/plain", 
                        "Contenido del archivo".getBytes()
                );

                mockMvc.perform(MockMvcRequestBuilders.multipart("/partidos/upload?id=1")
                                .file(file)
                                .param("nombre", "fichero.txt"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.content().string("Fichero subido con éxito: fichero.txt"));

                // Descargar el fichero
                mockMvc.perform(MockMvcRequestBuilders.get("/partidos/download?id=1"))
                                .andExpect(status().isOk())
                                .andExpect(MockMvcResultMatchers.content().string("Contenido del archivo"));
        }
        
}
