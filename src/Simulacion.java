import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Simulacion {
    //RAM = 4096 bytes
    //cache = 512 bytes
    //bloque = 8 bytes

    private final int DIRECCIONES_RAM = 4096;
    private final int LINEAS_CACHE = 64;
    private final int TAMANO_BLOQUE = 8;
    private double tiempo;
    private int siguiente;

    //64 lineas, 3 columnas (0 = valido, 1 = modificado, 2 = etiqueta)
    private int cache[][] = new int[LINEAS_CACHE][3];
    private int RAM[];

    //banderas
    private final int VALIDO = 1;
    private final int MODIFICADO = 1;
    private final int INVALIDO = -1;
    private final int NO_MODIFICADO = -1;

    //leer el data.txt y llenar un arreglo con esos numeros
    private int[] crearArreglo() {
        int arreglo[] = new int[DIRECCIONES_RAM];
        FileReader lector = null;
        try {
            lector = new FileReader("data.txt");
            BufferedReader buffer = new BufferedReader(lector);
            for (int i = 0; i < DIRECCIONES_RAM; i++) {
                arreglo[i] = Integer.parseInt(buffer.readLine());
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error: no se encontró el archivo.");
        } catch (IOException e) {
            System.out.println("Error al leer el contenido del archivo.");
        }
        return arreglo;
    }

    private void restaurarCache() {
        for (int i = 0; i < 64; i++) {
            cache[i][0] = -1;
            cache[i][1] = -1;
            cache[i][2] = -1;
        }
    }

    private int estaEnCache(int bloque) {
        for (int i = 0; i < LINEAS_CACHE; i++) {
            if (cache[i][2] == bloque) return i;
        }
        return -1;
    }

    //lee de la cache (o RAM) a partir de una direccion y tipo de correspondencia
    private int leer(int direccion, int tipo) {
        switch (tipo) {
            //sin cache
            case 0:
                tiempo += 0.1;
                break;

            //directa
            case 1: {
                int bloque = direccion / TAMANO_BLOQUE;
                int linea = bloque % LINEAS_CACHE;
                int etiqueta = bloque / LINEAS_CACHE;

                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][1] = NO_MODIFICADO;
                    cache[linea][2] = etiqueta;

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
                            //transferir bloque modificado de cache a RAM, luego transferir nuevo bloque de RAM a cache, +0.22
                            tiempo += 0.22;
                        } else {
                            //linea no modificada, entonces se puede transferir de la RAM a la cache
                            //transferir de RAM a cache, +0.11
                            tiempo += 0.11;
                        }
                        //actualizar etiqueta siempre que se trae un bloque de la RAM
                        cache[linea][2] = etiqueta;
                    }
                }
            }
            break;

            //asociativa
            case 2: {
                //si el siguiente se pasa del tamaño de la cache, que regrese a la 0
                if (siguiente > 63) siguiente = 0;
                int bloque = direccion / TAMANO_BLOQUE;

                if (estaEnCache(bloque) != -1) {
                    tiempo += 0.01;
                    break;
                }

                //no estaba en cache entonces hay que traer el bloque de la RAM a la linea donde 'apunte' la variable 'siguiente'
                int linea = siguiente;
                // en el asociativo, la etiqueta es todo el bloque
                int etiqueta = bloque;

                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][1] = NO_MODIFICADO;
                    cache[linea][2] = etiqueta;
                    siguiente++;

                    //transferir de RAM a cache y luego leer de cache, +0.11
                    tiempo += 0.11;
                } else {
                    //linea es valida, entonces ya habia un bloque ahi. Ver si esa linea esta modificada
                    if (cache[linea][1] == MODIFICADO) {
                        cache[linea][1] = NO_MODIFICADO;
                        //transferir bloque modificado de cache a RAM, luego transferir nuevo bloque de RAM a cache, +0.22
                        tiempo += 0.22;
                    } else {
                        //se puede transferir de la RAM a la cache sin perder cambios en el bloque reemplazado
                        //transferir de RAM a cache, +0.11
                        tiempo += 0.11;
                    }
                    //actualizar etiqueta siempre que se trae un bloque de la RAM a la cache
                    cache[linea][2] = etiqueta;
                    siguiente++;
                }
            }
            break;

            //asociativa por conjuntos
            case 3: {

            }
            break;

            default: break;
        }

        return RAM[direccion];
    }

    private void escribir(int direccion, int tipo, int dato) {
        switch (tipo) {
            //sin cache
            case 0:
                tiempo += 0.1;
                break;

            //directa
            case 1: {
                int bloque = direccion / TAMANO_BLOQUE;
                int linea = bloque % LINEAS_CACHE;
                int etiqueta = bloque / LINEAS_CACHE;

                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][2] = etiqueta;
                    //transferir de RAM a cache, +0.11
                    tiempo += 0.11;
                } else {
                    //linea es valida, entonces revisar si tienen la misma etiqueta
                    if (cache[linea][2] == etiqueta) {
                        //escribir a la cache, +0.01
                        tiempo += 0.01;
                    } else {
                        //etiqueta es diferente, entonces revisar si ha sido modificada la linea
                        if (cache[linea][1] == MODIFICADO) {
                            //transferir de cache a RAM (para no perder ese bloque), luego transferir de RAM a cache el nuevo bloque, +0.22
                            tiempo += 0.22;
                        } else {
                            //transferir de RAM a cache, +0.11
                            tiempo += 0.11;
                        }
                        //actualizar etiqueta siempre que se trae un bloque de la RAM
                        cache[linea][2] = etiqueta;
                    }
                }
                //modificado porque se escribe en cache y no en RAM
                cache[linea][1] = MODIFICADO;
            }
            break;

            //asociativa
            case 2: {
                //si el siguiente se pasa del tamaño de la cache, que regrese a la 0
                if (siguiente > 63) siguiente = 0;
                int bloque = direccion / TAMANO_BLOQUE;
                int linea;

                if ((linea = estaEnCache(bloque)) != -1) {
                    tiempo += 0.01;
                    cache[linea][1] = MODIFICADO;
                    break;
                }

                //no estaba en cache entonces hay que traer el bloque de la RAM a la linea donde 'apunte' la variable 'siguiente'
                linea = siguiente;
                //en el asociativo, la etiqueta es todo el bloque
                int etiqueta = bloque;
                if (cache[linea][0] == INVALIDO) {
                    cache[linea][0] = VALIDO;
                    cache[linea][2] = etiqueta;
                    //transferir de RAM a cache, +0.11
                    tiempo += 0.11;
                } else {
                    //linea es valida, entonces ya habia un bloque ahi. Ver si esa linea esta modificada
                    if (cache[linea][1] == MODIFICADO) {
                        //transferir bloque de cache a RAM (para no perder los cambios), luego transferir de RAM a cache el nuevo bloque, +0.22
                        tiempo += 0.22;
                    } else {
                        //transferir de RAM a cache, +0.11
                        tiempo += 0.11;
                    }
                    //actualizar etiqueta siempre que se trae un bloque de la RAM
                    cache[linea][2] = etiqueta;
                }
                //se escribe en la cache y no en la RAM, entonces marcar la linea como modificada
                cache[linea][1] = MODIFICADO;
                //una vez escrito, que esriba la proxima en la siguiente linea
                siguiente++;
            }
            break;

            //asociativa por conjuntos
            case 3: {

            }
            break;

            default: break;
        }

        RAM[direccion] = dato;
    }

    private void ordenar(int tipo) {
        for (int i = 0; i <= DIRECCIONES_RAM - 2; i++) {
            for (int j = i + 1; j <= DIRECCIONES_RAM - 1; j++) {
                if (leer(i, tipo) > leer(j, tipo)) {
                    int temp = leer(i, tipo);
                    escribir(i, tipo, leer(j, tipo));
                    escribir(j, tipo, temp);
                }
            }
        }
    }

    public void correr() {
        final int DATOS[] = crearArreglo();
        final String TIPOS[] = new String[] { "Sin cache", "Directa", "Asociativa", "Asociativa por conjuntos" };

        System.out.printf("Resultados (tiempo en microsegundos)\n\nCon n = %d\n", DIRECCIONES_RAM);
        for (int tipo = 0; tipo < 4; tipo++) {
            //restaurar RAM, cache, y tiempo
            RAM = Arrays.copyOf(DATOS, DIRECCIONES_RAM);
            restaurarCache();
            tiempo = 0;

            ordenar(tipo);
            System.out.format("%s -> %.2f\n", TIPOS[tipo], tiempo);
        }
    }
}
