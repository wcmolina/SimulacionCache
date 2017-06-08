import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main {
    //RAM = 4096 bytes
    //cache = 512 bytes
    //bloque = 8 bytes

    static int RAM[];
    static int totalDirecciones = 4096;
    static int totalLineas = 64;
    static int tamanoBloque = 8;
    static double tiempo;

    //arreglo con numeros entre 0 y 255
    final static int DATOS[] = crearArreglo();

    //64 lineas, 3 columnas (0 = valido, 1 = modificado, 2 = etiqueta)
    static int cache[][] = new int[totalLineas][3];
    static HashMap<Integer, ArrayList<Integer>> direccionesEnCache = new HashMap();

    //banderas
    final static int VALIDO = 1;
    final static int MODIFICADO = 1;
    final static int INVALIDO = 0;
    final static int NO_MODIFICADO = 0;

    //restaura e invalida la cache y sus etiquetas
    public static void restaurarCache() {
        for (int i = 0; i < 64; i++) {
            cache[i][0] = 0;
            cache[i][1] = 0;
            cache[i][2] = 0;
        }
        direccionesEnCache = new HashMap<>();
    }

    //leer el data.txt y llenar un arreglo con esos numeros
    public static int[] crearArreglo() {
        int arreglo[] = new int[totalDirecciones];
        FileReader lector = null;
        try {
            lector = new FileReader("data.txt");
            BufferedReader buffer = new BufferedReader(lector);
            for (int i = 0; i < totalDirecciones; i++) {
                arreglo[i] = Integer.parseInt(buffer.readLine());
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error: no se encontró el archivo.");
        } catch (IOException e) {
            System.out.println("Error al leer el contenido del archivo.");
        }
        return arreglo;
    }

    //dada una linea de cache, revisa si en ella se encuentra la direccion
    public static boolean estaEnCache(int linea, int direccion) {
        if (direccionesEnCache.containsKey(linea))
            return direccionesEnCache.get(linea).contains(direccion);
        else
            return false;
    }

    //agregar un bloque de direcciones a la cache en determinada linea
    public static void moverBloqueACache(int linea, int bloque) {
        ArrayList<Integer> direcciones = new ArrayList();

        //se calcula la posicion inicial del bloque de la direccion
        int direccionInicial = bloque * tamanoBloque;
        for (int i = 0; i < tamanoBloque; i++) {
            direcciones.add(direccionInicial + i);
        }
        direccionesEnCache.put(linea, direcciones);
    }

    //lee de la cache (o RAM) a partir de una direccion y tipo de correspondencia
    public static int leer(int direccion, int tipo) {
        //double diferencia = tiempo;
        switch (tipo) {
            //sin cache
            case 0:
                tiempo += 0.1;
                break;

            //correspondencia directa
            case 1:
                int bloque = direccion / tamanoBloque;
                int linea = bloque % totalLineas;
                int etiqueta = bloque / totalLineas;

                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][1] = NO_MODIFICADO;
                    cache[linea][2] = etiqueta;
                    moverBloqueACache(linea, bloque);

                    //transferir de RAM a cache y luego leer de cache, +0.11
                    tiempo += 0.11;
                } else {
                    //linea es valida, entonces revisar si tienen la misma etiqueta
                    if (cache[linea][2] == etiqueta) {
                        //leer de la cache, +0.01
                        tiempo += 0.01;
                    } else {
                        //etiqueta es diferente, entonces revisar si ha sido modificada la linea
                        if (cache[linea][1] == MODIFICADO) {
                            cache[linea][1] = NO_MODIFICADO;
                            cache[linea][2] = etiqueta;
                            moverBloqueACache(linea, bloque);

                            //transferir bloque modificado de cache a RAM, luego transferir nuevo bloque de RAM a cache, +0.22
                            tiempo += 0.22;
                        } else {
                            //linea no modificada, entonces se puede transferir de la RAM a la cache
                            cache[linea][2] = etiqueta;
                            moverBloqueACache(linea, bloque);

                            //transferir de RAM a cache, +0.11
                            tiempo += 0.11;
                        }
                    }
                }
                //System.out.println("Leer -> dir: " + direccion + ", etiq: " + etiqueta + ", blq: "+bloque+", lin: "+linea+", t: " + redondeoDosCifras(tiempo)+", (+"+redondeoDosCifras(tiempo-diferencia)+")");
                break;
        }
        return RAM[direccion];
    }

    public static void escribir(int direccion, int tipo, int dato) {
        //double diferencia = tiempo;
        switch (tipo) {
            case 0:
                tiempo += 0.1;
                break;

            case 1:
                int bloque = direccion / tamanoBloque;
                int linea = bloque % totalLineas;
                int etiqueta = bloque / totalLineas;

                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][1] = MODIFICADO;
                    cache[linea][2] = etiqueta;
                    moverBloqueACache(linea, bloque);

                    //transferir de RAM a cache, +0.11
                    tiempo += 0.11;
                } else {
                    //linea es valida, entonces revisar si tienen la misma etiqueta
                    if (cache[linea][2] == etiqueta) {
                        cache[linea][1] = MODIFICADO;

                        //escribir a la cache, +0.01
                        tiempo += 0.01;
                    } else {
                        //etiqueta es diferente, entonces revisar si ha sido modificada la linea
                        if (cache[linea][1] == MODIFICADO) {
                            cache[linea][0] = VALIDO;
                            cache[linea][1] = MODIFICADO;
                            cache[linea][2] = etiqueta;
                            moverBloqueACache(linea, bloque);

                            //transferir de cache a RAM (para no perder ese bloque), luego transferir de RAM a cache el nuevo bloque, +0.22
                            tiempo += 0.22;
                        } else {
                            cache[linea][0] = VALIDO;
                            cache[linea][1] = MODIFICADO;
                            cache[linea][2] = etiqueta;
                            moverBloqueACache(linea, bloque);

                            //transferir de RAM a cache, +0.11
                            tiempo += 0.11;
                        }
                    }
                }
                //System.out.println("Escr -> dir: " + direccion + ", etiq: " + etiqueta + ", blq: " + bloque + ", lin: " + linea + ", t: " + redondeoDosCifras(tiempo)+", (+"+redondeoDosCifras(tiempo-diferencia)+")");
                break;
            default:
                break;
        }
        RAM[direccion] = dato;
    }

    public static double redondeoDosCifras(double numero) {
        return Math.round(numero * 1000.0) / 1000.0;
    }

    public static void ordenamiento() {
        RAM = DATOS;
        int tipo = 1;
        //Bubblesort
        for (int i = 0; i <= totalDirecciones - 2; i++) {
            for (int j = i + 1; j <= totalDirecciones - 1; j++) {
                if (leer(i, tipo) > leer(j, tipo)) {
                    int temp = leer(i, tipo);
                    escribir(i, tipo, leer(j, tipo));
                    escribir(j, tipo, temp);
                }
            }
        }

        System.out.println("El tiempo fue de: " + Math.round(tiempo * 1000.0) / 1000.0);
    }

    public static void pruebaEscritorio() {
        restaurarCache();
        int tipo = 1;

        //reset RAM
        RAM = new int[4096];
        tiempo = 0;
        escribir(100, tipo, 10); //fallo
        escribir(101, tipo, 13); //acierto
        escribir(102, tipo, 21); //acierto
        escribir(103, tipo, 11); //acierto
        escribir(104, tipo, 67); //fallo
        escribir(105, tipo, 43); //acierto
        escribir(106, tipo, 9); //acierto
        escribir(107, tipo, 11); //acierto
        escribir(108, tipo, 19); //acierto
        escribir(109, tipo, 23); //acierto
        escribir(110, tipo, 32); //acierto
        escribir(111, tipo, 54); //acierto
        escribir(112, tipo, 98); //fallo
        escribir(113, tipo, 7); //acierto
        escribir(114, tipo, 13); //acierto
        escribir(115, tipo, 1); //acierto

        //hasta aqui, tiempo debe valer 0.46 (esto esta bien)

        int menor = leer(100, tipo);
        int mayor = menor;
        int a = 0;
        for (int i = 101; i <= 115; i++) {
            a++;
            escribir(615, tipo, a);
            if (leer(i, tipo) < menor)
                menor = leer(i, tipo);
            if (leer(i, tipo) > mayor)
                mayor = leer(i, tipo);
            //System.out.println("i: " + i + ", tiempo: " + Math.round(tiempo * 1000.0) / 1000.0);
        }
        System.out.println("\nEl mayor es: " + mayor);
        System.out.println("El menor es: " + menor);
        System.out.println("El tiempo de la prueba de escritorio con tipo " + tipo + " fue de: " + Math.round(tiempo * 1000.0) / 1000.0);
    }

    public static void main(String[] args) {
        ordenamiento();
        //pruebaEscritorio();
    }
}
