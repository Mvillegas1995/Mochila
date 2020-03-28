package mochila;

import java.util.Random;

public class Mochila {
    //Data set de 15 objetos obtenido de https://people.sc.fsu.edu/~jburkardt/datasets/knapsack_01/knapsack_01.html
    public static final int capMochila = 750;
    public static final int[] pesoObj = { 70, 73, 77, 80, 82, 87, 90, 94, 98, 106, 110, 113, 115, 118, 120};
    public static final int[] valorObj = { 135 , 139, 149, 150, 156, 163, 173, 184, 192, 201, 210, 214, 221, 229, 240};
    public static final int cantObj = 15;
    public static float Tau;
    public static Random random;
    public static int[] mochila = { 0, 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static int[] mejorMochila;
    public static int mejorValor;
    public static int pesoFinal;
    //1 significa que está dentro de la mochila y 0 que está fuera de ella.
    //óptimo 1458
    public static void main(String[] args) {
        //Parámetros a modificar
        int semilla = 2;
        int iteraciones = 100;
        Tau = (float) 1.6;
        //Fin de parámetros a modificar
        random = new Random(semilla);
        // Inicializo mochila
        for (int i = cantObj-1; i > 0; i--) {
            if(capMochila>pesoMochila()+pesoObj[i])
                mochila[i] = 1;              
        }
        mejorMochila = new int[cantObj];
        for (int j = 0; j < cantObj; j++) {
            mejorMochila[j] = mochila[j]; 
        }
        mejorValor = valorMochila();
        pesoFinal = pesoMochila();
        System.out.println("Mochila inicial");
        for (int i = 0; i < cantObj; i++) {
            System.out.print(mochila[i]+" ");
        }
        System.out.println("");
        System.out.println("Valor: "+mejorValor+"   Peso: "+pesoFinal);
        System.out.println("");
        //Generando array de probabilidades
        float[] P = new float[cantObj];
        for (int i = 1; i <= cantObj; i++) {
            P[i-1] = (float) Math.pow(i, -Tau);
        }
        //Comienzan las iteraciones
        for (int i = 0; i < iteraciones; i++) {
            int objMochila = 0;
            int aux = 0;
            for (int j = 0; j < cantObj; j++) {
                objMochila += mochila[j];
            }
            //Columna 1 guarda el objeto, Columna 2 guarda el fitness
            int[][] fitnessLocal = new int[objMochila][2]; 
            for (int j = 0; j < cantObj; j++) {
                if(mochila[j]!=0){
                    fitnessLocal[aux][0] = j;
                    fitnessLocal[aux][1] = (valorObj[j]-pesoObj[j])*-1;
                    aux++;
                }
            }
            //Ordenar fitness de Peor a Mejor
            int temp1;
            int temp2;
            for (int j = 0; j < objMochila-1 ; j++) {
                for (int k = j+1; k < objMochila; k++) {
                    if (fitnessLocal[j][1]>fitnessLocal[k][1]) {
                        temp1 = fitnessLocal[j][0];
                        temp2 = fitnessLocal[j][1];
                        fitnessLocal[j][0] = fitnessLocal[k][0];
                        fitnessLocal[j][1] = fitnessLocal[k][1];
                        fitnessLocal[k][0] = temp1;
                        fitnessLocal[k][1] = temp2;
                    }
                }
            }
            //Obtengo los objetos fuera de la mochila
            int[] fueraMochila = new int[cantObj - objMochila];
            int aux2 = 0;
            for (int j = 0; j < fueraMochila.length; j++) {
                if(mochila[j]==0){
                    fueraMochila[aux2] = j;
                    aux2++;
                }                    
            }
            //Elijo un objeto fuera de la mochila para ingresar y uno de dentro para quitar
            int ingresa = random.nextInt(fueraMochila.length);
            int quitar = quitarObj(objMochila, fitnessLocal, P);
            //Cambio el "peor" por uno random
            mochila[fitnessLocal[quitar][0]] = 0;
            mochila[fueraMochila[ingresa]] = 1;
            //Si sobrepasa la capacidad máxima de la mochila vuelvo al estado anterior
            if(pesoMochila() > capMochila){
                mochila[fitnessLocal[quitar][0]] = 1;
                mochila[fueraMochila[ingresa]] = 0;
            }//Por si me cabe objeto más
            else{
                for (int j = cantObj-1; j >= 0; j--) {
                    if(mochila[j]==0){
                        if(pesoObj[j]+pesoMochila()<= capMochila)
                            mochila[j] = 1;
                    }
                }
                //Si es mejor que nuestra mejor mochila, actualizo los resultados
                if(valorMochila()>mejorValor){
                    for (int j = 0; j < cantObj; j++) {
                        mejorMochila[j] = mochila[j]; 
                    }
                    mejorValor = valorMochila();
                    pesoFinal = pesoMochila();
                }
            }            
        }
        System.out.println("Mochila Final");
        for (int i = 0; i < cantObj; i++) {
            System.out.print(mejorMochila[i]+" ");
        }
        System.out.println("");
        System.out.println("Valor: "+mejorValor+"   Peso: "+pesoFinal);
    }
    // Obtiene el valor total de los objetos dentro de la mochila 
    public static int valorMochila(){
        int valorMochila = 0;
        for (int i = 0; i < cantObj; i++) {
            valorMochila += mochila[i]*valorObj[i];
        }
        return valorMochila;
    }
    // Obtiene el peso total de los objetos dentro de la mochila 
    public static int pesoMochila(){
        int pesoMochila = 0;
        for (int i = 0; i < cantObj; i++) {
            pesoMochila += mochila[i]*pesoObj[i];
        }
        return pesoMochila;
    }
    //Elijo al componente más debil con el método de la ruleta
    public static int quitarObj(int objMochila, int[][] fitnessLocal, float[] P){
        int peorObj = 0;
        float Aleatorio = random.nextFloat();
        float[] ruleta = new float[objMochila];
        float total = 0;
        for (int i = 0; i < objMochila; i++) {
            total += P[i];
        }
        ruleta[0] = P[0]/total;
        for (int i = 1; i < objMochila; i++) {
            ruleta[i] = ruleta[i-1]+(P[i]/total); 
        }
        for (int i = 0; i < objMochila; i++) {
            if(i != 0 && Aleatorio > ruleta[i-1] && ruleta[i] >= Aleatorio){
                peorObj = i;
            }
            else {
                if(i==0 && Aleatorio < ruleta[i]){
                    peorObj = i;
                }
            }           
        }
        return peorObj;
    }
}
