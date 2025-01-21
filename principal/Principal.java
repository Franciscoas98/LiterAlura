package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoApi;
import com.alura.literalura.service.ConvierteDatos;

import java.util.*;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoAPI = new ConsumoApi();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<Libro> librosRegistrados = new ArrayList<>();
    private List<Autor> autoresRegistrados = new ArrayList<>();
    private Datos datos;
    private AutorRepository autorRepositorio;
    private LibroRepository libroRepositorio;

    public Principal(AutorRepository autorRepositorio, LibroRepository libroRepository) {
        this.autorRepositorio = autorRepositorio;
        this.libroRepositorio = libroRepository;    }


    public void mostrarMenu(){

            int opcion = 1;

            do{
                var menu = """
                        ELIJA UNA OPCIÓN:
                        1- BUSCAR LIBROS POR TITULO
                        2- LISTAR LIBROS REGISTRADOS
                        3- LISTAR AUTORES REGISTRADOS
                        4- LISTAR AUTORES VIVOS EN UN DETERMINADO AÑO
                        5- LISTAR LIBROS POR IDIOMA
                        6- TOP 10 LIBROS
                        7- CONSULTAR AUTOR POR NOMBRE
                        0- SALIR
                        """;
                try {
                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                    switch (opcion) {
                        case 1:
                            buscarLibroPorTitulo();
                            break;
                        case 2:
                            listarLibrosRegistrados();
                            break;
                        case 3:
                            listarAutoresRegistrados();
                            break;
                        case 4:
                            listarAutoresVivosEnUnDeterminadoAnho();
                            break;
                        case 5:
                            listarLibrosPorIdioma();
                            break;
                        case 6:
                            Top10Libros();
                            break;
                        case 7:
                            ConsultarAutorPorNombre();
                            break;

                        case 0:
                            System.out.println("""             
                                       ¡HASTA LUEGO!....................
                                    """);
                            break;
                        default:
                            System.out.println("""
                                    ********************  FAVOR DE ELEGIR UNA OPCIÓN DE LA LISTA  *********************
                                    """);
                            break;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("""
                                        **********************  OPCION NO VÁLIDA  ********************
                                        """);
                    teclado.nextLine();
                    }
            } while (opcion != 0);
    }

    private Datos getDatosLibro() {
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatosAPI(URL_BASE + "?search=" + tituloLibro.replace(" ", "%20"));
        datos = conversor.obtenerDatos(json, Datos.class);
        return datos;

    }


    private Libro crearLibro(DatosLibro datosLibro, Autor autor) {
        Libro libro = new Libro(datosLibro, autor);
        return libroRepositorio.save(libro);
    }


    private void buscarLibroPorTitulo() {
        System.out.println("ESCRIBE EL TÍTULO DEL LIBRO A CONSULTAR");
        Datos datos = getDatosLibro();
        if (!datos.resultados().isEmpty()) {
            DatosLibro datosLibro = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibro.autor().get(0);
            Libro libro = null;
            Libro libroDb = libroRepositorio.findByTitulo(datosLibro.titulo());
            if (libroDb != null) {
                System.out.println(libroDb );
            } else {
                Autor autorDb = autorRepositorio.findByNombreIgnoreCase(datosLibro.autor().get(0).nombre());
                if (autorDb == null) {
                    Autor autor = new Autor(datosAutor);
                    autor = autorRepositorio.save(autor);
                    libro = crearLibro(datosLibro, autor);
                    System.out.println(libro);
                } else {
                    libro = crearLibro(datosLibro, autorDb);
                    System.out.println(libro);
                }
            }
        } else {
            System.out.println("""
            EL LIBRO NO EXISTE..............
        """);
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> librosRegistrados = libroRepositorio.findAll();
        librosRegistrados.stream()
                .sorted(Comparator.comparing(libro -> libro.getAutor().getNombre()))
                .forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        autoresRegistrados = autorRepositorio.findAll();
        autoresRegistrados.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void listarAutoresVivosEnUnDeterminadoAnho() {
        System.out.println("INGRESE UN AÑO PARA VALIDAR AUTORES VIVOS: ");
        try {
            int yearQuery = teclado.nextInt();
            teclado.nextLine();
            List<Autor> autoresVivos = autorRepositorio.autorVivosEnDeterminadoYear(yearQuery);
            if(autoresVivos.isEmpty()){
                System.out.println("""
                     ******************* NO HAY AUTORES EN EL AÑO SELECIONADO **********
                """);
            }else{
                autoresVivos.forEach(System.out::println);
            }

        } catch (InputMismatchException e) {
            teclado.nextLine();
            System.out.println("""
                    *********************** INGRESAR EL AÑO EN NUMERO  **********************
                    """);
        }
    }

    private void listarLibrosPorIdioma() {
        String idioma;
        System.out.println("""
   ************************************************************
                        IDIOMA DISPONIBLE:
                        1 - Español
                        2 - Inglés
                        3 - Francés
                        4 - Portugues
   *************************************************************
""");

        var opcion = teclado.nextInt();
        teclado.nextLine();

        if (opcion == 1) {
            idioma = "es";
        } else if (opcion == 2) {
            idioma = "en";
        } else if (opcion == 3) {
            idioma = "fr";
        } else if (opcion == 4) {
            idioma = "pt";
        }else {
            idioma = null;
            System.out.println("""
            *************************  OPCION NO VALIDA  ************************
            """);
        }

        List<Libro> librosPorIdioma = libroRepositorio.findByIdiomasContaining(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("""
            ************ NO HAY LIBROS PARA EL IDIOMA SELECIONADO  **************** 
            """);
        } else {
            var  cantidadLibrosPorIdioma =libroRepositorio.countByLanguage(idioma);
            System.out.println( " " + "\n" +

                    "**************** HAY " + cantidadLibrosPorIdioma + " LIBROS EN ESTE IDIOMA ******************" + "\n" +

                    "  ");
            librosPorIdioma.forEach(System.out::println);
        }

    }


    private void Top10Libros() {
        var top10LibrosDescargados = libroRepositorio.findTop10ByOrderByNumeroDescargasDesc();
        top10LibrosDescargados.forEach(System.out::println);

    }

    private void ConsultarAutorPorNombre() {
        System.out.println("INGRESE UN AUTOR A CONSULTAR ");
        var nombreAutor = teclado.nextLine();
        var autorPorNombre = autorRepositorio.findByNombreIgnoreCase(nombreAutor);
        if(autorPorNombre != null) {
            var  ID_AUTOR = autorPorNombre.getId();
            var libroEscritosPorEseAutor = libroRepositorio.findByAutor(autorPorNombre);
            System.out.println(
                    "************************************************************************************"  + "\n" +
                            "**********************************     AUTOR            ****************************"   + "\n" +
                            autorPorNombre + "\n" +
                            "" + "\n" +
                            "***********************  LIBROS ESCRITOS                ****************************"
            );
            libroEscritosPorEseAutor.stream()
                    .sorted(Comparator.comparing(Libro::getNumeroDescargas).reversed())
                    .forEach(libro -> System.out.println(libro));

        }

    }

}
